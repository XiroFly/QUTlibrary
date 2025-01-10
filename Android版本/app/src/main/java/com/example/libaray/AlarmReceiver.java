package com.example.libaray;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.example.libaray.utilize.Reserve;
import com.example.libaray.utilize.SigninOrSignout;
import com.example.libaray.utilize.EnumTurns;
import com.example.libaray.utilize.LogManager;
import com.example.libaray.utilize.TimerManager;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;
    private static final String TAG = "AlarmReceiver";
    private Reserve embodyReserve;
    private SigninOrSignout signinOrSignout;
    private static final String FILENAME = "usesettings.json";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (embodyReserve == null) {
            embodyReserve = new Reserve(context);
        }
        if (signinOrSignout == null) {
            signinOrSignout = new SigninOrSignout();
        }
        LogManager.init(context);
        sharedPreferences = context.getSharedPreferences("accountant", Context.MODE_PRIVATE);
        String type = intent.getStringExtra("type");
        if (type == null) {
            LogManager.write("Received intent without type");
            return;
        }

        LogManager.write("Received intent with type: " + type);

        // Handle different alarm types
        switch (EnumTurns.turns.valueOf(type)) {
            case morningReserve:
            case afternoonReserve:
            case eveningReserve:
                reserve(type);
                break;

            case morningCheckIn:
            case afternoonCheckIn:
            case eveningCheckIn:
                signinOrSignout.init(context);
                checkIn(type);
                break;

            case morningCheckOut:
            case afternoonCheckOut:
            case eveningCheckOut:
                signinOrSignout.init(context);
                checkOut(type);
                break;

            default:
                Log.e(TAG, "Unknown type received: " + type);
        }
        // 重新设置下次闹钟
        TimerManager timerManager = new TimerManager(context);
        Calendar nextAlarmTime = Calendar.getInstance();
        nextAlarmTime.add(Calendar.DAY_OF_YEAR, 1); // 设置为明天的同一时间
        timerManager.scheduleAlarm(nextAlarmTime, EnumTurns.turns.valueOf(type));
    }

    private void checkIn(String type) {
        //Log.d(TAG, "Performing check-in...");
        // Simulate check-in action
        // Network requests or database updates can be added here
        signinOrSignout.scanIn();
        changeTurn(1); // Indicate check-in completion
    }

    private void checkOut(String type) {
        //Log.d(TAG, "Performing check-out...");
        // Simulate check-out action
        signinOrSignout.scanOut();
        if (getAuxTurn() == 2) {
            //Log.d(TAG, "Auxiliary account is holding the reservation. Transferring...");
            cancelReserve(type);
            type=getType();
            reserve(type); // Re-reserve under main account
        }
        changeTurn(0); // Reset the turn state
    }

    public void reserve(String type) {
        //Log.d(TAG, "Performing reservation...");
        if (getAuxTurn() == 1) {
            //Log.d(TAG, "Reserving using auxiliary account...");
            reserveUsingAux(type);
            changeTurn(2); // Indicate auxiliary account has reserved
        } else {
            //Log.d(TAG, "Reserving using main account...");
            reserveMainAccount(type);
        }
    }

    private void cancelReserve(String type) {
        //Log.d(TAG, "Cancelling current reservation...");
        // 只涉及辅助账号的取消预约
        embodyReserve.deleteReserve(type,true );

    }

    private void reserveUsingAux(String type) {
        //Log.d(TAG, "Auxiliary account reservation...");
        // Implement auxiliary account reservation logic
        embodyReserve.reserve(type,true);
        setType(type);

    }

    private void reserveMainAccount(String type) {
        //Log.d(TAG, "Main account reservation...");
        // Implement main account reservation logic
        embodyReserve.reserve(type,false);
        
    }

    private int getAuxTurn() {
        return sharedPreferences.getInt(EnumTurns.AUX_TURN, 0);
    }
    private String getType(){
    return     sharedPreferences.getString("type", null);
    }
    private void setType(String type){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("type",type );
        editor.apply();
    }
    private void changeTurn(int turnState) {
        //Log.d(TAG, "Changing turn state to: " + turnState);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(EnumTurns.AUX_TURN, turnState);
        editor.apply();
    }
}
