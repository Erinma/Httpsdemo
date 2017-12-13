package com.example.kson.httpsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  loadData();//信任所有https主机的访问
        cardData();//信任当前证书的https主机的访问
    }

    /**
     * 带证书验证
     */
    private void cardData() {
        FormBody formbody = new FormBody.Builder().add("mobile", "18612991023").add("password", "111111").build();
        Request request = new Request.Builder().url("https://www.12306.cn/mormhweb/").post(formbody).build();

        setCard().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
Log.d("fai",e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
Log.e("carddata",response.body().string());
            }
        });
    }

    /**
     * 信任所有https的请求：第一种实现，tls／ssl安全协议
     */
    private void loadData() {
        OkHttpClient httpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(new LogInterceptor())
//                        .sslSocketFactory(createSSLSocketFactory())
//                        .hostnameVerifier(new TrustAllHostnameVerifier())
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(false)
                        .build();
        FormBody formbody = new FormBody.Builder().add("mobile", "18612991023").add("password", "111111").build();
     //   Request request = new Request.Builder().url("https://www.zhaoapi.cn/user/login").post(formbody).build();
        Request request = new Request.Builder().url("https://www.12306.cn/mormhweb/").post(formbody).build();


        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure",e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("onResponse",response.body().string());

            }
        });

    }

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }


    /**
     * app带证书验证
     * @return
     */
    public OkHttpClient setCard() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            String certificateAlias = Integer.toString(0);
            keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(getAssets().open("zhaoapi_server.cer")));//拷贝好的证书
            SSLContext sslContext = SSLContext.getInstance("TLS");
            final TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            builder.sslSocketFactory(sslContext.getSocketFactory());
            builder.addInterceptor(new LogInterceptor());
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {

                    return true;
                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }



}
