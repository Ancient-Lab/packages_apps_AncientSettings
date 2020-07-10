/*
 * Copyright (C) 2019-2020 The Ancient OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancient.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class QsClockStylePreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String QS_CLOCK_STYLE = "qs_clock_style";

    private ListPreference mQsClockStyle;

    public QsClockStylePreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return QS_CLOCK_STYLE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mQsClockStyle = screen.findPreference(QS_CLOCK_STYLE);
        int qsClockStyle = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QS_CLOCK_STYLE, 0, UserHandle.USER_CURRENT);
        int valueIndex = mQsClockStyle.findIndexOfValue(String.valueOf(qsClockStyle));
        mQsClockStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mQsClockStyle.setSummary(mQsClockStyle.getEntry());
        mQsClockStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsClockStyle) {
            int qsClockStyleValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_CLOCK_STYLE, qsClockStyleValue, UserHandle.USER_CURRENT);
            mQsClockStyle.setSummary(mQsClockStyle.getEntries()[qsClockStyleValue]);
            return true;
        }
        return false;
    }
}
