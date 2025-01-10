package com.example.libaray.utilize;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {

    private static final String PREF_NAME = "ReservationPrefs";
    private static final String KEY_RESV_ID = "resvId";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_UNION_ID = "unionId";
    private static final String KEY_DEV_ID = "devId";

    private SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveReservationDetails(String resvId, String uuid, String unionId, String devId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_RESV_ID, resvId);
        editor.putString(KEY_UUID, uuid);
        editor.putString(KEY_UNION_ID, unionId);
        editor.putString(KEY_DEV_ID, devId);
        editor.apply();
    }
    public void saveUuid(String uuid){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_UUID, uuid);
        editor.apply();
    }

    public String getResvId() {
        return sharedPreferences.getString(KEY_RESV_ID, null);
    }

    public String getUuid() {
        return sharedPreferences.getString(KEY_UUID, null);
    }

    public String getUnionId() {
        return sharedPreferences.getString(KEY_UNION_ID, null);
    }

    public String getDevId() {
        return sharedPreferences.getString(KEY_DEV_ID, null);
    }

    public void clearReservationDetails() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_RESV_ID);
        editor.remove(KEY_UUID);
        editor.remove(KEY_UNION_ID);
        editor.remove(KEY_DEV_ID);
        editor.apply();
    }
}
