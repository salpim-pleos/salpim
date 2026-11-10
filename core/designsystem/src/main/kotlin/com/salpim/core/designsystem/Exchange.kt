package com.salpim.core.designsystem

/** 음성 바에 남기는 마지막 주고받음. [heard]가 null이면 앱이 먼저 말을 건 것 */
data class Exchange(val heard: String?, val reply: String)
