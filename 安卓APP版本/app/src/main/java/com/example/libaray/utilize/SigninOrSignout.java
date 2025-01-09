package com.example.libaray.utilize;

import okhttp3.*;
import org.json.JSONObject;

import android.content.Context;
import android.os.StrictMode;

import java.net.CookieHandler;
import java.net.CookieManager;
public class SigninOrSignout {
    private static final String BASE_URL = "http://10.20.15.27/ic-web/phoneSeatReserve/";
    private  String token;
    private  String devSn;
    private  String resvId;
    private  String unionId;
    private PreferencesManager preferencesManager;
    private final OkHttpClient client;
    // 初始化参数
    public SigninOrSignout(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))  // 设置 JavaNetCookieJar
                .build();

    }
    public void init(Context context){
        preferencesManager= new PreferencesManager(context);
        this.devSn= preferencesManager.getDevId();
        this.resvId=preferencesManager.getResvId();
        this.unionId=preferencesManager.getUnionId();

    }

    // 登录并获取 token
    private  String login(String devSn , String unionId) {
        try {
            String urlString = BASE_URL + "login";
            JSONObject payload = new JSONObject();
            payload.put("devSn", devSn);
            payload.put("unionId", unionId);
            payload.put("type", "1");
            payload.put("bind", 0);

            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json;charset=UTF-8"));
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(body)
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    return jsonResponse.getJSONObject("data").getString("token");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 签到
    public void scanIn() {
        try {
            this.token = login(devSn, unionId);
            String urlString = BASE_URL + "sign";
            JSONObject payload = new JSONObject();
            payload.put("resvId", this.resvId);

            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json;charset=UTF-8"));
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(body)
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("token", token)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("签到成功：" + responseBody);
                } else {
                    System.out.println("签到失败，状态码：" + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 签退
    public  void scanOut() {
        try {
            this.token = login(devSn, unionId);
            String urlString = BASE_URL + "quit";
            JSONObject payload = new JSONObject();
            payload.put("resvId", resvId);

            RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json;charset=UTF-8"));
            Request request = new Request.Builder()
                    .url(urlString)
                    .post(body)
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("token", token)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("签退成功：" + responseBody);
                } else {
                    System.out.println("签退失败，状态码：" + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
