package com.example.rishadkavad.medicalmessenger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button alertButton;
    TextView textViewCoordinates;
    int count = 0;
    private BroadcastReceiver broadcastReceiver;
    Double lat = 0.0;
    Double lon = 0.0;
    Geocoder geocoder;
    List<Address> addresses ;
    SQLiteDatabase sqLiteDatabase;
   public static String userEmail;

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Toast.makeText(getApplicationContext(),"Lat :"+intent.getExtras().get("lat")+"Lon :"+intent.getExtras().get("lon"),Toast.LENGTH_SHORT).show();
                    lat = Double.parseDouble(String.valueOf(intent.getExtras().get("lat")));
                    lon = Double.parseDouble(String.valueOf(intent.getExtras().get("lon")));

                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(lat,lon,1);
                        String address = addresses.get(0).getAddressLine(0);
                        String area = addresses.get(0).getLocality();
                        String city = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryCode();
                        String postalId = addresses.get(0).getPostalCode();

                        String fullAddress = address+","+area+","+city+","+","+country+","+postalId;
                        textViewCoordinates.setText(fullAddress);
                        String latitude = String.valueOf(lat);
                        String longitude = String.valueOf(lon);
                        String userEmail = String.valueOf(MainActivity.userEmail);

                        new RegisterAccident(getApplicationContext()).execute(latitude,longitude,userEmail);
                        Toast.makeText(getApplicationContext(),"User Email :"+userEmail,Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(),GpsService.class);
                        stopService(i);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(getApplicationContext(),"New App 02:11 pm 20-03-2018",Toast.LENGTH_SHORT).show();
        textViewCoordinates = (TextView)findViewById(R.id.coordinates_tv);
        //Code to check user login
        sqLiteDatabase = openOrCreateDatabase("api_db",MODE_PRIVATE,null);
        Cursor cursor = sqLiteDatabase.rawQuery("select * from login_tb", null);
        if (cursor.moveToNext()){
            textViewCoordinates.setText("User Email :"+String.valueOf(cursor.getString(0)));
            userEmail = String.valueOf(cursor.getString(0));
        }
        sqLiteDatabase.close();
        //Code ends(user login)
        alertButton = (Button) findViewById(R.id.btn_alert);
        //speakerButton = (Button) findViewById(R.id.btn_voice);
      if(!runTimePermissions())
          enableButtons();
    }

    private void enableButtons() {
    alertButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(count == 0){
               // Toast.makeText(getApplicationContext()," Count :"+count,Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Emergency")
                        .setMessage("Do you really want to rise an emergency message?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(MainActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
            count++;
            //Toast.makeText(getApplicationContext(),"Count :"+count,Toast.LENGTH_SHORT).show();
            if(count == 5) {

                Intent intent = new Intent(getApplicationContext(), GpsService.class);
                startService(intent);
                count = 0;
               // alertButton.setEnabled(false);
            }
        }
    });

    }


    private boolean runTimePermissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enableButtons();
            }
            else {
                runTimePermissions();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.menu_itm1 : Toast.makeText(getApplicationContext(),"News",Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        // your code.
        Intent intent = new Intent(getApplicationContext(), GpsService.class);
        stopService(intent);
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
    class RegisterAccident extends AsyncTask<String,Void,String> {
        Context context;

        public RegisterAccident(Context context) {
            this.context = context;

        }

        @Override
        protected String doInBackground(String... params) {
            String latitude = (String)params[0];
            String longitude = (String)params[1];
            String userEmail = (String)params[2];

            //String link = "http://192.168.43.245/APISystem/notifications/RegisterAccident.php";
            String link = "https://rishadkavad.000webhostapp.com/PAS/notifications/RegisterAccident.php";
            try{
                String data = URLEncoder.encode("lat", "UTF-8") + "=" +
                        URLEncoder.encode(latitude, "UTF-8");
                data += "&" + URLEncoder.encode("lon", "UTF-8") + "=" +
                        URLEncoder.encode(longitude, "UTF-8");
                data += "&" + URLEncoder.encode("user_email", "UTF-8") + "=" +
                        URLEncoder.encode(userEmail, "UTF-8");


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
                Toast.makeText(getApplicationContext(), "Your request has been sent!", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Nearest Driver :"+s.toString(), Toast.LENGTH_LONG).show();
                textViewCoordinates.setText("Nearest Driver :"+s.toString());
                //Intent intent = new Intent(OTPActivity.this,MainActivity.class);
               // startActivity(intent);
            }

        }
    }


}
