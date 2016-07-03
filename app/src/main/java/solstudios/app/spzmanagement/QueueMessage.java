package solstudios.app.spzmanagement;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by solbadguyky on 7/3/16.
 */

public class QueueMessage {
    public static final String TAB = "QueueMessage";

    private String message;
    private MessageAction messageAction;

    public QueueMessage() {

    }

    public QueueMessage(String message) {
        this.message = message;
    }

    public QueueMessage(String message, MessageAction action) {
        this.message = message;
        this.messageAction = action;
    }

    public static Snackbar createSnackBar(View view, QueueMessage queueMessage) {
        Snackbar snackbar = Snackbar
                .make(view, queueMessage.getMessage(), Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(Color.RED);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        if (queueMessage.getMessageAction() != null) {
            snackbar.setAction(queueMessage.getMessageAction().actionName, queueMessage.getMessageAction().action);
        }

        return snackbar;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageAction getMessageAction() {
        return this.messageAction;
    }

    public void setMessageAction(MessageAction messageAction) {
        this.messageAction = messageAction;
    }

    public static class MessageAction {
        public static final String TAB = "MessageAction";
        public String actionName;
        public View.OnClickListener action;

        public MessageAction() {

        }

        public MessageAction(String actionName, View.OnClickListener onClickListener) {
            this.actionName = actionName;
            this.action = onClickListener;
        }
    }

    public static class SnackbarManager extends Snackbar.Callback {
        ///những snackbar đang xuất hiện
        Queue<Snackbar> snackbarsWaiting;
        Snackbar currentSnackbar;
        private boolean onShowing = false;
        ///những snackbar đang chờ
        //Queue<Snackbar> snackbarsHolding;

        public SnackbarManager() {
            snackbarsWaiting = new LinkedList<>();
            //snackbarsHolding = new LinkedList<>();
        }

        public void clear() {
            if (snackbarsWaiting != null) {
                snackbarsWaiting.clear();
            }
        }


        /**
         * Thêm snackbar vào hàng chờ, nều hàng chờ trống thì show() luôn
         *
         * @param snackbar
         */
        public void addToQueue(Snackbar snackbar) {
            if (snackbar.getDuration() == Snackbar.LENGTH_INDEFINITE)
                snackbar.setDuration(Snackbar.LENGTH_LONG);
            snackbar.setCallback(this);
            if (snackbarsWaiting.size() > 0) {
                snackbarsWaiting.add(snackbar);
            } else {
                setFirstShown(snackbar);
            }

        }

        private void setFirstShown(Snackbar snackbar) {
            currentSnackbar = snackbar;
            snackbarsWaiting.add(snackbar);
            currentSnackbar.show();
        }

        public void show() {
            onShowing = true;
            currentSnackbar = snackbarsWaiting.peek();
            currentSnackbar.show();
        }

        @Override
        public void onShown(Snackbar snackbar) {
            super.onShown(snackbar);
        }

        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            super.onDismissed(snackbar, event);
            //snackbarsWaiting.remove(snackbar);
            snackbarsWaiting.poll();
            if (snackbarsWaiting.size() > 0) show();
        }
    }
}
