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
        descAr = "يكتشف التطبيقات والمواقع المقيدة ويرسل إشعارات تذكير إيمانية دون حظر الوصول.",
        descEn = "Detects restricted sites/apps and sends gentle Islamic notifications without blocking."
    ),
    SMART(
        titleAr = "الحماية الذكية",
        titleEn = "Smart Protection",
        descAr = "يحظر كافة المواقع الإباحية والضارة تلقائياً عبر DNS العائلي الآمن ويُذكّرك عند فتح التطبيقات المقيدة.",
        descEn = "Automatically blocks all adult/harmful sites via Family DNS and reminds on restricted apps."
    ),
    STRICT(
        titleAr = "الحماية الصارمة",
        titleEn = "Strict Protection",
        descAr = "حظر شامل لكل المواقع الإباحية وإغلاق فوري لأي تطبيق مقيد عند محاولة فتحه.",
        descEn = "Full adult domain blocking and immediate home redirection on restricted apps."
    )
}

enum class DomainCategory(val labelAr: String, val labelEn: String) {
    ADULT("قاعدة المواقع الإباحية (مدمجة)", "Adult Sites Shield (Built-in)"),
    INAPPROPRIATE("محتوى غير لائق", "Inappropriate Content"),
    CUSTOM("مواقع مخصصة", "User-Defined")
}

data class RestrictedDomain(
    val id: Long,
    val maskedLabel: String,
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

data class DailyChallenge(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val targetDays: Int,
    val completedDays: Int,
    val isCompletedToday: Boolean
)
