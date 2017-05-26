package com.example.user.confirmedkill;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class main_page extends AppCompatActivity {

    SQLiteHelper db;
    TextView tv_count, tv_xrand, tv_frand, tv_id, tv_mtest;
    EditText et_password, et_dummy;
    String password, key, id, dummy;
    double count,random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
       tv_count = (TextView)findViewById(R.id.tv_count);
       tv_xrand = (TextView)findViewById(R.id.tv_xrand);
        tv_frand = (TextView)findViewById(R.id.tv_frand);
        //tv_mtest = (TextView)findViewById(R.id.tv_mtest);
        tv_id = (TextView)findViewById(R.id.tv_id);
        et_password = (EditText)findViewById(R.id.et_password);
        //et_password.setVisibility(View.INVISIBLE);
        et_dummy = (EditText)findViewById(R.id.et_dummy);

        id = getIntent().getExtras().getString("id");
        password = getIntent().getExtras().getString("password");

        et_password.setText(""+password);
    }

    public void showMessage(String title, String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    //METHOD CALCULATION
    public double getEquation(String password,double random,String key)
    {
        double result = 0;
        double x = 0;

        byte[] pass = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
             pass = password.getBytes(StandardCharsets.US_ASCII);
        }

        if(key.length() != password.length()){
            return 0;
        }

        for(int i = password.length(); i>0; i--)
        {
            double asci = (double) pass[password.length() - i];
            if(key.charAt(password.length()-i) == '1') {
                x = (double) Math.pow(random, i);
            }else{
                x = (double) Math.pow(random, 0);
            }
            result = result + (asci * x);
        }
        return result;
    }


    public void onCounts(View view)
    {
        validation();
    }
    //ONCLICK LOGIN
    public void validation()
    {
        String method = "login";

        Random rand = new Random();
        double generateRandom = rand.nextInt(1000)+1;

        random = generateRandom/100;

        password = et_password.getText().toString();
        dummy = et_dummy.getText().toString();

        tv_id.setText("App_ID : "+id);

        if(!password.isEmpty()) {
            backgroundTask bgTask = new backgroundTask(this);
            bgTask.execute(method, String.valueOf(password), String.valueOf(random),String.valueOf(dummy), id);
        }else{
            showMessage("Error","Password must not empty");
        }

    }

    public String createMAC() {
        try {

            // get a key generator for the HMAC-MD5 keyed-hashing algorithm
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");

            // generate a key from the generator
            SecretKey key = keyGen.generateKey();


            // create a MAC and initialize with the above key
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);

            String message = "This is a confidential message";

            // get the string as UTF-8 bytes
            byte[] b = message.getBytes("UTF-8");

            // create a digest from the byte array
            byte[] digest = mac.doFinal(b);

        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("No Such Algorithm:" + e.getMessage());

        }
        catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported Encoding:" + e.getMessage());

        }
        catch (InvalidKeyException e) {
            System.out.println("Invalid Key:" + e.getMessage());

        }
        return null;
    }




    // CHECK PASSWORD SQLITE
    public boolean checkData(String parameter)
    {
        Cursor res = db.getPassword();

        if(parameter == "id"){
            res = db.getID();
        }

        if(res.getCount() == 0) {
            return false;
        }
        while(res.moveToNext()){
            if(res.getString(0) == null){
                return false;
            }
        }
        return true;
    }

    //GET PASSWORD FROM SQLITE
    public String getPass()
    {
        String data = "";
        Cursor res = db.getPassword();

        StringBuffer stringBuffer = new StringBuffer();
        while(res.moveToNext())
        {
            data  = res.getString(0);
        }
        return data;
    }

    //GET ID FROM SQLITE
    public String getID()
    {
        String data = "";
        Cursor res = db.getID();

        StringBuffer stringBuffer = new StringBuffer();
        while(res.moveToNext())
        {
            data  = res.getString(0);
        }
        return data;
    }



    class backgroundTask extends AsyncTask<String, Void, String> {

        Context ctx;
        double count;
        String key = "";   // input to receive from server frand
        backgroundTask(Context ctx)
        {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String trans_url = "http://192.168.1.101/test/inFunction2.php";
            String vald_url = "http://192.168.1.101/test/inCount2.php";

            String method = strings[0];
            if(method.equals("login")){
                String password = strings[1];
                String random = strings[2];
                String dummy = strings[3];
                String id = strings[4];
                String response = "";
                try {
                    URL url = new URL(trans_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    String data = URLEncoder.encode("random","UTF-8")+"="+URLEncoder.encode(random,"UTF-8")+"&"+
                            URLEncoder.encode("dummy","UTF-8")+"="+URLEncoder.encode(dummy,"UTF-8")+"&"+
                            URLEncoder.encode("id","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                    String line = "";
                    while((line = bufferedReader.readLine())!=null)
                    {
                        response+= line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                    key = response;
                    count = getEquation(password,Double.parseDouble(random), key);

                    url = new URL(vald_url);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    outputStream = httpURLConnection.getOutputStream();
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    data = URLEncoder.encode("count","UTF-8")+"="+URLEncoder.encode(String.valueOf(count),"UTF-8")+"&"+
                            URLEncoder.encode("id","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    inputStream = httpURLConnection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                    response = "";
                    line = "";
                    while((line = bufferedReader.readLine())!=null)
                    {
                        response+= line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                    return response;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("true")) {
                Toast.makeText(ctx, "Authentication Success, Data Inserted", Toast.LENGTH_LONG).show();

                tv_count.setText("C1 : "+count);
                tv_xrand.setText("Xrand : "+random);
                tv_frand.setText("Frand : "+key);
            }else if(result.equals("false")){
                Toast.makeText(ctx, "Authentication Failed", Toast.LENGTH_LONG).show();

                tv_count.setText("C1 : "+count);
                tv_xrand.setText("Xrand : "+random);
                tv_frand.setText("Frand : "+key);
            }else{
                Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}



