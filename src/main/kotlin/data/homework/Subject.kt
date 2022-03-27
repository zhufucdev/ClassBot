package com.zhufucdev.data.homework

enum class Subject(vararg names: String) {
    CHINESE("语文", "国文"), ENGLISH("英语"), MATHEMATICS("数学"),
    PHYSICS("物理"), CHEMISTRY("化学"), BIOLOGY("生物"),
    GEOGRAPHY("地理"), POLITICS("政治"), HISTORY("历史");
    val names = arrayOf(*names)

    companion object {
        fun named(name: String) = values().firstOrNull { it.names.any { n -> n.equals(name, ignoreCase = true) } }
    }
}