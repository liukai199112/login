package com.example.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private EditText edt_userName;
    private EditText edt_password;
    private Button sign;
    private Button login;
    public static int MYACTIVITY_FAILURE = 1;
    public static int MYACTIVITY_SUCCESS = 2;
    String strResult = null;
    Handler handler;
    private boolean loginFlag;
    private String userName;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        edt_userName = (EditText) findViewById(R.id.user);
        edt_password = (EditText) findViewById(R.id.password);
        sign = (Button) findViewById(R.id.sign);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread loginThread = new Thread(new LoginHandler() );
                loginThread.start();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                loginFlag = msg.getData().getBoolean("loginFlag");
                if(loginFlag){
                    Toast.makeText(MyActivity.this,"登录成功",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MyActivity.this,"登录失败（用户名或密码错误）",Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    class LoginHandler implements Runnable{
        @Override
        public void run() {
            userName = edt_userName.getText().toString();
            password = edt_password.getText().toString();
            String url = "http://10.103.240.141:8090/login";
            boolean loginState = validatelocalLogin(userName,password,url);
            Message message = new Message();
            Bundle bundle = new Bundle();
            if(loginState){
                bundle.putBoolean("loginFlag",true);
                message.setData(bundle);
                handler.sendMessage(message);
            }else {
                bundle.putBoolean("loginFlag",false);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }

    private boolean validatelocalLogin(String userName,String password,String url){
        System.out.println("username"+userName);
        System.out.println("password"+password);
        boolean loginState = false;
        HttpPost httpRequest = new HttpPost(url);
        List params = new ArrayList<>();
        params.add(new BasicNameValuePair("userName",userName));
        params.add(new BasicNameValuePair("password",password));
        try{
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,5000);
            HttpConnectionParams.setSoTimeout(httpParams,5000);
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(),"UTF-8"));
                String str = reader.readLine();
                URLEncoder.encode(URLEncoder.encode(str, "UTF-8"), "UTF-8");
                reader.close();
                strResult = str;
                System.out.println("strResult:"+strResult);
                JSONObject object = new JSONObject(strResult);
                String flag = object.getString("success");
                String message = object.getString("message");
                System.out.print(".."+flag+".."+message);

            }
        }catch (Exception e){

        }
        if(strResult != null && strResult.contains("true")){
            loginState = true;
        }else {
            loginState = false;
        }
        return loginState;
    }
}
