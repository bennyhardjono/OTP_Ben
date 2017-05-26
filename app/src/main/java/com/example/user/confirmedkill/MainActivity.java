package com.example.user.confirmedkill;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.os.AsyncTask;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.telephony.TelephonyManager;
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
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.Random;

public class MainActivity extends AppCompatActivity {

    SQLiteHelper db;
    //Context notice;
    double count, random;
    EditText tv_id, et_password ;
    String id, phone_id, password ,key, method;
    TextView tv_count,tv_rand,tv_register,tv_dolp,tv_imei,tv_hashedimei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_id = (EditText)findViewById(R.id.tv_id);
        //tv_id.setVisibility(View.INVISIBLE);
        tv_hashedimei = (TextView)findViewById(R.id.tv_hashedimei);
        tv_imei = (TextView)findViewById(R.id.tv_imei);
        tv_register =(TextView)findViewById(R.id.tv_register);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_rand = (TextView)findViewById(R.id.tv_rand);
        tv_dolp = (TextView)findViewById(R.id.tv_dolp);
        et_password = (EditText)findViewById(R.id.et_password);

        db = new SQLiteHelper(this);


        reqAppId();
        tv_hashedimei.setText("Hashed IMEI : "+phone_id);



    }

    //CHECK ID AND PASSWORD FROM MYSQL (IF IMEI NOT YET REGISTERED, REGISTER IT)
    public void reqAppId() {
        phone_id = generateID().toString();

        if(!checkData("id")) {
            method = "oncreateRegister";
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.execute(method, phone_id);
        }else{
            method = "onAppStart";
            id = getID();
            tv_id.setText(""+id);
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.execute(method, id);

            if(checkData("password"))
            {
                password = getPass();
                et_password.setText(""+password);
                //et_password.setVisibility(View.INVISIBLE);
                validation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                tv_register.setVisibility(View.INVISIBLE);
                showMessage("Validation","You Have Successfully Registered ! ");
                //String newpassword = getPass();
                //et_password.setText(""+newpassword);
                finish();
                startActivity(getIntent());
            }
        }
    }

    //METHOD ALERT FOR ERROR
    public void showMessage(String title, String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    //Get IMEI
    private String generateID(){
        String deviceid = android.provider.Settings.Secure.getString(this.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

        if("9774d56d682e549c".equals(deviceid) || deviceid == null)
        {
            deviceid = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

            if (deviceid == null) {
                Random tmprand = new Random();
                deviceid = String.valueOf(tmprand.nextLong());
            }
        }
        tv_imei.setText("IMEI : "+ deviceid);
        return getHash(deviceid);
    }

    public String getHash(String stringtohash)
    {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[]result = null;
        try {
            result = digest.digest(stringtohash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : result){
            stringBuilder.append(String.format("%02x",b));
        }

        String messageDigest = stringBuilder.toString();
        return messageDigest;
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

    public void onCount(View view)
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
        /*
        if(checkData("password"))
        {
            password = getPass();
        }else{
            password = et_password.getText().toString();
        }

        */
        password = et_password.getText().toString();

        if(!password.isEmpty()) {
            BackgroundTask bgTask = new BackgroundTask(this);
            bgTask.execute(method, String.valueOf(password), String.valueOf(random), id);
        }else{
            showMessage("Error","Password must not empty");
        }

    }

    //ONCLICK REGISTER BUTTON
    public void manualRegIntent(View view){
        Intent intent = new Intent(this,Register.class);
        intent.putExtra("id",id);
        startActivityForResult(intent,1);
    }

    //INTENT MAIN PAGE LAYOUT
    public void intention() {
        Intent intent = new Intent(this, main_page.class);
        intent.putExtra("password", password);
        intent.putExtra("id", id);
        intent.putExtra("count",count);
        intent.putExtra("key",key);
        intent.putExtra("random",random);
        if (!checkData("password")) {
            boolean isInserted = db.insertPassword(password, id);
            if (isInserted) {
                startActivity(intent);
            }
        }else{
            startActivity(intent);
        }
    }

    //INTENT REGISTER LAYOUT
    public void inReg(String id)
    {
        Intent intent = new Intent(this,Register.class);
        intent.putExtra("id",id);
        startActivityForResult(intent,1);
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

    //GET PASSWORD FROM SQLITE
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

    //CONNECTION TO DATABASE (ASYNCTASK)
    class BackgroundTask extends AsyncTask<String, Void, String> {

        AlertDialog alertDialog;
        Context ctx;
        double count;
        String key = "";
        BackgroundTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            alertDialog = new AlertDialog.Builder(ctx).create();
            alertDialog.setTitle("Login Information....");
        }

        @Override
        protected String doInBackground(String... strings) {
            String get_url = "http://192.168.1.101/test/Getid.php";
            String pas_url = "http://192.168.1.101/test/Getpass.php";
            String log_url = "http://192.168.1.101/test/inFunction.php";
            String val_url = "http://192.168.1.101/test/inCount.php";
            String method = strings[0];

            if(method.equals("oncreateRegister")) {
                String phone_id = strings[1];
                String response = "";

                try {
                    URL url = new URL(get_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String data = URLEncoder.encode("phone_id", "UTF-8") + "=" + URLEncoder.encode(phone_id, "UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        response += line;
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

            if(method.equals("onAppStart")) {
                String id = strings[1];
                String response = "";

                try {
                    URL url = new URL(pas_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        response += line;
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

            if(method.equals("login")){
                String password = strings[1];
                String random = strings[2];
                String id = strings[3];
                String response = "";
                try {
                    URL url = new URL(log_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    String data = URLEncoder.encode("random","UTF-8")+"="+URLEncoder.encode(random,"UTF-8")+"&"+
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



                    url = new URL(val_url);
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
            if(result.equals("true")){
                Toast.makeText(ctx, "Login Success", Toast.LENGTH_LONG).show();
                intention();

                tv_count.setText("C1 : "+count);
                tv_rand.setText("Frand :"+key);
                tv_dolp.setText("Xrand :"+random);

            }else if(result.equals("false")) {
                Toast.makeText(ctx, "Password does not match", Toast.LENGTH_LONG).show();
            }else if(result.equals("unregistered")) {
                inReg(id);
            }else if(result.equals("registered")){
                if(!checkData("password")) {
                    Toast.makeText(ctx, "You already Registered, Please insert your password !", Toast.LENGTH_LONG).show();
                }
                tv_register.setVisibility(View.INVISIBLE);
                tv_register.setOnClickListener(null);
            }else if(result.contains(",")) {
                String[] separated = result.split(",");
                id = separated[0];
                String check = separated[1];
                db.insertID(id);
                if (check.equals("false")) {
                    inReg(id);
                } else {
                    Toast.makeText(ctx, "You already Registered, Please insert your password !", Toast.LENGTH_LONG).show();
                    tv_register.setVisibility(View.INVISIBLE);
                    tv_register.setOnClickListener(null);
                }
            }else{
                Toast.makeText(ctx, "Connection Error", Toast.LENGTH_LONG).show();
                //tv_id.setText(""+result);
            }
        }
    }
}
