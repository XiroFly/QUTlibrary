package com.example.libaray.utilize;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.libaray.AlarmReceiver;
import com.example.libaray.utilize.EnumTurns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Calendar;

public class TimerManager {

    private Context context;
    private boolean isTimersEnabled;

    public TimerManager(Context context) {
        this.context = context;
        this.isTimersEnabled = true;  // 默认为开启状态
    }

    // 打开定时器
    public void enableTimers() {
        isTimersEnabled = true;
        scheduleTimers();
    }

    // 关闭定时器
    public void disableTimers() {
        isTimersEnabled = false;
        cancelTimers();  // 取消所有定时任务
    }

    // 设置定时任务
    public boolean scheduleTimers() {
        if (!isTimersEnabled) {
            //Log.d("TimerManager", "Timers are disabled. No actions will be taken.");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ! ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms()) {
            // 请求权限
            Intent permissionIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(permissionIntent);
            return false;
        }

        try {
            JSONArray settings = loadUseSettingsJson();
            for (int i = 0; i < settings.length(); i++) {
                JSONObject setting = settings.getJSONObject(i);

                String startTime = setting.getString("start");
                String endTime = setting.getString("end");
                boolean checkbox1 = setting.getBoolean("checkbox1");
                boolean checkbox2 = setting.getBoolean("checkbox2");
                boolean checkbox3 = setting.getBoolean("checkbox3");

                if (checkbox1&&i==0) { // 早上
                    scheduleAlarmAt6_10(EnumTurns.turns.morningReserve); // 上午预约
                    scheduleAlarmAtStartTime(startTime, EnumTurns.turns.morningCheckIn, false); // 上午签到
                    scheduleAlarmAtStartTime(endTime, EnumTurns.turns.morningCheckOut, false); // 上午签退
                }

                if (checkbox2&&i==1) { // 下午
                    scheduleAlarmAtStartTime(startTime, EnumTurns.turns.afternoonReserve, true); // 下午预约
                    scheduleAlarmAtStartTime(startTime, EnumTurns.turns.afternoonCheckIn, false); // 下午签到
                    scheduleAlarmAtStartTime(endTime, EnumTurns.turns.afternoonCheckOut, false); // 下午签退
                }

                if (checkbox3&&i==2) { // 晚上
                    scheduleAlarmAtStartTime(startTime, EnumTurns.turns.eveningReserve, true); // 晚上预约
                    scheduleAlarmAtStartTime(startTime, EnumTurns.turns.eveningCheckIn, false); // 晚上签到
                    scheduleAlarmAtStartTime(endTime, EnumTurns.turns.eveningCheckOut, false); // 晚上签退
                }
            }
            return true;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    // 从用户文件读取JSON
    private JSONArray loadUseSettingsJson() throws IOException, JSONException {
        // 假设文件路径是 "files/usesettings.json"
        FileInputStream fis = context.openFileInput("usesettings.json");
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(isr);

        StringBuilder jsonContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonContent.append(line);
        }

        reader.close();
        isr.close();
        fis.close();

        return new JSONArray(jsonContent.toString());
    }

    // 在6:10执行上午的预约
    private void scheduleAlarmAt6_10(EnumTurns.turns type) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);

        scheduleAlarm(calendar, type);
    }

    // 设置定时任务，指定start前5小时55分钟
    private void scheduleAlarmAtStartTime(String time, EnumTurns.turns type, boolean isReserve) {
        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        // 设置闹钟触发时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (isReserve) {
            // 预约提前5小时55分钟
            calendar.add(Calendar.HOUR_OF_DAY, -5);
            calendar.add(Calendar.MINUTE, -55);
        }

        scheduleAlarm(calendar, type);
    }

    // 通用的设置闹钟方法
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleAlarm(Calendar calendar, EnumTurns.turns type) {
        // 获取当前时间
        Calendar now = Calendar.getInstance();

        // 如果预定时间已经过去，则将闹钟时间推迟到第二天
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            //Log.d("TimerManager", "Alarm time has passed. Rescheduling for the next day.");
        }
        // 创建一个PendingIntent来触发BroadcastReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("type", type.toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, type.ordinal(), intent, PendingIntent.FLAG_MUTABLE );

        // 获取AlarmManager并设置闹钟
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // only once
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),  pendingIntent);
            //Log.d("TimerManager", "Repeating alarm scheduled for: " + calendar.getTime() + " with type: " + type);
        }
    }

    // 取消所有定时任务
    public void cancelTimers() {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            for (EnumTurns.turns type : EnumTurns.turns.values()) {
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("type", type.toString());//复用intent
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, type.ordinal(), intent, PendingIntent.FLAG_MUTABLE);
                alarmManager.cancel(pendingIntent);
               // Log.e("TimerManager", "Cancelled alarm for type: " + type);
            }
        }
    }
}
