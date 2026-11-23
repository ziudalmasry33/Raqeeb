package com.raqeeb.app.domain.model

enum class ProtectionMode(
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String
) {
    REMINDER(
        titleAr = "وضع التذكير",
        titleEn = "Reminder Mode",
        descAr = "يرسل تذكيرات إيمانية لطيفة بعد 5 ثوانٍ من فتح التطبيقات المقيدة دون حظر.",
        descEn = "Sends gentle Islamic reminders 5s after opening restricted apps without blocking."
    ),
    SMART(
        titleAr = "الحماية الذكية",
        titleEn = "Smart Shield",
        descAr = "تصفية ذكية فائقة السرعة للمواقع الضارة وغير اللائقة مع تنبيهات هادئة للتطبيقات.",
        descEn = "Ultra-fast safe DNS filtering for harmful content with calm app reminders."
    ),
    STRICT(
        titleAr = "الحماية الصارمة",
        titleEn = "Strict Guard",
        descAr = "حظر كامل للنطاقات غير اللائقة وإغلاق فوري لأي تطبيق مقيد لحفظ وقتك وبصرك.",
        descEn = "Full safe domain filtering and immediate redirection on restricted apps."
    )
}

enum class DomainCategory(val labelAr: String, val labelEn: String) {
    SAFE_SHIELD("قاعدة حفظ البصر والتصفح الآمن", "Safe Browsing & Gaze Shield"),
    HARMFUL("مواقع المراهنات والمحتوى الضار", "Harmful & Distracting Sites"),
    CUSTOM("نطاقات مخصصة", "Custom Restricted Domains")
}

data class RestrictedDomain(
    val id: Long,
    val maskedLabel: String,
    val rawDomain: String,
    val category: DomainCategory,
    val isEnabled: Boolean = true,
    val isBuiltInShield: Boolean = false
)

data class RestrictedApp(
    val packageName: String,
    val appName: String,
    val isRestricted: Boolean = false
)

data class IslamicReminder(
    val textAr: String,
    val textEn: String,
    val sourceAr: String,
    val sourceEn: String
)

data class AdminBroadcast(
    val id: Long,
    val title: String,
    val body: String,
    val timestamp: String
)

data class DailyChallenge(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val categoryAr: String,
    val categoryEn: String,
    val targetDays: Int,
    val completedDays: Int,
    val isCompletedToday: Boolean,
    val weekHistory: List<Boolean> = listOf(true, true, false, false, false, false, false)
)
