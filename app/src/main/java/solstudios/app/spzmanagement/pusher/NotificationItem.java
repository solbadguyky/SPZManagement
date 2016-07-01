package solstudios.app.spzmanagement.pusher;

import java.io.Serializable;

public class NotificationItem implements Serializable {

    public static final String TAB = "NotificationItem";
    public static final int PRIOR_LOW = 1;
    public static final int PRIOR_NORMAL = 2;
    public static final int PRIOR_HIGH = 3;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Item item;
    private Object tag;

    public NotificationItem(Item item) {
        this.item = item;
    }


    public void setTag(String tag) {
        this.tag = tag;
    }

    public Object getTag() {
        if (tag == null) {
            return "notag";
        } else {
            return tag;
        }
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Item getItem() {
        return this.item;
    }

    public class Item implements Serializable {
        public long itemid;
        public String title, summary, user, url;
    }

}
