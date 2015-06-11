package com.hello.seanbroomfield.todo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;


public class AddToDoActivity extends Activity {

    public static final String CONTENT = "content";
    public static final String DONE = "done";
    public static final String TODO = "todo";
    private EditText contentEditText;
    private CheckBox doneBox;
    private Button saveButton;
    private TodoApplication.LoginManager loginManager;
    private TodoApplication application;
    private TodoDao todoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        contentEditText = (EditText) findViewById(R.id.content_et);
        doneBox = (CheckBox) findViewById(R.id.done_cb);
        saveButton  = (Button) findViewById(R.id.save_bt);
        application = (TodoApplication) getApplication();
        loginManager = application.getLoginManager();
        todoDao = application.getTodoDao();


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = contentEditText.getText().toString();
                boolean isDone = doneBox.isChecked();

                final Todo todo = new Todo();
                todo.content = content;
                todo.done = isDone;
                todo.userId = application.getLoginManager().getUserId();


                //Sending data to the server is a lengthy operation so asynctask needed
                AsyncTask<Todo, Void, JSONObject> asyncTask = new AsyncTask<Todo, Void, JSONObject>() {
                    @Override
                    protected JSONObject doInBackground(Todo... todos) {
                        Todo todoToSave = todos[0];
                        //Token is the session token
                        String token =  application.getLoginManager().getSessionToken();
                        try {
                            String result = HttpUtils.postTodo(todoToSave.toJsonString(),token);
                            return new JSONObject(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(JSONObject jsonObject) {
                        super.onPostExecute(jsonObject);

                        if(jsonObject == null) {
                            //error
                        } else if (jsonObject.has("error")) {
                            Toast.makeText(getApplicationContext(), "Error" + jsonObject.optString("error"), Toast.LENGTH_LONG).show();
                        } else {
                            //Parse the object and add to the database
                            try {
                                Todo todoFromSever = Todo.fromJsonObject(jsonObject);
                                todoDao.insertOrUpdate(todoFromSever);
                                setResult(RESULT_OK);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_to_do, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
