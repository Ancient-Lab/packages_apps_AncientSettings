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
    private static final String PREF_STOCK_RECENTS_CATEGORY = "stock_recents_category";
    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";
    private static final String PREF_SWIPE_UP_ENABLED = "swipe_up_enabled_warning";

    private SystemSettingMasterSwitchPreference mSmartPixelsEnabled;
    private PreferenceCategory mStockRecentsCategory;
    private PreferenceCategory mAlternativeRecentsCategory;
    private Context mContext;

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

        mStockRecentsCategory = (PreferenceCategory) findPreference(PREF_STOCK_RECENTS_CATEGORY);
        mAlternativeRecentsCategory =
                (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

        // Alternative recents en-/disabling
        Preference.OnPreferenceChangeListener alternativeRecentsChangeListener =
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDependencies(preference, (Boolean) newValue);
                return true;
            }
        };
        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (preference instanceof SystemSettingMasterSwitchPreference) {
                preference.setOnPreferenceChangeListener(alternativeRecentsChangeListener);
            }
        }

        updateDependencies();

        // Warning for alternative recents when gesture navigation is enabled,
        // which directly controls quickstep (launcher) recents.
        final int navigationMode = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_navBarInteractionMode);
        // config_navBarInteractionMode:
        // 0: 3 button mode (supports slim recents)
        // 1: 2 button mode (currently does not support alternative recents)
        // 2: gesture only (currently does not support alternative recents)
        if (navigationMode != 0) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (PREF_SWIPE_UP_ENABLED.equals(preference.getKey())) {
                    // We want to have that one enabled
                    continue;
                }
                preference.setEnabled(false);
            }
        } else {
            mAlternativeRecentsCategory.removePreference(findPreference(PREF_SWIPE_UP_ENABLED));
        }
    }

    private void updateDependencies() {
        updateDependencies(null, null);
    }

    private void updateDependencies(Preference updatedPreference, Boolean newValue) {
        // Disable stock recents category if alternative enabled
        boolean alternativeRecentsEnabled = newValue != null && newValue;
        if (!alternativeRecentsEnabled) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (preference == updatedPreference) {
                    // Already used newValue
                    continue;
                }
                if (preference instanceof SystemSettingMasterSwitchPreference
                        && ((SystemSettingMasterSwitchPreference) preference).isChecked()) {
                    alternativeRecentsEnabled = true;
                    break;
                }
            }
        }
        if (mStockRecentsCategory != null) {
            mStockRecentsCategory.setEnabled(!alternativeRecentsEnabled);
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
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ANCIENT_SETTINGS;
    }
}
