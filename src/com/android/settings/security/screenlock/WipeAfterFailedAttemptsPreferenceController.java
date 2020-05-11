package com.android.settings.security.screenlock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.Preference;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.trustagent.TrustAgentManager;
import com.android.settingslib.core.AbstractPreferenceController;

public class WipeAfterFailedAttemptsPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_WIPE_AFTER_FAILED_ATTEMPTS = "wipe_after_failed_attempts";

    private final int mUserId;
    private final LockPatternUtils mLockPatternUtils;
    private final TrustAgentManager mTrustAgentManager;

    public WipeAfterFailedAttemptsPreferenceController(Context context, int userId,
            LockPatternUtils lockPatternUtils) {
        super(context);
        mUserId = userId;
        mLockPatternUtils = lockPatternUtils;
        mTrustAgentManager = FeatureFactory.getFactory(context)
                .getSecurityFeatureProvider().getTrustAgentManager();
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
        return KEY_WIPE_AFTER_FAILED_ATTEMPTS;
    }

    @Override
    public void updateState(Preference preference) {
        setupWipeAfterPreference((RestrictedListPreference) preference);
        updateWipeAfterPreferenceSummary((RestrictedListPreference) preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            final int attempts = Integer.parseInt((String) newValue);
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_WIPE_AFTER_FAILED_ATTEMPTS, attempts);
            updateState(preference);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist wipeAfter failed attempts setting", e);
        }
        return true;
    }

    private void setupWipeAfterPreference(RestrictedListPreference preference) {
        // Compatible with pre-Froyo
        int currentAttempts = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.LOCK_SCREEN_WIPE_AFTER_FAILED_ATTEMPTS, 0);
        preference.setValue(String.valueOf(currentAttempts));
    }

    private void updateWipeAfterPreferenceSummary(RestrictedListPreference preference) {
        final CharSequence summary;
        if (preference.isDisabledByAdmin()) {
            summary = mContext.getText(R.string.disabled_by_policy_title);
        } else {
            // Update summary message with current value
            int currentAttempts = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_WIPE_AFTER_FAILED_ATTEMPTS, 0);
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            int best = 0;
            for (int i = 0; i < values.length; i++) {
                int attempts = Integer.valueOf(values[i].toString());
                if (currentAttempts >= attempts) {
                    best = i;
                }
            }

            final CharSequence trustAgentLabel = mTrustAgentManager
                    .getActiveTrustAgentLabel(mContext, mLockPatternUtils);
            if (!TextUtils.isEmpty(trustAgentLabel)) {
                if (Integer.valueOf(values[best].toString()) == 0) {
                    summary = mContext.getString(R.string.wipe_disabled_summary_with_exception,
                            trustAgentLabel);
                } else {
                    summary = mContext.getString(R.string.wipe_after_failed_attempts_summary_with_exception,
                            entries[best], trustAgentLabel);
                }
            } else {
                if (Integer.valueOf(values[best].toString()) == 0) {
                    summary = mContext.getString(R.string.wipe_disabled_summary);
                } else {
                    summary = mContext.getString(R.string.lock_after_timeout_summary, entries[best]);
                }
            }
        }
        preference.setSummary(summary);
    }
}
