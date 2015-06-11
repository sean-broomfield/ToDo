package com.hello.seanbroomfield.todo;

import java.io.Serializable;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Todo implements Serializable {


    /**
     * Format daty zwracany przez Parse.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * Nazwy kluczy w JSON, używane w komunikacji z Parse.
     */
    public static final String DONE_KEY = "done";
    public static final String CONTENT_KEY = "content";
    public static final String OBJECT_ID_KEY = "objectId";
    public static final String UPDATED_AT_KEY = "updatedAt";
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String USER_KEY = "user";
    public static final String USER_ID_KEY = "userId";

    /**
     * Unikalne id użytkownika, który utworzył zadanie. Nadawane przez Parse.
     */
    public String userId;
    /**
     * Trześć zadania.
     */
    public String content;
    /**
     * Flaga zakończenia zadania.
     */
    public boolean done;

    /**
     * Unikalne id zadania. Nadawane przez Parse.
     */
    public String objectId;
    /**
     * Data ostatniej aktualizacji zadania.
     */
    public Date updatedAt;
     /**
     * Data utworzenia zadania.
     */
    public Date createdAt;


    /**
     * @param object
     * @return Obiekt modelowy {@link Todo}.
     * @throws JSONException
     * @throws ParseException
     */
    public static Todo fromJsonObject(JSONObject object) throws JSONException, ParseException {
        Todo todo = new Todo();
        todo.objectId = object.getString(OBJECT_ID_KEY);
        todo.content = object.getString(CONTENT_KEY);
        todo.done = object.getBoolean(DONE_KEY);
        todo.createdAt = DATE_FORMAT.parse(object.getString(CREATED_AT_KEY));
        //dla nowo utworzonego obiektu updatedAt = createdAt
        if(object.has(UPDATED_AT_KEY)) {
            todo.updatedAt = DATE_FORMAT.parse(object.getString(UPDATED_AT_KEY));
        } else {
            todo.updatedAt = DATE_FORMAT.parse(object.getString(CREATED_AT_KEY));
        }
        todo.userId = object.getJSONObject(USER_KEY).getString(OBJECT_ID_KEY);

        return todo;
    }

    /**
     * @param jsonArray
     * @return
     * @throws JSONException
     * @throws ParseException
     */
    public static List<Todo> fromJsonArray(JSONArray jsonArray) throws JSONException, ParseException {
        List<Todo> todos = new ArrayList<Todo>();
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            Todo todo = Todo.fromJsonObject(object);
            todos.add(todo);
        }
        return todos;
    }

    /**
     * @return
     * @throws JSONException
     */
    public String toJsonStringForPost() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(CONTENT_KEY, content);
        object.put(DONE_KEY, done);
        object.put(USER_ID_KEY, userId);
        return object.toString();
    }

    /**
     * @return
     * @throws JSONException
     */
    public String toJsonStringForPut() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("done", done);
        return jsonObject.toString();
    }


    @Override
    public String toString() {
        return "Todo [content=" + content + ", done=" + done + "]";
    }


    public String toJsonString() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CONTENT_KEY, content);
        jsonObject.put(DONE_KEY, done);
        jsonObject.put(USER_ID_KEY, userId);
        return jsonObject.toString();
    }
}
