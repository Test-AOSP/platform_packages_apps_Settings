package com.android.settings.security;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;

import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class KeyguardCameraPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, OnResume, Preference.OnPreferenceChangeListener {

    private static final String SYS_KEY_KEYGUARD_CAMERA = "persist.keyguard.camera";
    private static final String PREF_KEY_KEYGUARD_CAMERA = "keyguard_camera";
    private static final String PREF_KEY_SECURITY_CATEGORY = "security_category";

    private static final int MY_USER_ID = UserHandle.myUserId();

    private final LockPatternUtils mLockPatternUtils;

    private PreferenceCategory mSecurityCategory;
    private SwitchPreference mKeyguardCamera;

    public KeyguardCameraPreferenceController(Context context) {
        super(context);
        mLockPatternUtils = new LockPatternUtils(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSecurityCategory = screen.findPreference(PREF_KEY_SECURITY_CATEGORY);
        updatePreferenceState();
    }

    @Override
    public boolean isAvailable() {
        if (!mLockPatternUtils.isSecure(MY_USER_ID)) {
            return false;
        }
        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality(MY_USER_ID)) {
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY_KEYGUARD_CAMERA;
    }

    // TODO: should we use onCreatePreferences() instead?
    private void updatePreferenceState() {
        if (mSecurityCategory == null) {
            return;
        }
        mKeyguardCamera = (SwitchPreference) mSecurityCategory.findPreference(PREF_KEY_KEYGUARD_CAMERA);
        mKeyguardCamera.setChecked(SystemProperties.getBoolean(SYS_KEY_KEYGUARD_CAMERA, true));
    }

    @Override
    public void onResume() {
        updatePreferenceState();
        if (mKeyguardCamera != null) {
            boolean mode = mKeyguardCamera.isChecked();
            SystemProperties.set(SYS_KEY_KEYGUARD_CAMERA, Boolean.toString(mode));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        if (PREF_KEY_KEYGUARD_CAMERA.equals(key)) {
            final boolean mode = !mKeyguardCamera.isChecked();
            SystemProperties.set(SYS_KEY_KEYGUARD_CAMERA, Boolean.toString(mode));
        }
        return true;
    }
}
