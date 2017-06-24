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

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.*;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto; 
import com.ancient.settings.preferences.SystemSettingMasterSwitchPreference;
import com.ancient.settings.utils.SuShell;
import com.ancient.settings.utils.SuTask;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class Misc extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String TAG = "Misc";

    private static final String SMART_PIXELS_ENABLED = "smart_pixels_enable";
    private static final String SELINUX_CATEGORY = "selinux";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    private SystemSettingMasterSwitchPreference mSmartPixelsEnabled;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

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
        // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);

        mSelinuxPersistence =
            (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
            .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
            .contains(PREF_SELINUX_MODE));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mSmartPixelsEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            SMART_PIXELS_ENABLED, value ? 1 : 0);
            return true;
        } else if (preference == mSelinuxMode) {
            boolean enabled = (Boolean) newValue;
            new SwitchSelinuxTask(getActivity()).execute(enabled);
            setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
            return true;
        } else if (preference == mSelinuxPersistence) {
            setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) newValue);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ANCIENT_SETTINGS;
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
      SharedPreferences.Editor editor = getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
      if (persistent) {
        editor.putBoolean(PREF_SELINUX_MODE, status);
      } else {
        editor.remove(PREF_SELINUX_MODE);
      }
      editor.apply();
      mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
      public SwitchSelinuxTask(Context context) {
        super(context);
      }

      @Override
      protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
        if (params.length != 1) {
          Log.e(TAG, "SwitchSelinuxTask: invalid params count");
          return;
        }
        if (params[0]) {
          SuShell.runWithSuCheck("setenforce 1");
        } else {
          SuShell.runWithSuCheck("setenforce 0");
        }
      }

      @Override
      protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (!result) {
          // Did not work, so restore actual value
          setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
        }
      }
    }
}
