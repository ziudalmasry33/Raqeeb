package com.raqeeb.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raqeeb.app.data.SupabaseSyncClient
import com.raqeeb.app.domain.model.*
import com.raqeeb.app.service.RaqeebAccessibilityService
import com.raqeeb.app.service.RaqeebVpnService
import com.raqeeb.app.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val currentReminder: IslamicReminder = defaultReminders.first(),
    val remindersList: List<IslamicReminder> = defaultReminders,
    val latestAdminMessage: AdminBroadcast? = null,
    val domains: List<RestrictedDomain> = builtInShieldEntries,
    val apps: List<RestrictedApp> = emptyList(),
    val isLoadingApps: Boolean = false,
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
    ),
    IslamicReminder(
        textAr = "﴿ وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ﴾",
        textEn = "\"And whoever fears Allah - He will make for him a way out.\"",
        sourceAr = "سورة الطلاق - آية 2",
        sourceEn = "Surah At-Talaq, Verse 2"
    )
)

val builtInShieldEntries = listOf(
    RestrictedDomain(1, "درع حفظ البصر الشامل (+45,000 نطاق غير لائق)", "", DomainCategory.SAFE_SHIELD, true, isBuiltInShield = true),
    RestrictedDomain(2, "درع الوقاية من المواقع الضارة والمشتتة", "", DomainCategory.HARMFUL, true, isBuiltInShield = true)
)

val defaultChallenges = listOf(
    DailyChallenge(1, "تحدي حفظ البصر", "Gaze Protection Streak", "تزكية النفس", "Self Purification", 7, 3, false, listOf(true, true, true, false, false, false, false)),
    DailyChallenge(2, "الصلوات الخمس في وقتها", "Five Daily Prayers on Time", "العبادات", "Worship", 30, 12, false, listOf(true, true, true, true, false, false, false)),
    DailyChallenge(3, "أذكار الصباح والمساء", "Morning & Evening Adhkar", "الذكر اليومي", "Daily Dhikr", 14, 5, false, listOf(true, true, false, false, false, false, false)),
    DailyChallenge(4, "ورد القرآن الكريم", "Daily Quran Portion", "القرآن", "Quran", 30, 8, false, listOf(true, true, true, false, false, false, false))
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RaqeebUiState())
    val uiState: StateFlow<RaqeebUiState> = _uiState.asStateFlow()
    private var pollingStarted = false

    fun initializeApp(context: Context) {
        val appContext = context.applicationContext
        refreshPermissionsOnly(appContext)

        if (_uiState.value.apps.isEmpty() && !_uiState.value.isLoadingApps) {
            _uiState.update { it.copy(isLoadingApps = true) }
            viewModelScope.launch {
                val installed = withContext(Dispatchers.IO) {
                    PermissionHelper.getInstalledUserApps(appContext)
                }
                _uiState.update { it.copy(apps = installed, isLoadingApps = false) }
            }
        }

        if (!pollingStarted) {
            pollingStarted = true
            // جلب الرسائل الفورية والتذكيرات من الخلفية بشكل دوري صامت
            viewModelScope.launch {
                while (true) {
                    syncFromCloudSilently(appContext)
                    delay(8_000L)
                }
            }
        }
    }

    fun refreshPermissionsOnly(context: Context, isVpnRunning: Boolean = RaqeebVpnService.isRunning) {
        _uiState.update { state ->
            state.copy(
                permissions = PermissionStatus(
                    notificationsGranted = PermissionHelper.hasNotificationPermission(context),
                    accessibilityGranted = PermissionHelper.isAccessibilityEnabled(context),
                    usageStatsGranted = PermissionHelper.hasUsageStatsPermission(context),
                    vpnActive = isVpnRunning
                )
            )
        }
    }

    private suspend fun syncFromCloudSilently(context: Context) {
        val msg = SupabaseSyncClient.fetchLatestAdminMessage()
        if (msg != null && msg.body.isNotBlank()) {
            _uiState.update { it.copy(latestAdminMessage = msg) }
            val prefs = context.getSharedPreferences("raqeeb_prefs", Context.MODE_PRIVATE)
            val lastSeenId = prefs.getLong("last_admin_msg_id", -1L)
            if (msg.id > lastSeenId) {
                prefs.edit().putLong("last_admin_msg_id", msg.id).apply()
                PermissionHelper.sendReminderNotification(context, msg.title, msg.body)
            }
        }

        val remoteReminders = SupabaseSyncClient.fetchPublishedReminders()
        if (remoteReminders.isNotEmpty()) {
            val combined = remoteReminders + defaultReminders
            _uiState.update { it.copy(remindersList = combined) }
        }

        val remoteDomains = SupabaseSyncClient.fetchRemoteBlockedDomains()
        if (remoteDomains.isNotEmpty()) {
            RaqeebVpnService.customBlockedDomains.addAll(remoteDomains)
        }
    }

    fun dismissAdminMessage() {
        _uiState.update { it.copy(latestAdminMessage = null) }
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
            val list = state.remindersList
            if (list.isEmpty()) return@update state
            val currentIndex = list.indexOf(state.currentReminder)
            val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % list.size
            state.copy(currentReminder = list[nextIndex])
        }
    }

    fun setShowRescueDialog(show: Boolean) {
        _uiState.update { it.copy(showRescueDialog = show) }
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

    fun completeChallengeToday(id: Int) {
        _uiState.update { state ->
            state.copy(
                challenges = state.challenges.map { ch ->
                    if (ch.id == id && !ch.isCompletedToday) {
                        val updatedWeek = ch.weekHistory.drop(1) + true
                        ch.copy(
                            isCompletedToday = true,
                            completedDays = (ch.completedDays + 1).coerceAtMost(ch.targetDays),
                            weekHistory = updatedWeek
                        )
                    } else ch
                }
            )
        }
    }

    fun addCustomChallenge(title: String, targetDays: Int) {
        if (title.isBlank()) return
        _uiState.update { state ->
            val newCh = DailyChallenge(
                id = (state.challenges.maxOfOrNull { it.id } ?: 0) + 1,
                titleAr = title.trim(),
                titleEn = title.trim(),
                categoryAr = "هدف شخصي",
                categoryEn = "Personal Goal",
                targetDays = targetDays.coerceIn(1, 90),
                completedDays = 0,
                isCompletedToday = false,
                weekHistory = listOf(false, false, false, false, false, false, false)
            )
            state.copy(challenges = state.challenges + newCh)
        }
    }
}
