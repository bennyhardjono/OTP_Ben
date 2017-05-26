package com.example.user.confirmedkill;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class Register extends AppCompatActivity {

    SQLiteHelper db;
    EditText et_name,et_password,et_email;
    TextView tv_id;
    String ref_password = "";
    String id,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = new SQLiteHelper(this);
        tv_id = (TextView)findViewById(R.id.tv_id);
        et_name = (EditText)findViewById(R.id.et_name);
        et_password = (EditText)findViewById(R.id.et_password);
        et_email = (EditText)findViewById(R.id.et_email);
        id = getIntent().getExtras().getString("id");
        tv_id.setText(""+id);
    }

    public final static boolean isValidEmail(String email) {
        CharSequence target = email;
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    //CHECK AND REGISTER
    public void onReg(View view) {
        Boolean tested = true;
        String name = et_name.getText().toString();
        String password = et_password.getText().toString();
        String email = et_email.getText().toString();
        String method = "register";
        StringBuilder strBuilder = new StringBuilder();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            tested = false;
            strBuilder.append("Input must not Empty !\n");
            showMessage("Error",strBuilder.toString());
        } else {
            if (!name.matches("[A-Za-z]*")) {
                tested = false;
                strBuilder.append("Name must be alphabetic\n");
            }
            if (!isValidEmail(email)) {
                tested = false;
                strBuilder.append("Email is not valid \n");
            }
            if (password.length() < 10 || password.length() > 20) {
                tested = false;
                strBuilder.append("Password must be 10 to 20 characters\n");
            }
            if (!password.matches("[A-Za-z0-9]*")) {
                tested = false;
                strBuilder.append("Password must be alphabetic and numeric \n");
            }
        }
        ref_password = password;
        if (tested) {
            backgroundTask backgroundTask = new backgroundTask(this);
            backgroundTask.execute(method, name, password, email, id);
        }else{
            showMessage("Error",strBuilder.toString());
        }

    }

    //RETURN TO LOGIN PAGE
    public void inSuccess()
    {
        boolean insertPass = db.insertPassword(et_password.getText().toString(), id);
        if(insertPass) {
            Intent intent = new Intent();
            this.setResult(RESULT_OK, intent);
            finish();
        }
    }

    //ALERT MESSAGE FOR ERROR
    public void showMessage(String title, String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    class backgroundTask extends AsyncTask<String, Void, String> {

        Context ctx;
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
            String reg_url = "http://192.168.1.101/test/registeration.php";
            String method = strings[0];
            if(method.equals("register"))
            {
                String name = strings[1];
                String password = strings[2];
                String email = strings[3];
                String id = strings[4];

                String response = "";

                try {
                    URL url = new URL(reg_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    String data = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+
                            URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8")+"&"+
                            URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
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
            if(result.equals("Failed")) {
                Toast.makeText(ctx, "Registeration Failed", Toast.LENGTH_LONG).show();
                showMessage("Error","Email has been used");
            }else {
                Toast.makeText(ctx, "Registeration Success", Toast.LENGTH_LONG).show();
                inSuccess();
            }
        }
    }
}
