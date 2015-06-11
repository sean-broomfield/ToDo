package com.hello.seanbroomfield.todo;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static com.hello.seanbroomfield.todo.R.layout.todo_item;

//"extends ListActivity" needed to extract ListView
public class ToDoListActivity extends ListActivity {

    public static final int REQUEST_CODE = 123;
    private static final String TAG = ToDoListActivity.class.getSimpleName();
    private TodoApplication.LoginManager loginManager;
    private ArrayAdapter<Todo> adapter;
    private TodoDao todoDao;
    private SimpleCursorAdapter simpleCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setProgressBarIndeterminate(true);
        TodoApplication todoApplication = (TodoApplication) getApplication();
        //Checks to see if user is logged in
        loginManager = todoApplication.getLoginManager();
        if(loginManager.isUserNotLogged()) {
            goToLogin();
            return;
        }

        //Name of XML file
        setContentView(R.layout.activity_todo_list);
        todoDao = ((TodoApplication) getApplication()).getTodoDao();

        /* Means that the C_Content will be mapped to the item_cb or the checkbox */
        String [] from = new String[] {TodoDao.C_CONTENT, TodoDao.C_DONE};
        int [] to = new int[]{R.id.item_cb, R.id.item_cb};

        simpleCursorAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.todo_item, null, from, to, 0);
        refreshCursor();
        SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if ( i == cursor.getColumnIndex(TodoDao.C_DONE)) {
                    CheckBox checkbox = (CheckBox) view;
                    //Need to extract from database to setCheckbox, downloads i'th column
                    int value = cursor.getInt(i);
                    checkbox.setChecked(value > 0);

                    //Means that we've handled the mapping on our own and dont want the default implementation
                    return true;
                }

                return false;
            }
        };
        //Automatically passes the adapter that will set to the list view
        //adapter = new ArrayAdapter<Todo>(getApplicationContext(), android.R.layout.simple_list_item_1);
        simpleCursorAdapter.setViewBinder(viewBinder);
        setListAdapter(simpleCursorAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.todo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            //Finish causes the activity to close, same as pressing the back button
            //Is an asynchronus call. So it will continue executing code until the method
            //is finished.
            loginManager.logout();
            goToLogin();
            return true;
        } else if (id == R.id.action_add) {
            Intent intent = new Intent(getApplicationContext(), AddToDoActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        } else if (id == R.id.action_refresh) {
            //No data needed to be passed so first param is Void, only able to assign a null
            //No need to inform about progress so Void is the 2nd param
            //Returns a Todo List objects
            AsyncTask<Void, Void, List<Todo>> asyncTask = new AsyncTask<Void, Void, List<Todo>>() {
                @Override
                protected List<Todo> doInBackground(Void... voids) {

                    //Needed to get data.
                    try {
                        String result = HttpUtils.getTodos(loginManager.getSessionToken());
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                        Log.d(TAG, "Result: " + result);
                        return Todo.fromJsonArray(jsonArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    setProgressBarIndeterminate(true);
                }

                @Override
                protected void onPostExecute(List<Todo> todos) {
                    setProgressBarVisibility(false);
                    super.onPostExecute(todos);
                    TodoDao todoDao = ((TodoApplication) getApplication()).getTodoDao();
                    for(Todo todo : todos) {
                        Log.d(TAG, todo.toString());
                        todoDao.insertOrUpdate(todo);
                    }
                    refreshCursor();
                }
            };
            asyncTask.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshCursor() {
        Cursor cursor = todoDao.query(loginManager.getUserId(), true);
        //Fills array adapter with data so it can be displayed.
        simpleCursorAdapter.changeCursor(cursor);
    }

    private void goToLogin() {
        finish();
        Intent intent  = new Intent(getApplicationContext(), Login_Activity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
               refreshCursor();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Request Cancelled!", Toast.LENGTH_LONG).show();
            }

        }
    }
}
