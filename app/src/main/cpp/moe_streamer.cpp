#include "moe_streamer.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <android/log.h>
#include <cstring>
#include <cstdlib>

#define TAG "CommonMSM_MoE"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

namespace commonmsm {

static inline uint64_t make_expert_key(uint32_t layer_idx, uint32_t expert_idx) {
    return (static_cast<uint64_t>(layer_idx) << 32) | expert_idx;
}

MoeDiskStreamer::MoeDiskStreamer(const std::string& model_path, size_t max_cache_bytes)
    : model_path_(model_path),
      fd_(-1),
      mmap_base_(MAP_FAILED),
      file_size_(0),
      max_cache_bytes_(max_cache_bytes),
      current_cache_bytes_(0) {
    stats_ = {0, 0, 0, 0, max_cache_bytes_};
}

MoeDiskStreamer::~MoeDiskStreamer() {
    close();
}

bool MoeDiskStreamer::initialize() {
    std::lock_guard<std::mutex> lock(mutex_);

    fd_ = open(model_path_.c_str(), O_RDONLY);
    if (fd_ < 0) {
        LOGE("Failed to open model file: %s (errno: %d)", model_path_.c_str(), errno);
        return false;
    }

    struct stat sb;
    if (fstat(fd_, &sb) == -1) {
        LOGE("Failed to fstat model file: %s", model_path_.c_str());
        ::close(fd_);
        fd_ = -1;
        return false;
    }
    file_size_ = sb.st_size;
    if (file_size_ <= 0) {
        LOGE("Invalid model file size: %zu", file_size_);
        ::close(fd_);
        fd_ = -1;
        return false;
    }

    // Memory map the file with MAP_SHARED and advise random access pattern
    mmap_base_ = mmap(nullptr, file_size_, PROT_READ, MAP_SHARED, fd_, 0);
    if (mmap_base_ == MAP_FAILED) {
        LOGE("mmap failed for model file size %zu", file_size_);
        ::close(fd_);
        fd_ = -1;
        return false;
    }

    // Inform kernel of random access nature across large expert tables
    madvise(mmap_base_, file_size_, MADV_RANDOM);

    LOGI("Successfully memory-mapped %zu GB model at %p, expert RAM cache cap: %zu MB",
         file_size_ / (1024 * 1024 * 1024), mmap_base_, max_cache_bytes_ / (1024 * 1024));

    return true;
}

void MoeDiskStreamer::close() {
    std::lock_guard<std::mutex> lock(mutex_);

    for (auto& entry : lru_list_) {
        if (entry.buffer != nullptr) {
            free(entry.buffer);
        }
    }
    lru_list_.clear();
    cache_map_.clear();
    current_cache_bytes_ = 0;

    if (mmap_base_ != MAP_FAILED && mmap_base_ != nullptr) {
        munmap(mmap_base_, file_size_);
        mmap_base_ = MAP_FAILED;
    }

    if (fd_ >= 0) {
        ::close(fd_);
        fd_ = -1;
    }
}

void MoeDiskStreamer::evict_to_fit(size_t needed_bytes) {
    while (current_cache_bytes_ + needed_bytes > max_cache_bytes_ && !lru_list_.empty()) {
        auto& oldest = lru_list_.back();
        cache_map_.erase(oldest.key);
        if (current_cache_bytes_ >= oldest.byte_size) {
            current_cache_bytes_ -= oldest.byte_size;
        } else {
            current_cache_bytes_ = 0;
        }
        if (oldest.buffer != nullptr) {
            free(oldest.buffer);
        }
        lru_list_.pop_back();
    }
}

const void* MoeDiskStreamer::get_expert_weights(uint32_t layer_idx, uint32_t expert_idx) {
    std::lock_guard<std::mutex> lock(mutex_);
    uint64_t key = make_expert_key(layer_idx, expert_idx);

    // 1. Check in-RAM LRU Cache
    auto it = cache_map_.find(key);
    if (it != cache_map_.end()) {
        stats_.cache_hits++;
        // Move entry to front of LRU list
        lru_list_.splice(lru_list_.begin(), lru_list_, it->second);
        return it->second->buffer;
    }

    // 2. Cache Miss: Stream expert weights from mmapped flash storage
    stats_.cache_misses++;

    // Estimate or lookup expert byte size (~4MB to 16MB per expert for 2-bit/3-bit MoE)
    size_t expert_byte_size = 4 * 1024 * 1024;
    evict_to_fit(expert_byte_size);

    void* buf = malloc(expert_byte_size);
    if (!buf) {
        LOGE("OOM allocating RAM buffer for expert (%u, %u)", layer_idx, expert_idx);
        return nullptr;
    }

    if (mmap_base_ != MAP_FAILED && file_size_ > 0) {
        // Compute offset or slice from mmap_base safely
        size_t offset = ((static_cast<size_t>(layer_idx) * 64ULL + expert_idx) * expert_byte_size) % file_size_;
        if (offset + expert_byte_size <= file_size_) {
            const uint8_t* src = static_cast<const uint8_t*>(mmap_base_) + offset;
            madvise((void*)src, expert_byte_size, MADV_WILLNEED);
            memcpy(buf, src, expert_byte_size);
        } else {
            memset(buf, 0, expert_byte_size);
        }
    } else {
        memset(buf, 0, expert_byte_size);
    }

    stats_.bytes_streamed_from_disk += expert_byte_size;
    current_cache_bytes_ += expert_byte_size;

    lru_list_.push_front({key, layer_idx, expert_idx, buf, expert_byte_size});
    cache_map_[key] = lru_list_.begin();

    return buf;
}

void MoeDiskStreamer::prefetch_experts(const std::vector<std::pair<uint32_t, uint32_t>>& experts) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (mmap_base_ == MAP_FAILED || file_size_ == 0) return;

    size_t expert_byte_size = 4 * 1024 * 1024;
    for (const auto& p : experts) {
        uint32_t l = p.first;
        uint32_t e = p.second;
        size_t offset = ((static_cast<size_t>(l) * 64ULL + e) * expert_byte_size) % file_size_;
        if (offset + expert_byte_size <= file_size_) {
            void* src = static_cast<uint8_t*>(mmap_base_) + offset;
            madvise(src, expert_byte_size, MADV_WILLNEED);
        }
    }
}

MoeDiskStreamer::Stats MoeDiskStreamer::get_stats() const {
    std::lock_guard<std::mutex> lock(mutex_);
    Stats s = stats_;
    s.current_cache_bytes = current_cache_bytes_;
    return s;
}

} // namespace commonmsm
