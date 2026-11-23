package com.raqeeb.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.raqeeb.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RaqeebUiState(
    val isArabic: Boolean = true,
    val isDarkMode: Boolean = true,
    val isProtectionEnabled: Boolean = true,
    val protectionMode: ProtectionMode = ProtectionMode.SMART,
    val remindersShownToday: Int = 4,
    val blockedAttemptsToday: Int = 2,
    val currentReminder: IslamicReminder = defaultReminders.first(),
    val domains: List<RestrictedDomain> = defaultDomains,
    val apps: List<RestrictedApp> = defaultApps,
    val challenges: List<DailyChallenge> = defaultChallenges,
    val showRescueDialog: Boolean = false
)

val defaultReminders = listOf(
    IslamicReminder(
        textAr = "﴿ أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ ﴾",
        textEn = "\"Does he not know that Allah sees?\"",
        sourceAr = "سورة العلق - آية 14",
        sourceEn = "Surah Al-Alaq, Verse 14"
    ),
    IslamicReminder(
        textAr = "﴿ قُل لِّلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ وَيَحْفَظُوا فُرُوجَهُمْ ۚ ذَٰلِكَ أَزْكَىٰ لَهُمْ ﴾",
        textEn = "\"Tell the believing men to lower their gaze and guard their modesty. That is purer for them.\"",
        sourceAr = "سورة النور - آية 30",
        sourceEn = "Surah An-Nur, Verse 30"
    ),
    IslamicReminder(
        textAr = "«مَن تَرَكَ شيئًا للهِ عوَّضَه اللهُ خيرًا منه»",
        textEn = "\"Whoever leaves something for the sake of Allah, Allah will replace it with something better.\"",
        sourceAr = "حديث صحيح - رواه أحمد",
        sourceEn = "Authentic Hadith - Musnad Ahmad"
    ),
    IslamicReminder(
        textAr = "﴿ وَمَا عِندَ اللَّهِ خَيْرٌ وَأَبْقَىٰ ﴾",
        textEn = "\"And what is with Allah is better and more lasting.\"",
        sourceAr = "سورة القصص - آية 60",
        sourceEn = "Surah Al-Qasas, Verse 60"
    )
)

val defaultDomains = listOf(
    RestrictedDomain(1, "adult-example.com", DomainCategory.ADULT, true),
    RestrictedDomain(2, "gambling-site.net", DomainCategory.INAPPROPRIATE, true),
    RestrictedDomain(3, "time-waster.org", DomainCategory.CUSTOM, false)
)

val defaultApps = listOf(
    RestrictedApp("com.android.chrome", "Google Chrome", false),
    RestrictedApp("com.zhiliaoapp.musically", "TikTok", true),
    RestrictedApp("com.instagram.android", "Instagram", false),
    RestrictedApp("com.twitter.android", "X (Twitter)", false)
)

val defaultChallenges = listOf(
    DailyChallenge(1, "تحدي غض البصر (7 أيام)", "7-Day Gaze Protection Challenge", 7, 3, true),
    DailyChallenge(2, "المحافظة على الصلوات الخمس", "Daily 5 Prayers Consistency", 30, 12, true),
    DailyChallenge(3, "أذكار الصباح والمساء", "Morning & Evening Adhkar", 14, 5, false),
    DailyChallenge(4, "ورد القرآن اليومي", "Daily Quran Reading", 30, 8, false)
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RaqeebUiState())
    val uiState: StateFlow<RaqeebUiState> = _uiState.asStateFlow()

    fun toggleProtection(enabled: Boolean) {
        _uiState.update { it.copy(isProtectionEnabled = enabled) }
    }

    fun setProtectionMode(mode: ProtectionMode) {
        _uiState.update { it.copy(protectionMode = mode) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun nextReminder() {
        _uiState.update { state ->
            val currentIndex = defaultReminders.indexOf(state.currentReminder)
            val nextIndex = (currentIndex + 1) % defaultReminders.size
            state.copy(currentReminder = defaultReminders[nextIndex])
        }
    }

    fun setShowRescueDialog(show: Boolean) {
        _uiState.update { state ->
            val updatedReminders = if (show) state.remindersShownToday + 1 else state.remindersShownToday
            state.copy(showRescueDialog = show, remindersShownToday = updatedReminders)
        }
    }

    fun addDomain(domain: String, category: DomainCategory) {
        val cleaned = domain.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("/")
        if (cleaned.isBlank() || !cleaned.contains(".")) return

        _uiState.update { state ->
            val newItem = RestrictedDomain(
                id = System.currentTimeMillis(),
                domain = cleaned,
                category = category,
                isEnabled = true
            )
            state.copy(domains = listOf(newItem) + state.domains)
        }
    }

    fun toggleDomain(id: Long) {
        _uiState.update { state ->
            state.copy(
                domains = state.domains.map {
                    if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
                }
            )
        }
    }

    fun removeDomain(id: Long) {
        _uiState.update { state ->
            state.copy(domains = state.domains.filterNot { it.id == id })
        }
    }

    fun toggleAppRestriction(packageName: String) {
        _uiState.update { state ->
            state.copy(
                apps = state.apps.map {
                    if (it.packageName == packageName) it.copy(isRestricted = !it.isRestricted) else it
                }
            )
        }
    }

    fun toggleChallengeToday(id: Int) {
        _uiState.update { state ->
            state.copy(
                challenges = state.challenges.map { ch ->
                    if (ch.id == id) {
                        val nowCompleted = !ch.isCompletedToday
                        val newDays = if (nowCompleted) (ch.completedDays + 1).coerceAtMost(ch.targetDays)
                        else (ch.completedDays - 1).coerceAtLeast(0)
                        ch.copy(isCompletedToday = nowCompleted, completedDays = newDays)
                    } else ch
                }
            )
        }
    }
}
