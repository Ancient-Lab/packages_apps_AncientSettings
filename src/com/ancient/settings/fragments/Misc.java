/*
 * Copyright (C) 2018 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ancient.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.ancient.settings.preferences.SystemSettingMasterSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Misc extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Misc";

    private static final String SMART_PIXELS_ENABLED = "smart_pixels_enable";
    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";
    private static final String PREF_SWIPE_UP_ENABLED = "swipe_up_enabled_warning";
    private static final String PREF_USE_SLIM_RECENTS = "use_slim_recents";

    private SystemSettingMasterSwitchPreference mSmartPixelsEnabled;
    private SystemSettingMasterSwitchPreference mEnableSlimRecent;
    private PreferenceCategory mAlternativeRecentsCategory;
    private Preference mSwipeUpEnabledWarning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ancient_settings_misc);

        mSmartPixelsEnabled = (SystemSettingMasterSwitchPreference) findPreference(SMART_PIXELS_ENABLED);
        mSmartPixelsEnabled.setOnPreferenceChangeListener(this);
        int smartPixelsEnabled = Settings.System.getInt(getContentResolver(),
                SMART_PIXELS_ENABLED, 0);
        mSmartPixelsEnabled.setChecked(smartPixelsEnabled != 0);

        if (!getResources().getBoolean(com.android.internal.R.bool.config_enableSmartPixels)) {
            getPreferenceScreen().removePreference(mSmartPixelsEnabled);
        }

        // Alternative recents en-/disabling
        mAlternativeRecentsCategory = (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);
        mSwipeUpEnabledWarning = (Preference) findPreference(PREF_SWIPE_UP_ENABLED);
        mEnableSlimRecent = (SystemSettingMasterSwitchPreference) findPreference(PREF_USE_SLIM_RECENTS);
        mEnableSlimRecent.setOnPreferenceChangeListener(this);

        updatePreferences();
        updateDependencies();
    }

    private void updateDependencies() {
        // Warning for alternative recents when gesture navigation is enabled,
        // which directly controls quickstep (launcher) recents.
        final int navigationMode = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
        // config_navBarInteractionMode:
        // 0: 3 button mode (supports slim recents)
        // 1: 2 button mode (currently does not support alternative recents)
        // 2: gesture only (currently does not support alternative recents)
        if (navigationMode != 0) {
            mEnableSlimRecent.setEnabled(false);
        } else {
            mAlternativeRecentsCategory.removePreference(mSwipeUpEnabledWarning);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSmartPixelsEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            SMART_PIXELS_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mEnableSlimRecent) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.USE_SLIM_RECENTS, value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private void updatePreferences() {
        int useSlimRecent = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.USE_SLIM_RECENTS, 0, UserHandle.USER_CURRENT);
        mEnableSlimRecent.setChecked(useSlimRecent != 0);
    }

    @Override
    public void onPause() {
        super.onPause();

        updatePreferences();
        updateDependencies();
    }

    @Override
    public void onResume() {
        super.onResume();

        updatePreferences();
        updateDependencies();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ANCIENT_SETTINGS;
    }
}
