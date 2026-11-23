package com.raqeeb.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.raqeeb.app.domain.model.*
import com.raqeeb.app.service.RaqeebAccessibilityService
import com.raqeeb.app.service.RaqeebVpnService
import com.raqeeb.app.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PermissionStatus(
    val notificationsGranted: Boolean = false,
    val accessibilityGranted: Boolean = false,
    val usageStatsGranted: Boolean = false,
    val vpnActive: Boolean = false
)

data class RaqeebUiState(
    val isArabic: Boolean = true,
    val isDarkMode: Boolean = true,
    val isProtectionEnabled: Boolean = true,
    val protectionMode: ProtectionMode = ProtectionMode.SMART,
    val permissions: PermissionStatus = PermissionStatus(),
    val remindersShownToday: Int = 1,
    val blockedAttemptsToday: Int = 0,
    val currentReminder: IslamicReminder = defaultReminders.first(),
    val domains: List<RestrictedDomain> = builtInShieldEntries,
    val apps: List<RestrictedApp> = emptyList(),
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
        textAr = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
        textEn = "\"Unquestionably, by the remembrance of Allah hearts are assured.\"",
        sourceAr = "سورة الرعد - آية 28",
        sourceEn = "Surah Ar-Ra'd, Verse 28"
    )
)

val builtInShieldEntries = listOf(
    RestrictedDomain(1, "درع حفظ البصر الشامل (+45,000 نطاق غير لائق)", "", DomainCategory.SAFE_SHIELD, true, isBuiltInShield = true),
    RestrictedDomain(2, "درع الوقاية من المواقع الضارة والمشتتة", "", DomainCategory.HARMFUL, true, isBuiltInShield = true)
)

val defaultChallenges = listOf(
    DailyChallenge(1, "تحدي حفظ البصر (7 أيام)", "7-Day Gaze Protection Challenge", 7, 1, true),
    DailyChallenge(2, "المحافظة على الصلوات الخمس", "Daily 5 Prayers Consistency", 30, 1, true),
    DailyChallenge(3, "أذكار الصباح والمساء", "Morning & Evening Adhkar", 14, 0, false),
    DailyChallenge(4, "ورد القرآن اليومي", "Daily Quran Reading", 30, 0, false)
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RaqeebUiState())
    val uiState: StateFlow<RaqeebUiState> = _uiState.asStateFlow()

    fun refreshSystemState(context: Context, isVpnRunning: Boolean = RaqeebVpnService.isRunning) {
        val installed = if (_uiState.value.apps.isEmpty()) {
            PermissionHelper.getInstalledUserApps(context)
        } else {
            _uiState.value.apps
        }

        _uiState.update { state ->
            state.copy(
                apps = installed,
                permissions = PermissionStatus(
                    notificationsGranted = PermissionHelper.hasNotificationPermission(context),
                    accessibilityGranted = PermissionHelper.isAccessibilityEnabled(context),
                    usageStatsGranted = PermissionHelper.hasUsageStatsPermission(context),
                    vpnActive = isVpnRunning
                )
            )
        }
    }

    fun setVpnActive(active: Boolean) {
        _uiState.update { it.copy(permissions = it.permissions.copy(vpnActive = active)) }
    }

    fun toggleProtection(enabled: Boolean) {
        RaqeebAccessibilityService.isProtectionActive = enabled
        _uiState.update { it.copy(isProtectionEnabled = enabled) }
    }

    fun setProtectionMode(mode: ProtectionMode) {
        RaqeebAccessibilityService.isStrictMode = (mode == ProtectionMode.STRICT)
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

    fun addCustomDomain(rawDomain: String) {
        val cleaned = rawDomain.trim().lowercase()
            .removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore("/")
        if (cleaned.isBlank() || !cleaned.contains(".")) return

        val masked = if (cleaned.length > 5) {
            "${cleaned.take(2)}••••••.${cleaned.substringAfterLast('.')}"
        } else "••••••.${cleaned.substringAfterLast('.')}"

        RaqeebVpnService.customBlockedDomains.add(cleaned)

        _uiState.update { state ->
            val newItem = RestrictedDomain(
                id = System.currentTimeMillis(),
                maskedLabel = masked,
                rawDomain = cleaned,
                category = DomainCategory.CUSTOM,
                isEnabled = true,
                isBuiltInShield = false
            )
            state.copy(domains = state.domains + newItem)
        }
    }

    fun toggleDomain(id: Long) {
        _uiState.update { state ->
            val updated = state.domains.map {
                if (it.id == id) {
                    val nextState = !it.isEnabled
                    if (it.rawDomain.isNotBlank()) {
                        if (nextState) RaqeebVpnService.customBlockedDomains.add(it.rawDomain)
                        else RaqeebVpnService.customBlockedDomains.remove(it.rawDomain)
                    }
                    it.copy(isEnabled = nextState)
                } else it
            }
            state.copy(domains = updated)
        }
    }

    fun removeDomain(id: Long) {
        _uiState.update { state ->
            val target = state.domains.find { it.id == id }
            if (target != null && target.rawDomain.isNotBlank()) {
                RaqeebVpnService.customBlockedDomains.remove(target.rawDomain)
            }
            state.copy(domains = state.domains.filterNot { it.id == id && !it.isBuiltInShield })
        }
    }

    fun toggleAppRestriction(packageName: String) {
        _uiState.update { state ->
            val updated = state.apps.map {
                if (it.packageName == packageName) it.copy(isRestricted = !it.isRestricted) else it
            }
            RaqeebAccessibilityService.restrictedPackages.clear()
            RaqeebAccessibilityService.restrictedPackages.addAll(
                updated.filter { it.isRestricted }.map { it.packageName }
            )
            state.copy(apps = updated)
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
