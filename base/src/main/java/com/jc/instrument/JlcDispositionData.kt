package com.jc.instrument

/**
 * "s": ["chat", "video_call", "voice_call", "game", "share", "meetup"],
"i": [12, 45, 87, 23, 94, 36],
"d": [1.23, 4.56, 7.89, 0.12, 3.45, 6.78],
"z": [true, false, true, false, true, false],
"f": [1.2, 3.4, 5.6, 7.8, 9.0, 1.3]
 */
data class JlcDispositionData(
    val s: List<String>,
    val i: List<Int>,
    val d: List<Double>,
    val z: List<Boolean>,
    val f: List<Float>,
)