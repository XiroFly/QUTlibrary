package com.example.libaray.utilize;

import android.content.Context;
import android.annotation.SuppressLint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

    private static File logFile;

    /**
     * 初始化日志文件路径
     *
     * @param context 上下文对象，用于获取外部存储路径
     */
    public static void init(Context context) {
        // 获取外部存储目录
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            logFile = new File(externalDir, "app_logs.txt");
        } else {
            System.err.println("外部存储不可用");
        }
    }

    /**
     * 写日志方法
     *
     * @param time    时间字符串，格式如 "yyyy-MM-dd HH:mm:ss"
     * @param message 日志内容
     */
    public static void write(String time, String message) {
        if (logFile == null) {
            System.err.println("日志文件未初始化");
            return;
        }

        String logEntry = String.format("[%s] %s", time, message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("写入日志失败: " + e.getMessage());
        }
    }

    /**
     * 自动获取当前时间并写入日志/storage/emulated/0/Android/data/com.example.library/files/app_logs.txt
     *
     * @param message 日志内容
     */
    public static void write(String message) {
        @SuppressLint("SimpleDateFormat") String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        write(currentTime, message);
    }
}
