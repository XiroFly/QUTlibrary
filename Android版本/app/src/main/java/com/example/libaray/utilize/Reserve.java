package com.example.libaray.utilize;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import com.example.libaray.utilize.EnumTurns;
import javax.crypto.Cipher;
import java.net.CookieManager;
public class Reserve {

    private static final String BASE_URL = "http://10.20.15.27/ic-web";
    private static final String TAG = "Reserve";
    private static String token;
    public  String resvId;
    public String uuid;
    public String unionId;
    public String devId;
    // 保存从 JSON 文件中读取的配置
    private String mainAccount;
    private String mainPassword;
    private String auxAccount;
    private String auxPassword;
    private JSONArray useSettings;
    private String accNo;


    private PreferencesManager preferencesManager;

    public Reserve(Context context) {

        this.preferencesManager = new PreferencesManager(context);
        loadSettings(context);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        CookieHandler.setDefault(new CookieManager());

    }

    // 登录并获取token
    public boolean signIn(String logonName, String password) {
        try {
            String publicKeyResponse = sendGetRequest(BASE_URL + "/login/publicKey");
            JSONObject publicKeyData = new JSONObject(publicKeyResponse).getJSONObject("data");
            String publicKey = publicKeyData.getString("publicKey");
            String nonceStr = publicKeyData.getString("nonceStr");

            String encryptedPassword = encryptPassword(password + ";" + nonceStr, publicKey);

            JSONObject loginPayload = new JSONObject();
            loginPayload.put("logonName", logonName);
            loginPayload.put("password", encryptedPassword);
            loginPayload.put("captcha", "");
            loginPayload.put("consoleType", 16);

            String loginResponse = sendPostRequest(BASE_URL + "/login/user", loginPayload.toString());
            JSONObject loginData = new JSONObject(loginResponse).getJSONObject("data");
            token = loginData.getString("token");
            accNo = loginData.getString("accNo");
            Log.i(TAG, "Token: " + token);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during sign-in", e);
            return false;
        }
    }

    // 预约
    public boolean reserve(String type,boolean isAuxAccount)  {
        //boolean isAuxAccount,为真使用辅助账号
        String logonName = isAuxAccount ? auxAccount : mainAccount;
        String password = isAuxAccount ? auxPassword : mainPassword;

        try {
            String[] nextTimeSection = getNextTimeSection(type);
            if (nextTimeSection == null) {
                Log.e(TAG, "No available time section.");
                return false;
            }
            if(!signIn(logonName, password)){Log.e(TAG, "登录错误"); return false;}

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            JSONObject reservePayload = new JSONObject();
            reservePayload.put("testName", "");
            reservePayload.put("appAccNo", accNo);
            reservePayload.put("memberKind", 1);
            reservePayload.put("resvDev", new JSONArray().put(getDeviceId()));
            reservePayload.put("resvMember", new JSONArray().put(accNo));
            reservePayload.put("resvProperty", 0);
            reservePayload.put("sysKind", 8);
            reservePayload.put("resvBeginTime", currentDate + " " + nextTimeSection[0]+":00");
            reservePayload.put("resvEndTime", currentDate + " " + nextTimeSection[1]+":00");

            String reserveResponse = sendPostRequest(BASE_URL + "/reserve", reservePayload.toString(), this.token);
            JSONObject reserveData = new JSONObject(reserveResponse).getJSONObject("data");

            this.resvId = reserveData.getString("resvId");
            this.uuid = reserveData.getString("uuid");
            this.devId = reserveData.getJSONArray("resvDevInfoList").getJSONObject(0).getString("devId");
            if(isAuxAccount){preferencesManager.saveUuid(this.uuid);}
            else{preferencesManager.saveReservationDetails(this.resvId, this.uuid, this.unionId, this.devId);}
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during reserve", e);
            return false;
        }
    }

    // 取消预约
    public boolean deleteReserve(String type,boolean isAuxAccount) {
        //isAuxAccount  ==ture为辅助账号,只有辅助账号有取消的操作
        String logonName = isAuxAccount ? auxAccount : mainAccount;
        String password = isAuxAccount ? auxPassword : mainPassword;
        try {
            if(!signIn(logonName, password)){Log.e(TAG, "登录错误");}
            JSONObject deletePayload = new JSONObject();
            deletePayload.put("uuid", preferencesManager.getUuid());
            String deleteResponse = sendPostRequest(BASE_URL + "/reserve/delete", deletePayload.toString(), this.token);
            //Log.i(TAG, "Delete response: " + deleteResponse);
            return true;
        } catch (Exception e) {
            //Log.e(TAG, "Error during delete reserve", e);
            return false;
        }
    }

