package com.example.rishadkavad.medicalmessenger;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class OTPActivity extends AppCompatActivity {
    EditText otpEditText;
    Button otpSubmitButton;
    String userEmail,userPassword;
    String userOTP;
    String status = "Not registered!";
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        sqLiteDatabase = openOrCreateDatabase("api_db",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("create table if not exists login_tb (email varchar(32),password varchar(64),otp int)");

        Toast.makeText(getApplication(),"OTP Activity",Toast.LENGTH_SHORT).show();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                userEmail = null;
            } else {
                userEmail= extras.getString("USER_EMAIL");
                userPassword = extras.getString("USER_PASSWORD");
            }
        } else {
            userEmail= (String) savedInstanceState.getSerializable("USER_EMAIL");
            userPassword = (String) savedInstanceState.getSerializable("USER_PASSWORD");
        }
        Toast.makeText(getApplication(),userEmail,Toast.LENGTH_SHORT).show();
        new OTPGenerate(getApplicationContext()).execute(userEmail);
        otpEditText = (EditText)findViewById(R.id.otp_tv);
        otpSubmitButton = (Button)findViewById(R.id.otp_btn);

        otpSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOTP = String.valueOf(otpEditText.getText());
                new RegisterPeople(getApplicationContext()).execute(userEmail,userPassword,userOTP);

            }
        });
    }
    class OTPGenerate extends AsyncTask<String,Void,String>{
        Context context;

        public OTPGenerate(Context context) {
            this.context = context;

        }

        @Override
        protected String doInBackground(String... params) {
            String userEmail = (String)params[0];
            //String userPassword = (String)params[1];

            //String link = "http://192.168.43.245/APISystem/pages/GenerateOtp.php";
            String link = "https://rishadkavad.000webhostapp.com/PAS/pages/GenerateOtp.php";
            https://rishadkavad.000webhostapp.com/PAS/pages/GenerateOtp.php
            try{
                String data = URLEncoder.encode("user_email", "UTF-8") + "=" +
                        URLEncoder.encode(userEmail, "UTF-8");
//                data += "&" + URLEncoder.encode("user_password", "UTF-8") + "=" +
//                        URLEncoder.encode(userPassword, "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine())!=null){
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
            catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null) {
                Toast.makeText(getApplicationContext(), "Your OTP has been sent to your registered E-mail", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), s.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    class RegisterPeople extends AsyncTask<String,Void,String>{
        Context context;

        public RegisterPeople(Context context) {
            this.context = context;

        }

        @Override
        protected String doInBackground(String... params) {
            String userEmail = (String)params[0];
            String userPassword = (String)params[1];
            String userOtp = (String)params[2];

           // String link = "http://192.168.43.245/APISystem/pages/RegisterPeople.php";
            String link = "https://rishadkavad.000webhostapp.com/PAS/pages/RegisterPeople.php";

            try{
                String data = URLEncoder.encode("user_email", "UTF-8") + "=" +
                        URLEncoder.encode(userEmail, "UTF-8");
                data += "&" + URLEncoder.encode("user_password", "UTF-8") + "=" +
                        URLEncoder.encode(userPassword, "UTF-8");
                data += "&" + URLEncoder.encode("user_otp", "UTF-8") + "=" +
                        URLEncoder.encode(userOtp, "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine())!=null){
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
            catch (Exception e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            if(s!=null) {
                Toast.makeText(getApplicationContext(), "You are successfully registered!", Toast.LENGTH_LONG).show();
                status = "Registered";
                sqLiteDatabase.execSQL("insert into login_tb values('"+userEmail+"','"+userPassword+"','"+userOTP+"')");
                sqLiteDatabase.close();
                Intent intent = new Intent(OTPActivity.this,MainActivity.class);
                startActivity(intent);
            }

        }
    }


}
