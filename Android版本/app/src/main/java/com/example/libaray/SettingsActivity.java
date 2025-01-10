package com.example.libaray;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
public class SettingsActivity extends AppCompatActivity {
        private EditText editAccount, editPassword, editWeChatId;
        private EditText editAuxAccount, editAuxPassword;


        private static final String JSON_FILE = "settings.json";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            // 初始化组件
            editAccount = findViewById(R.id.editAccount);
            editPassword = findViewById(R.id.editPassword);
            editWeChatId = findViewById(R.id.editWeChatId);
            editAuxAccount = findViewById(R.id.editAuxAccount);
            editAuxPassword = findViewById(R.id.editAuxPassword);
            Button btnSave = findViewById(R.id.btnSave);
            // 初始化按钮
            Button btnExplain = findViewById(R.id.btnExplain);

            // 加载已保存的数据
            loadSettings();
            btnExplain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showExplanationDialog();
                }
            });
            // 保存按钮事件
            btnSave.setOnClickListener(v -> saveSettings());
        }

        private void saveSettings() {
            try {
                JSONObject settings = new JSONObject();
                settings.put("account", editAccount.getText().toString());
                settings.put("password", editPassword.getText().toString());
                settings.put("wechatId", editWeChatId.getText().toString());
                settings.put("auxAccount", editAuxAccount.getText().toString());
                settings.put("auxPassword", editAuxPassword.getText().toString());

                File file = new File(getFilesDir(), JSON_FILE);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(settings.toString());
                    Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show(); // 保存成功提示
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "保存设置失败", Toast.LENGTH_SHORT).show(); // 保存失败提示
            }
        }

        private void loadSettings() {
            try {
                File file = new File(getFilesDir(), JSON_FILE);
                if (!file.exists()) return;

                try (FileReader reader = new FileReader(file)) {
                    char[] buffer = new char[(int) file.length()];
                    reader.read(buffer);
                    JSONObject settings = new JSONObject(new String(buffer));


                    editAccount.setText(settings.optString("account", ""));
                    editPassword.setText(settings.optString("password", ""));
                    editWeChatId.setText(settings.optString("wechatId", ""));
                    editAuxAccount.setText(settings.optString("auxAccount", ""));
                    editAuxPassword.setText(settings.optString("auxPassword", ""));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "加载设置失败", Toast.LENGTH_SHORT).show(); // 加载失败提示
            }
        }
    private void showExplanationDialog() {
        // 创建 AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("丰川祥子提醒您")
                .setMessage("unionId" +
                        "类似Steam令牌，在图书馆扫码时也是使用它来标识你的身份。\n" +
                        "辅助账号密码是另一个登录图书馆的账号密码（你同学的），因为在图书馆使用座位时是不允许预约座位的。\n" +
                        "预约也只能预约一个时间段且提前6小时才能预约，只能使用另一个的账号来占下午的，不然有同学会抢你的座位哟。\n" +
                        "在你使用完座位签退后，会自动再将辅助账号取消预约，然后再预约抢占，可实现占座位一天。\n" +
                        "本程序会在你选择的开始时间前5小时55分钟预约，开始时间签到，结束时间签退.所以选择开始时间最好是当前时间六小时后的\n" +
                        "当然预约、签到签退都能自动实现，但是闸机检测你必须在签到时间之前通过一次。实际一天只通过一次闸机就行。\n" +
                        "三个时间段打上option的才有效，记得点save。\n"+
                        "开启定时需要SCHEDULE_EXACT_ALARM权限,最好允许后台运行和避免电池优化\n"+
                        "日志在/storage/emulated/0/Android/data/com.example.library/files/app_logs.txt")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 用户点击确定
                        dialog.dismiss();
                    }
                });

        // 显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}



