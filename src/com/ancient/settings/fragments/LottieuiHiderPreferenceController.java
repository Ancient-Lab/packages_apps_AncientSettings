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

public class LottieuiHiderPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String LOTTIEUI_HIDER = "lottieui_hider";

    private ListPreference mLottieuiHider;

    public uiLottieuiHiderPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return LOTTIEUI_HIDER;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mLottieuiHider = screen.findPreference(LOTTIEUI_HIDER);
        int uiLottieuiHider = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.LOTTIEUI_HIDER, 0, UserHandle.USER_CURRENT);
        int valueIndex = mLottieuiHider.findIndexOfValue(String.valueOf(uiLottieuiHider));
        mLottieuiHider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mLottieuiHider.setSummary(mLottieuiHider.getEntry());
        mLottieuiHider.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLottieuiHider) {
            int uiLottieuiHiderValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.LOTTIEUI_HIDER, uiLottieuiHiderValue, UserHandle.USER_CURRENT);
            mLottieuiHider.setSummary(mLottieuiHider.getEntries()[uiLottieuiHiderValue]);
            return true;
        }
        return false;
    }
}
