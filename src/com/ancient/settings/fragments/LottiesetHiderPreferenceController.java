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

public class LottiesetHiderPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String LOTTIESET_HIDER = "lottieset_hider";

    private ListPreference mLottiesetHider;

    public setLottiesetHiderPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return LOTTIESET_HIDER;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mLottiesetHider = screen.findPreference(LOTTIESET_HIDER);
        int setLottiesetHider = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.LOTTIESET_HIDER, 0, UserHandle.USER_CURRENT);
        int valueIndex = mLottiesetHider.findIndexOfValue(String.valueOf(setLottiesetHider));
        mLottiesetHider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mLottiesetHider.setSummary(mLottiesetHider.getEntry());
        mLottiesetHider.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLottiesetHider) {
            int setLottiesetHiderValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.LOTTIESET_HIDER, setLottiesetHiderValue, UserHandle.USER_CURRENT);
            mLottiesetHider.setSummary(mLottiesetHider.getEntries()[setLottiesetHiderValue]);
            return true;
        }
        return false;
    }
}
