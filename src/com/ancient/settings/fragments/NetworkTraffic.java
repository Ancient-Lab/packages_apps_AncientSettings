/*
 * Copyright (C) 2019 Rebellion-OS
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

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.ancient.settings.preferences.CustomSeekBarPreference;
import com.ancient.settings.preferences.SystemSettingSeekBarPreference;
import com.ancient.settings.preferences.SystemSettingSwitchPreference;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class NetworkTraffic extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String NETWORK_TRAFFIC_HIDEARROW = "network_traffic_hidearrow";
    private static final String NETWORK_TRAFFIC_LOCATION = "network_traffic_location";
    private static final String NETWORK_TRAFFIC_REFRESH_INTERVAL = "network_traffic_refresh_interval";

    private SystemSettingSeekBarPreference mThreshold;
    private SystemSettingSwitchPreference mNetMonitor;
    private SystemSettingSwitchPreference mHideArrows;
    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficType;
    private ListPreference mNetTrafficLayout;
    private SystemSettingSeekBarPreference mNetTrafficRefreshInterval;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.networktraffic);

        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        int value = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (SystemSettingSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(value);
        mThreshold.setOnPreferenceChangeListener(this);

        int nettype = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_TYPE, 3, UserHandle.USER_CURRENT);
        mNetTrafficType = (ListPreference) findPreference("network_traffic_type");
        mNetTrafficType.setValue(String.valueOf(nettype));
        mNetTrafficType.setSummary(mNetTrafficType.getEntry());
        mNetTrafficType.setOnPreferenceChangeListener(this);

        int netlayout = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_LAYOUT, 0, UserHandle.USER_CURRENT);
        mNetTrafficLayout = (ListPreference) findPreference("network_traffic_layout");
        mNetTrafficLayout.setValue(String.valueOf(netlayout));
        mNetTrafficLayout.setSummary(mNetTrafficLayout.getEntry());
        mNetTrafficLayout.setOnPreferenceChangeListener(this);

        mNetTrafficLocation = (ListPreference) findPreference(NETWORK_TRAFFIC_LOCATION);
        int location = Settings.System.getInt(resolver,
                Settings.System.NETWORK_TRAFFIC_LOCATION, 0);
        mNetTrafficLocation.setValue(String.valueOf(location));
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        mHideArrows = (SystemSettingSwitchPreference) findPreference(NETWORK_TRAFFIC_HIDEARROW);

        mNetTrafficRefreshInterval = (SystemSettingSeekBarPreference) findPreference(NETWORK_TRAFFIC_REFRESH_INTERVAL);
        int interval = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL, 2, UserHandle.USER_CURRENT);
        mNetTrafficRefreshInterval.setValue(interval);
        mNetTrafficRefreshInterval.setOnPreferenceChangeListener(this);

        updateTrafficLocation(location);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_LOCATION, location);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            updateTrafficLocation(location);
            return true;
        } else if (preference == mNetTrafficType) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_TYPE, val,
                    UserHandle.USER_CURRENT);
            int index = mNetTrafficType.findIndexOfValue((String) newValue);
            mNetTrafficType.setSummary(mNetTrafficType.getEntries()[index]);
            return true;
        } else if (preference == mNetTrafficLayout) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_LAYOUT, val,
                    UserHandle.USER_CURRENT);
            int index = mNetTrafficLayout.findIndexOfValue((String) newValue);
            mNetTrafficLayout.setSummary(mNetTrafficLayout.getEntries()[index]);
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNetTrafficRefreshInterval) {
            int interval = (Integer) newValue;
            Settings.System.putIntForUser(resolver,
                    Settings.System.NETWORK_TRAFFIC_REFRESH_INTERVAL, interval, UserHandle.USER_CURRENT);
            return true;
		}
        return false;
    }

    public void updateTrafficLocation(int location) {
        switch(location){ 
            case 0:
                mNetTrafficType.setEnabled(false);
                mNetTrafficLayout.setEnabled(false);
                mThreshold.setEnabled(false);
                mHideArrows.setEnabled(false);
                mNetTrafficRefreshInterval.setEnabled(false);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 0);
                break;
            case 1:
                mNetTrafficType.setEnabled(true);
                mNetTrafficLayout.setEnabled(true);
                mThreshold.setEnabled(true);
                mHideArrows.setEnabled(true);
                mNetTrafficRefreshInterval.setEnabled(true);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 0);
                break;
            case 2:
                mNetTrafficType.setEnabled(true);
                mNetTrafficLayout.setEnabled(true);
                mThreshold.setEnabled(true);
                mHideArrows.setEnabled(true);
                mNetTrafficRefreshInterval.setEnabled(true);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 1);
                break;
            default: 
                break;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ANCIENT_SETTINGS;
    }

}
