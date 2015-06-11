package com.hello.seanbroomfield.todo;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

//Intent filter removed from manifest for login activity so it doesnt start as a default.

public class Login_Activity extends Activity implements View.OnClickListener {

    private static final String TAG = Login_Activity.class.getSimpleName();
    public static final String SESSION_TOKEN = "sessionToken";
    public static final String OBJECT_ID = "objectId";
    private Button button;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private AsyncTask<String, Integer, JSONObject> asyncTask;


        @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Have to cast to a button
        button = (Button) findViewById(R.id.login_btn);

        //Have to use onClickListener and implements View.OnclickListener
        button.setOnClickListener(this);

        usernameEditText = (EditText) findViewById(R.id.username_et);
        passwordEditText = (EditText) findViewById(R.id.password_et);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       // if (id == R.id.action_settings) {
       //     return true;
       // }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean isError = false;

        //Checks if the field is empty and if the string contains a NULL
        if(TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.this_field_is_required));
            isError = true;
        }

        /*
        Used to check to see if the user name entered is an email address or not!
        if(Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            passwordEditText.setError("This field has to be an email!")
        }
        */

        if(TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.this_field_is_required));
            isError = true;
        }

        if(!isError) {
            passwordEditText.setText("");
            login(username, password);
        }
    }

    private void login(String username, String password) {
            //Application context required to make a toast, message, duration time
            //<Returns login string, Integer tracks progress, Boolean is success of logging in
            asyncTask = new AsyncTask<String, Integer, JSONObject>() {
            @Override
            //String... is a string array
            protected JSONObject doInBackground(String... strings) {
                String username = strings[0];
                String password = strings[1];
                try {
                    String result = HttpUtils.getLogin(username, password);
                    Log.d(TAG, "Result: " + result);
                    Log.d(TAG, username + " " + password);
                    return new JSONObject(result);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //If exception from JSON then return this null
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                Log.d(TAG, "Progress: " + values[0]);
            }

            //GOOD PLACE TO RECEIVE DATA
            @Override
            protected void onPostExecute(JSONObject result) {
                super.onPostExecute(result);
                button.setEnabled(true);
                if (result == null) {
                    Toast.makeText(getApplicationContext(), "Connection error", Toast.LENGTH_LONG).show();
                } else if (result.has("error")) {
                    Toast.makeText(getApplicationContext(), "Error: " + result.optString("error"), Toast.LENGTH_LONG).show();
                } else {
                    String sessionToken = result.optString(SESSION_TOKEN);
                    String userId = result.optString(OBJECT_ID);

                    //Saves data for future login
                    ((TodoApplication) getApplication()).getLoginManager().saveLogin(sessionToken, userId);

                    Toast.makeText(getApplicationContext(), "Logged In!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ToDoListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                button.setEnabled(false);
            }
        };

        asyncTask.execute(username, password);
    }
}