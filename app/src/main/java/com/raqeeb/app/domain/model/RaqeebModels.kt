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
        descAr = "يكتشف المواقع والتطبيقات المقيدة ويرسل تذكيرات إيمانية لطيفة دون حظر الوصول.",
        descEn = "Detects restricted sites/apps and displays gentle Islamic reminders without blocking."
    ),
    SMART(
        titleAr = "الحماية الذكية",
        titleEn = "Smart Protection",
        descAr = "يحظر المواقع المصنفة عبر VPN المحلي ويُذكّرك عند فتح التطبيقات المختارة.",
        descEn = "Blocks categorized domains via local VPN and reminds you on selected apps."
    ),
    STRICT(
        titleAr = "الحماية الصارمة",
        titleEn = "Strict Protection",
        descAr = "يُفعّل الحظر الكامل للنطاقات المقيدة ويمنع الوصول للتطبيقات المقيدة بشاشة تدخل.",
        descEn = "Enforces full domain blocking and prevents access to restricted apps with intervention."
    )
}

enum class DomainCategory(val labelAr: String, val labelEn: String) {
    ADULT("محتوى للبالغين", "Adult Content"),
    INAPPROPRIATE("محتوى غير لائق", "Inappropriate Content"),
    CUSTOM("مواقع مخصصة", "User-Defined")
}

data class RestrictedDomain(
    val id: Long,
    val domain: String,
    val category: DomainCategory,
    val isEnabled: Boolean = true
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

data class DailyChallenge(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val targetDays: Int,
    val completedDays: Int,
    val isCompletedToday: Boolean
)
