package com.android.settings.security.screenlock;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

import android.os.SystemProperties;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class KeyguardCameraPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String SYS_KEY_KEYGUARD_CAMERA = "persist.keyguard.camera";
    private static final String PREF_KEY_KEYGUARD_CAMERA = "keyguard_camera";

    private final int mUserId;
    private final LockPatternUtils mLockPatternUtils;

    public KeyguardCameraPreferenceController(Context context, int userId,
            LockPatternUtils lockPatternUtils) {
        super(context);
        mUserId = userId;
        mLockPatternUtils = new LockPatternUtils(context);
    }

    @Override
    public boolean isAvailable() {
        if (!mLockPatternUtils.isSecure(mUserId)) {
            return false;
        }
        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality(mUserId)) {
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

    @Override
    public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(
                SystemProperties.getBoolean(SYS_KEY_KEYGUARD_CAMERA, true));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final boolean mode = !((TwoStatePreference) preference).isChecked();
        SystemProperties.set(SYS_KEY_KEYGUARD_CAMERA, Boolean.toString(mode));
        return true;
    }
}
