package com.example.libaray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libaray.utilize.TimerManager;


public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "TimerPrefs";
    private static final String PREFS_KEY_ENABLED = "isTimerEnabled";
    private TimerManager timerManager;

    // 声明按钮变量
    private Button toggleTimerButton, btnSettings,usebtnSettings;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerManager = new TimerManager(this);
        // 获取按钮引用
        toggleTimerButton = findViewById(R.id.toggleTimerButton);
        toggleTimerButton.setText(isTimerEnabled() ? "停止定时" : "开启定时");

        btnSettings = findViewById(R.id.btnSettings);
        usebtnSettings=findViewById(R.id.usebtnSettings);

        // 设置按钮点击事件监听器
        toggleTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 添加
                if (isTimerEnabled()) {
                    disableTimer();
                    toggleTimerButton.setText("开启定时");
                    Toast.makeText(MainActivity.this, "定时任务已停止", Toast.LENGTH_SHORT).show();
                } else {
                    if(enableTimer()){
                    toggleTimerButton.setText("停止定时");
                    Toast.makeText(MainActivity.this, "定时任务已开启", Toast.LENGTH_SHORT).show();
                }
                }
            }
        });


        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 添加 基本参数设置 按钮的功能
                // 启动 SettingsActivity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        usebtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 添加 使用参数设置 按钮的功能
                // 启动 UseSettingsActivity
                Intent intent = new Intent(MainActivity.this, UseSettingsActivity.class);
                startActivity(intent);
            }
        });



    }
    private boolean isTimerEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFS_KEY_ENABLED, false);

    }

    private boolean enableTimer() {

       if( timerManager.scheduleTimers()){
           SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
           prefs.edit().putBoolean(PREFS_KEY_ENABLED,true).apply();
       return true;
       }
       return false;


    }

    private void disableTimer() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_KEY_ENABLED, false).apply();
        timerManager.cancelTimers();
    }

}
