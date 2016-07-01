package solstudios.app.spzmanagement;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class Channel implements Serializable {
    public static final String TAB = "Channel";

    private long channelId = -1;
    private String channelName;
    private ArrayList<Event> events;

    public Channel() {

    }

    public Channel(String channelName) {
        this.channelName = channelName;
    }

    public long getChannelId() {
        if (channelId == -1) {
            if (channelName != null && !channelName.isEmpty()) {
                return toHash(channelName);
            }
        }
        return this.channelId;
    }

    public void setChannelId(long id) {
        this.channelId = id;
    }

    private long toHash(String src) {
        return src.hashCode();
    }

    boolean addEvent(Event event) {
        if (events == null) {
            events = new ArrayList<>();
        }
        return events.add(event);
    }

    boolean removeEvent(Event event) {
        if (events != null) {
            if (events.contains(event)) {
                return events.remove(event);
            }
        }
        return false;
    }

    public String getChannelName() {
        return this.channelName;
    }

    void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String[] getEventsArray() {
        ArrayList<String> eventsString = new ArrayList<>();
        for (Channel.Event event : getEvents()) {
            eventsString.add(event.getEventName().toString());
        }
        String[] eventArr = new String[eventsString.size()];
        eventsString.toArray(eventArr);
        return eventArr;
    }

    public String getEventsString() {
        String eventsString = new String();
        for (Channel.Event event : getEvents()) {
            eventsString += (event.getEventName() + ",");
        }
        return eventsString;
    }

    public ArrayList<Event> getEvents() {
        return this.events;
    }

    public static class Event extends Channel {
        private String eventName;

        public Event(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return this.eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }
}
