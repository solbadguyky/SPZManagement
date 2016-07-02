package solstudios.app.spzmanagement;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by solbadguyky on 6/30/16.
 */
public class Channel implements Serializable {
    public static final String TAB = "Channel";

    private int channelId = -1;
    private String channelName;
    private ArrayList<Event> events;

    public Channel() {

    }

    public Channel(String channelName) {
        this.channelName = channelName;
    }

    public int getChannelId() {
        if (channelId == -1) {
            if (channelName != null && !channelName.isEmpty()) {
                return toHash(channelName);
            }
        }
        return this.channelId;
    }

    public void setChannelId(int id) {
        this.channelId = id;
    }

    public int toHash(String src) {
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

    boolean removeEventByName(String eventName) {
        if (!eventName.isEmpty() && eventName != null) {
            for (Event mEvent : events) {
                if (mEvent.getEventName().equals(eventName)) {
                    return events.remove(mEvent);
                }
            }
        }
        return false;
    }

    void setEventList(ArrayList<Event> events) {
        this.events = events;
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
        public static final String NO_SPECIFIC = "No Specific";
        private String eventName;
        private int eventId = -1;
        private Object eventType;

        public Event() {

        }

        public Event(String eventName) {
            this.eventName = eventName;
        }

        public int getEventId() {
            if (this.eventId == -1) {
                if (this.eventName != null && !this.eventName.isEmpty()) {
                    return toHash(eventName);
                } else {
                    return eventId;
                }
            } else
                return eventId;

        }

        public void setEventId(int id) {
            this.eventId = id;
        }

        public String getEventName() {
            return this.eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public Object getEventType() {
            if (this.eventType == null) {
                return NO_SPECIFIC;
            } else {
                return this.eventType;
            }
        }

        public void setEventType(Object type) {
            this.eventType = type;
        }
    }
}
