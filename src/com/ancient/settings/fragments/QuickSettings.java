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
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import com.ancient.settings.preferences.SystemSettingSwitchPreference;
import com.ancient.settings.preferences.CustomSeekBarPreference;
import com.ancient.settings.preferences.SystemSettingEditTextPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class QuickSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "QuickSettings";

    private static final String CRAFT_FOOTER_TEXT_STRING = "craft_footer_text_string";
    private static final String QS_PANEL_COLOR = "qs_panel_color";
    static final int DEFAULT_QS_PANEL_COLOR = 0xffffffff;
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String PREF_SMART_PULLDOWN = "smart_pulldown";

    private ColorPickerPreference mQsPanelColor;
    private SystemSettingEditTextPreference mFooterString;
    private ListPreference mQuickPulldown;
    private ListPreference mSmartPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ancient_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mFooterString = (SystemSettingEditTextPreference) findPreference(CRAFT_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(getContentResolver(),
                CRAFT_FOOTER_TEXT_STRING);
        if (footerString != null && footerString != "")
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("CraftWithHeart");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.CRAFT_FOOTER_TEXT_STRING, "CraftWithHeart");
        }

        mQsPanelColor = (ColorPickerPreference) findPreference(QS_PANEL_COLOR);
        mQsPanelColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.QS_PANEL_BG_COLOR, DEFAULT_QS_PANEL_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQsPanelColor.setSummary(hexColor);
        mQsPanelColor.setNewPreviewColor(intColor);

        mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        updatePulldownSummary(quickPulldownValue);

        mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        int smartPulldown = Settings.System.getInt(resolver,
                Settings.System.QS_SMART_PULLDOWN, 0);
        mSmartPulldown.setValue(String.valueOf(smartPulldown));
        updateSmartPulldownSummary(smartPulldown);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    	ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != "" && value != null)
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CRAFT_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("CraftWithHeart");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CRAFT_FOOTER_TEXT_STRING, "CraftWithHeart");
            }
            return true;
        } else if (preference == mQsPanelColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.QS_PANEL_BG_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            updatePulldownSummary(quickPulldownValue);
            return true;
        } else if (preference == mSmartPulldown) {
            int smartPulldown = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN, smartPulldown);
            updateSmartPulldownSummary(smartPulldown);
            return true;
        }
        return false;
    }

    private void updatePulldownSummary(int value) {
        Resources res = getResources();
         if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else if (value == 3) {
            // quick pulldown always
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary_always));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_left
                    : R.string.quick_pulldown_right);
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }

    private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
        } else if (value == 3) {
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
        } else {
            String type = res.getString(value == 1
                    ? R.string.smart_pulldown_dismissable
                    : R.string.smart_pulldown_ongoing);
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ANCIENT_SETTINGS;
    }
}
