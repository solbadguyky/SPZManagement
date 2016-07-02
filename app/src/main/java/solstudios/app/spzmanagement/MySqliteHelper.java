package solstudios.app.spzmanagement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by solbadguyky on 7/1/16.
 */
public class MySqliteHelper {
    public static final String TAB = " MySqlHelper";

    public static final String JSON_OBJECT_EVENTS = "events";
    public static final String JSON_OBJECT_EVENT_ID = "event_id"; //integer
    public static final String JSON_OBJECT_EVENT_NAME = "event_name"; //string
    public static final String JSON_OBJECT_EVENT_TYPE = "event_type"; //string-enum


    public static JSONObject convertArrayToJSONObject(ArrayList<Channel.Event> events) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Channel.Event event : events) {
            try {
                JSONObject childObject = convertToJSONObject(event);
                jsonArray.put(childObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        jsonObject.put(JSON_OBJECT_EVENTS, jsonArray);

        return jsonObject;
    }

    public static JSONObject convertToJSONObject(Channel.Event event) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_OBJECT_EVENT_ID, event.getEventId());
        jsonObject.put(JSON_OBJECT_EVENT_NAME, event.getEventName());
        jsonObject.put(JSON_OBJECT_EVENT_TYPE, event.getEventType());
        return jsonObject;
    }

    public static ArrayList<Channel.Event> getEventsFromJSONObject(JSONObject jsonObject) throws JSONException {
        ArrayList<Channel.Event> eventArrayList = new ArrayList<>();
        if (jsonObject.has(JSON_OBJECT_EVENTS)) {
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_OBJECT_EVENTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject childObject = jsonArray.getJSONObject(i);
                new LogTask("getEventsFromJSONObject|event_id:" + childObject.getInt(JSON_OBJECT_EVENT_ID), TAB, LogTask.LOG_D);
                int eventid = childObject.getInt(JSON_OBJECT_EVENT_ID);
                String eventname = childObject.getString(JSON_OBJECT_EVENT_NAME);
                Object eventtype = childObject.get(JSON_OBJECT_EVENT_TYPE);
                Channel.Event event = new Channel.Event();
                event.setEventId(eventid);
                event.setEventName(eventname);
                event.setEventType(eventtype);

                eventArrayList.add(event);
            }
        }

        return eventArrayList;
    }
}
