package com.example.ankush.customersupportai;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RESULT";
    private static final int RECORD_REQUEST_CODE = 101;
    Button fab;
    TextView tvReply;
    TextView tvUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


                fab = findViewById(R.id.fab);
                tvReply = findViewById(R.id.tvReply);
                tvUser = findViewById(R.id.tvUser);



                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        promptSpeechInput();
                    }
                });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something I'm giving up on you");

        try {
            startActivityForResult(intent,RECORD_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (resultCode == RESULT_OK && data != null) {

                    ArrayList<String> resultString = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String userQuery = resultString.get(0);
                    tvUser.setText(userQuery);
                    RetrieveFeedTask task= new RetrieveFeedTask();
                    task.execute(userQuery);
                } break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }

    }

    public String GetText(String query){
        String text = "";
        BufferedReader reader = null;

        String speech;
        try {

            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");

            //Send POST data request
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Authorization", "Bearer de67e5339fc84a10b5d8ab87296b06b7");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            //Create JSONObject here
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(query);
            jsonObject.put("query", jsonArray);

            jsonObject.put("lang", "en");
            jsonObject.put("sessionId", "12345");


            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            Log.e(TAG, "After conversion is " + jsonObject.toString());
            wr.write(jsonObject.toString());
            wr.flush();
            Log.e(TAG, "JSON is " + jsonObject);

            //Get the server REsponse
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            //REad Server Response
            while ((line = reader.readLine()) != null) {
                //Append server response in string;
                sb.append(line + "\n");
            }

            text = sb.toString();
            JSONObject object1 = new JSONObject(text);
            JSONObject object2 = object1.getJSONObject("result");
            JSONObject fulfilment = null;
            speech = null;

            fulfilment = object2.getJSONObject("fulfillment");
            speech = fulfilment.optString("speech");

            SharedPreferences sharedPreferences = getSharedPreferences("pref1", Context.MODE_PRIVATE);
            String userid = sharedPreferences.getString("userId", null);

            Log.e(TAG, "Response is " + text);
            Log.e(TAG, "\nReply is" + speech);


            return speech;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }


    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... voids) {
            String s = null;
            s = GetText(voids[0]);
            Log.e(TAG,"Response from do in background " + s);
            return s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e(TAG,"Response from do in background " + s);
            tvReply.setText(s);


        }
    }


}
