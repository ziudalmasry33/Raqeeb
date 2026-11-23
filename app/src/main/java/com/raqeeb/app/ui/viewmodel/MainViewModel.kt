package com.raqeeb.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raqeeb.app.data.SupabaseSyncClient
import com.raqeeb.app.data.TrustedAzkarLibrary
import com.raqeeb.app.domain.model.*
import com.raqeeb.app.service.RaqeebAccessibilityService
import com.raqeeb.app.service.RaqeebKeepAliveService
import com.raqeeb.app.service.RaqeebVpnService
import com.raqeeb.app.util.PermissionHelper
import com.raqeeb.app.widget.DailyAzkarReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val currentReminder: IslamicReminder = TrustedAzkarLibrary.allReminders.first(),
    val remindersList: List<IslamicReminder> = TrustedAzkarLibrary.allReminders,
    val latestAdminMessage: AdminBroadcast? = null,
    val domains: List<RestrictedDomain> = builtInShieldEntries,
    val challenges: List<DailyChallenge> = emptyList(),
    val showRescueDialog: Boolean = false
)

val builtInShieldEntries = listOf(
    RestrictedDomain(1, "درع حفظ البصر الشامل (+45,000 نطاق غير لائق)", "", DomainCategory.SAFE_SHIELD, true, isBuiltInShield = true),
    RestrictedDomain(2, "درع الوقاية من المواقع الضارة والمشتتة", "", DomainCategory.HARMFUL, true, isBuiltInShield = true)
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RaqeebUiState())
    val uiState: StateFlow<RaqeebUiState> = _uiState.asStateFlow()
    private var pollingStarted = false
    private var appCtx: Context? = null

    private fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun last7DateKeys(): List<String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        return (0..6).map {
            val d = fmt.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            d
        }
    }

    private fun loadChallengesFromLocalCache(context: Context): List<DailyChallenge> {
        val prefs = context.getSharedPreferences("raqeeb_challenges_cache", Context.MODE_PRIVATE)
        val today = todayDateKey()
        val weekDates = last7DateKeys()

        val templates = listOf(
            Triple(1, "تحدي حفظ البصر|Gaze Protection Streak", "تزكية النفس|Self Purification|7"),
            Triple(2, "الصلوات الخمس في وقتها|Five Daily Prayers", "العبادات|Worship|30"),
            Triple(3, "أذكار الصباح والمساء|Morning & Evening Adhkar", "الذكر اليومي|Daily Dhikr|14"),
            Triple(4, "الورد اليومي من القرآن|Daily Quran Portion", "القرآن|Quran|30")
        )

        return templates.map { (id, titles, meta) ->
            val tParts = titles.split("|")
            val mParts = meta.split("|")
            val completedDates = prefs.getStringSet("ch_dates_$id", emptySet()) ?: emptySet()
            val weekHistory = weekDates.map { dateStr -> completedDates.contains(dateStr) }
            DailyChallenge(
                id = id,
                titleAr = tParts[0],
                titleEn = tParts[1],
                categoryAr = mParts[0],
                categoryEn = mParts[1],
                targetDays = mParts[2].toInt(),
                completedDays = completedDates.size,
                isCompletedToday = completedDates.contains(today),
                weekHistory = weekHistory
            )
        }
    }

    fun initializeApp(context: Context) {
        val appContext = context.applicationContext
        this.appCtx = appContext
        refreshPermissionsOnly(appContext)
        DailyAzkarReceiver.scheduleDailyAlarms(appContext)
        RaqeebKeepAliveService.start(appContext)

        _uiState.update { it.copy(challenges = loadChallengesFromLocalCache(appContext)) }

        if (!pollingStarted) {
            pollingStarted = true
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
            _uiState.update { it.copy(remindersList = remoteReminders + TrustedAzkarLibrary.allReminders) }
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
            val idx = list.indexOf(state.currentReminder)
            val nextIdx = if (idx == -1) 0 else (idx + 1) % list.size
            state.copy(currentReminder = list[nextIdx])
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
            state.copy(domains = state.domains.map {
                if (it.id == id) {
                    val next = !it.isEnabled
                    if (it.rawDomain.isNotBlank()) {
                        if (next) RaqeebVpnService.customBlockedDomains.add(it.rawDomain)
                        else RaqeebVpnService.customBlockedDomains.remove(it.rawDomain)
                    }
                    it.copy(isEnabled = next)
                } else it
            })
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

    fun completeChallengeToday(id: Int) {
        val ctx = appCtx ?: return
        val prefs = ctx.getSharedPreferences("raqeeb_challenges_cache", Context.MODE_PRIVATE)
        val today = todayDateKey()
        val existing = (prefs.getStringSet("ch_dates_$id", emptySet()) ?: emptySet()).toMutableSet()
        if (!existing.contains(today)) {
            existing.add(today)
            prefs.edit().putStringSet("ch_dates_$id", existing).apply()
            _uiState.update { it.copy(challenges = loadChallengesFromLocalCache(ctx)) }
        }
    }

    fun addCustomChallenge(title: String, targetDays: Int) {
        if (title.isBlank()) return
        _uiState.update { state ->
            val newId = (state.challenges.maxOfOrNull { it.id } ?: 0) + 1
            state.copy(
                challenges = state.challenges + DailyChallenge(
                    id = newId,
                    titleAr = title.trim(),
                    titleEn = title.trim(),
                    categoryAr = "هدف شخصي",
                    categoryEn = "Personal Goal",
                    targetDays = targetDays.coerceIn(1, 90),
                    completedDays = 0,
                    isCompletedToday = false,
                    weekHistory = List(7) { false }
                )
            )
        }
    }
}
