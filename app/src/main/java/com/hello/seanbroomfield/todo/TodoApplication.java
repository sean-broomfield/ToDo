package com.hello.seanbroomfield.todo;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by seanbroomfield on 6/9/15.
 */
public class TodoApplication extends Application {

    private LoginManager loginManager;
    private TodoDao todoDao;

    public LoginManager getLoginManager() {
        return loginManager;
    }

    public TodoDao getTodoDao () {
        return todoDao;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loginManager = new LoginManager();
        todoDao = new TodoDao(getApplicationContext());
    }

    class LoginManager {

        public String getSessionToken() {
            return sessionToken;
        }

        private String sessionToken;

        public String getUserId() {
            return userId;
        }

        private String userId;

        public static final String SESSION_TOKEN = "sessionToken";
        public static final String OBJECT_ID = "objectId";
        private SharedPreferences sharedPreferences;

        public LoginManager () {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //SESSION TOKEN is the key youre looking for and null is returned if not there.
            this.sessionToken = sharedPreferences.getString(SESSION_TOKEN, null);
            this.userId = sharedPreferences.getString(OBJECT_ID, null);
        }

        //Checks to see if user is logged in
        public boolean isUserNotLogged() {
            return TextUtils.isEmpty(sessionToken) || TextUtils.isEmpty(userId);
        }

        public void saveLogin(String sessionToken, String userId) {
            this.sessionToken = sessionToken;
            this.userId = userId;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString(SESSION_TOKEN, sessionToken);
            edit.putString(OBJECT_ID, userId);
            edit.apply();
        }

        public void logout () {
            sessionToken = null;
            sessionToken = null;

            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.remove(SESSION_TOKEN);
            edit.remove(OBJECT_ID);
            edit.apply();
        }
    }
}
