package solstudios.app.spzmanagement;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class Channel implements Serializable {
    public static final String TAB = "Channel";

    private String channelName;
    private ArrayList<Event> events;

    public Channel() {

    }

    public Channel(String channelName) {
        this.channelName = channelName;
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

    void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return this.channelName;
    }

    public ArrayList<Event> getEvents() {
        return this.events;
    }

    public static class Event extends Channel {
        private String eventName;

        public Event(String eventName) {
            this.eventName = eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return this.eventName;
        }
    }
}
