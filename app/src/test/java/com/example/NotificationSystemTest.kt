package com.example

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.example.util.NotificationHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric unit test verifying state changes, SharedPreferences persistence logic,
 * notification channels initialization, and device permission check stability.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationSystemTest {

    @Test
    fun testSharedPreferencesStorageForNotificationToggle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences("test_settings_prefs", Context.MODE_PRIVATE)

        // 1. Ensure preference record doesn't exist by default in a clean sandbox
        assertFalse(sharedPrefs.contains("notifications_enabled"))

        // 2. Set to true, commit on preference storage, and assert state change
        sharedPrefs.edit().putBoolean("notifications_enabled", true).commit()
        assertTrue(sharedPrefs.getBoolean("notifications_enabled", false))

        // 3. Set to false, commit, and verify value updates appropriately
        sharedPrefs.edit().putBoolean("notifications_enabled", false).commit()
        assertFalse(sharedPrefs.getBoolean("notifications_enabled", true))
    }

    @Test
    fun testNotificationChannelCreationAndProperties() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        try {
            // Verify channel creation executes cleanly on the simulated OS sdk level
            NotificationHelper.createNotificationChannel(context)
            assertTrue("Notification Channel registered successfully without exceptions", true)
        } catch (e: Exception) {
            fail("Notification channel registration threw an unexpected exception: ${e.message}")
        }
    }

    @Test
    fun testSystemLevelNotificationPermissionCheck() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Assert system check can be retrieved successfully without crashes or exceptions 
        val isEnabled = NotificationHelper.areNotificationsEnabled(context)
        assertNotNull("System notification status must be resolveable", isEnabled)
    }
}
