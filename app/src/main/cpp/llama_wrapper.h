#ifndef LLAMA_WRAPPER_H
#define LLAMA_WRAPPER_H

#include <string>
#include <functional>
#include <memory>
#include <atomic>
#include "moe_streamer.h"

namespace commonmsm {

using TokenCallback = std::function<bool(const std::string& token, float speed_tps)>;

struct EngineConfig {
    std::string model_path;
    int n_threads = 4;
    int n_ctx = 4096;
    bool enable_moe_streaming = false;
    size_t moe_cache_mb = 4096;
    float temperature = 0.6f;
    float top_p = 0.9f;
};

class LlamaEngineWrapper {
public:
    LlamaEngineWrapper();
    ~LlamaEngineWrapper();

    bool load_model(const EngineConfig& config);
    void unload_model();
    bool is_loaded() const;

    bool generate(const std::string& prompt, TokenCallback callback);
    void stop_generation();

    std::string get_performance_stats_json() const;

private:
    EngineConfig config_;
    std::atomic<bool> is_loaded_{false};
    std::atomic<bool> should_stop_{false};
    std::unique_ptr<MoeDiskStreamer> moe_streamer_;

    uint64_t total_tokens_generated_{0};
    double last_generation_tps_{0.0};
    double last_prompt_eval_tps_{0.0};
};

} // namespace commonmsm

#endif // LLAMA_WRAPPER_H
