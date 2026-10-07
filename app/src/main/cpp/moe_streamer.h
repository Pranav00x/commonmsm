#ifndef MOE_STREAMER_H
#define MOE_STREAMER_H

#include <string>
#include <vector>
#include <unordered_map>
#include <list>
#include <mutex>
#include <cstdint>
#include <cstddef>

namespace commonmsm {

struct ExpertHeader {
    uint32_t layer_idx;
    uint32_t expert_idx;
    size_t file_offset;
    size_t byte_size;
};

struct ExpertCacheEntry {
    uint64_t key; // (layer_idx << 32) | expert_idx
    uint32_t layer_idx;
    uint32_t expert_idx;
    void* buffer;
    size_t byte_size;
};

class MoeDiskStreamer {
public:
    MoeDiskStreamer(const std::string& model_path, size_t max_cache_bytes);
    ~MoeDiskStreamer();

    bool initialize();
    void close();

    // Fetches pointer to expert weights, streaming from flash if not cached
    const void* get_expert_weights(uint32_t layer_idx, uint32_t expert_idx);

    // Prefetch a set of experts expected in upcoming tokens
    void prefetch_experts(const std::vector<std::pair<uint32_t, uint32_t>>& experts);

    struct Stats {
        uint64_t cache_hits;
        uint64_t cache_misses;
        uint64_t bytes_streamed_from_disk;
        size_t current_cache_bytes;
        size_t max_cache_bytes;
        double hit_rate() const {
            uint64_t total = cache_hits + cache_misses;
            return total > 0 ? static_cast<double>(cache_hits) / total : 0.0;
        }
    };

    Stats get_stats() const;

private:
    std::string model_path_;
    int fd_;
    void* mmap_base_;
    size_t file_size_;
    size_t max_cache_bytes_;
    size_t current_cache_bytes_;

    mutable std::mutex mutex_;
    std::unordered_map<uint64_t, ExpertHeader> expert_catalog_;

    // LRU cache structures
    std::list<ExpertCacheEntry> lru_list_;
    std::unordered_map<uint64_t, std::list<ExpertCacheEntry>::iterator> cache_map_;

    Stats stats_;

    void evict_to_fit(size_t needed_bytes);
};

} // namespace commonmsm

#endif // MOE_STREAMER_H