    // 工具方法：发送GET请求
    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json, text/plain, */*");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    // 工具方法：发送POST请求
    private String sendPostRequest(String urlString, String payload) throws Exception {
        return sendPostRequest(urlString, payload, null);
    }

    private String sendPostRequest(String urlString, String payload, String token) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        if (token != null) {
            connection.setRequestProperty("token", token);
        }

        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(payload);
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    // 获取下一时间段
    private String[] getNextTimeSection(String type) {
        try {
            EnumTurns.turns turn = EnumTurns.turns.valueOf(type);
            JSONObject setting;
                switch (turn) {
                    case morningReserve:
                        setting = useSettings.getJSONObject(0);
                        return new String[]{
                                setting.getString("start"),
                                setting.getString("end")
                        };

                    case afternoonReserve:
                        setting = useSettings.getJSONObject(1);
                        return new String[]{
                                setting.getString("start"),
                                setting.getString("end")
                        };

                    case eveningReserve:
                        setting = useSettings.getJSONObject(2);
                        return new String[]{
                                setting.getString("start"),
                                setting.getString("end")
                        };


                }



        } catch (Exception e) {
            //Log.e(TAG, "Error parsing use settings", e);
        }
        return null;
    }
    // 加密密码
    private String encryptPassword(String message, String publicKeyStr) throws Exception {
        // Clean and decode the public key
        publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] publicKeyBytes = Base64.decode(publicKeyStr, Base64.DEFAULT);

        // Generate PublicKey instance
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // Initialize cipher for encryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Encode the message in GBK charset
        Charset gbkCharset = Charset.forName("GBK");
        byte[] messageBytes = message.getBytes(gbkCharset);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(messageBytes);

        // Convert encrypted bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : encryptedBytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        // Ensure the result length matches 256
        while (hexString.length() < 256) {
            hexString.insert(0, '0');
        }

        // Convert the hex string to bytes
        byte[] hexBytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexBytes.length; i++) {
            hexBytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        }

        // Base64 encode the bytes and return as string
        return new String(Base64.encode(hexBytes, Base64.NO_WRAP), gbkCharset);
    }
    private void loadSettings(Context context) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.openFileInput("settings.json"))
            );
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONObject settings = new JSONObject(jsonBuilder.toString());
            this.mainAccount = settings.getString("account");
            this.mainPassword = settings.getString("password");
            this.auxAccount = settings.getString("auxAccount");
            this.auxPassword = settings.getString("auxPassword");
            this.unionId=settings.getString("wechatId");
        } catch (Exception e) {
            //Log.e(TAG, "Failed to load settings.json", e);
        }
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.openFileInput("usesettings.json"))
            );
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            this.useSettings = new JSONArray(jsonBuilder.toString());
        } catch (Exception e) {
           // Log.e(TAG, "Failed to load usesettings.json", e);
        }
    }
    private int getDeviceId() {
    try {
        JSONObject setting = useSettings.getJSONObject(0);
        String floor=setting.getString("floor");
        String area=setting.getString("area");
        String seat=setting.getString("seat");
        int start = 0, end = 0;

        switch (floor) {
            case "3F":
                if (area.equals("A")) {
                    start = 100494740;
                    end = 100494822;
                } else if (area.equals("B")) {
                    start = 100494719;
                    end = 100495049;
                }
                break;
            case "4F":
                if (area.equals("A")) {
                    start = 100495052;
                    end = 100495175;
                } else if (area.equals("B")) {
                    start = 100495176;
                    end = 100495413;
                }
                break;
            case "5F":
                start = 100495416;
                end = 100495493;
                break;
            case "6F":
                if (area.equals("A")) {
                    start = 100495494;
                    end = 100495625;
                } else if (area.equals("B")) {
                    start = 100495638;
                    end = 100495889;
                } else if (area.equals("C")) {
                    start = 100495890;
                    end = 100496032;
                }
                break;
        }
        // 根据座位号计算设备号
        int seatNumber = Integer.parseInt(seat);
        return start + seatNumber - 1;
    }
    catch (Exception e) {
        //Log.e(TAG, "JSONObject setting = useSettings.getJSONObject(0);", e);
    }
    return 0;
    }

}
