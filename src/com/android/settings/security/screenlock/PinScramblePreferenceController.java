package com.android.settings.security.screenlock;

import static android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
import static android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX;

import android.content.Context;

import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class PinScramblePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_SCRAMBLE_PIN_LAYOUT = "scramble_pin_layout";

    private final int mUserId;
    private final LockPatternUtils mLockPatternUtils;

    public PinScramblePreferenceController(Context context, int userId,
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
            case PASSWORD_QUALITY_NUMERIC:
            case PASSWORD_QUALITY_NUMERIC_COMPLEX:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SCRAMBLE_PIN_LAYOUT;
    }

    @Override
    public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.SCRAMBLE_PIN_LAYOUT, 0) != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final boolean mode = !((TwoStatePreference) preference).isChecked();
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.SCRAMBLE_PIN_LAYOUT, (mode) ? 1 : 0);
        return true;
    }
}
