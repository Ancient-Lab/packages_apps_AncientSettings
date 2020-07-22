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

public class QsBarheightStylePreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String QS_BARHEIGHT_STYLE = "qs_barheight_style";

    private ListPreference mQsBarheightStyle;

    public QsBarheightStylePreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return QS_BARHEIGHT_STYLE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mQsBarheightStyle = screen.findPreference(QS_BARHEIGHT_STYLE);
        int qsBarheightStyle = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QS_BARHEIGHT_STYLE, 0, UserHandle.USER_CURRENT);
        int valueIndex = mQsBarheightStyle.findIndexOfValue(String.valueOf(qsBarheightStyle));
        mQsBarheightStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mQsBarheightStyle.setSummary(mQsBarheightStyle.getEntry());
        mQsBarheightStyle.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsBarheightStyle) {
            int qsBarheightStyleValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_BARHEIGHT_STYLE, qsBarheightStyleValue, UserHandle.USER_CURRENT);
            mQsBarheightStyle.setSummary(mQsBarheightStyle.getEntries()[qsBarheightStyleValue]);
            return true;
        }
        return false;
    }
}
