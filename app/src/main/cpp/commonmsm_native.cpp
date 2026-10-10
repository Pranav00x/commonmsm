#include <jni.h>
#include <string>
#include <memory>
#include <mutex>
#include <algorithm>
#include <android/log.h>
#include "llama_wrapper.h"

#define TAG "CommonMSM_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static std::unique_ptr<commonmsm::LlamaEngineWrapper> g_engine = nullptr;
static std::mutex g_engine_mutex;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_commonmsm_engine_LlamaEngineBridge_nativeInitEngine(
        JNIEnv *env,
        jobject /* this */,
        jstring model_path,
        jint n_threads,
        jint n_ctx,
        jboolean enable_moe_streaming,
        jlong moe_cache_mb) {

    if (!model_path) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    if (!path) return JNI_FALSE;

    commonmsm::EngineConfig config;
    config.model_path = std::string(path);
    config.n_threads = std::clamp(static_cast<int>(n_threads), 1, 32);
    config.n_ctx = std::clamp(static_cast<int>(n_ctx), 128, 32768);
    config.enable_moe_streaming = enable_moe_streaming;
    config.moe_cache_mb = (moe_cache_mb > 0 && moe_cache_mb <= 32768) ? static_cast<size_t>(moe_cache_mb) : 4096;

    env->ReleaseStringUTFChars(model_path, path);

    std::lock_guard<std::mutex> lock(g_engine_mutex);
    g_engine = std::make_unique<commonmsm::LlamaEngineWrapper>();
    bool ok = g_engine->load_model(config);

    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_commonmsm_engine_LlamaEngineBridge_nativeUnloadEngine(
        JNIEnv * /* env */,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_engine_mutex);
    if (g_engine) {
        g_engine->unload_model();
        g_engine.reset();
    }
}

JNIEXPORT jboolean JNICALL
Java_com_commonmsm_engine_LlamaEngineBridge_nativeGenerate(
        JNIEnv *env,
        jobject /* this */,
        jstring prompt,
        jobject callback_obj) {

    if (!prompt || !callback_obj) {
        LOGE("nativeGenerate called with null arguments");
        return JNI_FALSE;
    }

    std::unique_lock<std::mutex> lock(g_engine_mutex);
    if (!g_engine || !g_engine->is_loaded()) {
        LOGE("nativeGenerate called but engine is not loaded");
        return JNI_FALSE;
    }
    auto* engine_ptr = g_engine.get();
    lock.unlock();

    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    if (!prompt_str) return JNI_FALSE;
    std::string prompt_cpp(prompt_str);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    jclass callback_class = env->GetObjectClass(callback_obj);
    if (!callback_class) return JNI_FALSE;

    jmethodID on_token_method = env->GetMethodID(callback_class, "onToken", "(Ljava/lang/String;F)Z");
    if (!on_token_method) {
        env->DeleteLocalRef(callback_class);
        return JNI_FALSE;
    }

    bool success = engine_ptr->generate(prompt_cpp, [&](const std::string& token, float tps) -> bool {
        jstring jtoken = env->NewStringUTF(token.c_str());
        if (!jtoken) return false;
        jboolean keep_going = env->CallBooleanMethod(callback_obj, on_token_method, jtoken, tps);
        env->DeleteLocalRef(jtoken);
        return keep_going == JNI_TRUE;
    });

    env->DeleteLocalRef(callback_class);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_commonmsm_engine_LlamaEngineBridge_nativeStopGeneration(
        JNIEnv * /* env */,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_engine_mutex);
    if (g_engine) {
        g_engine->stop_generation();
    }
}

JNIEXPORT jstring JNICALL
Java_com_commonmsm_engine_LlamaEngineBridge_nativeGetEngineStats(
        JNIEnv *env,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_engine_mutex);
    if (!g_engine) {
        return env->NewStringUTF("{}");
    }
    std::string json = g_engine->get_performance_stats_json();
    return env->NewStringUTF(json.c_str());
}

} // extern "C"
