package com.commonmsm.engine

interface NativeTokenCallback {
    fun onToken(token: String, speedTps: Float): Boolean
}

object LlamaEngineBridge {
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("commonmsm_native")
            isNativeLoaded = true
        } catch (_: Throwable) {
            isNativeLoaded = false
        }
    }

    fun isAvailable(): Boolean = isNativeLoaded

    external fun nativeInitEngine(
        modelPath: String,
        nThreads: Int,
        nCtx: Int,
        enableMoeStreaming: Boolean,
        moeCacheMb: Long
    ): Boolean

    external fun nativeUnloadEngine()

    external fun nativeGenerate(
        prompt: String,
        callback: NativeTokenCallback
    ): Boolean

    external fun nativeStopGeneration()

    external fun nativeGetEngineStats(): String
}
