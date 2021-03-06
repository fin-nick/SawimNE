
package sawim.chat.message;

import sawim.comm.StringConvertor;
import sawim.util.JLocale;
import protocol.Contact;
import protocol.Protocol;

public class PlainMessage extends Message {

    private String text;
    private int messageId;
    private boolean offline;
    public static final String CMD_WAKEUP = "/wakeup";
    public static final String CMD_ME = "/me ";
    
    public static final int MESSAGE_LIMIT = 1024;

    
    public PlainMessage(String contactUin, Protocol protocol, long date, String text, boolean offline) {
        super(date, protocol, contactUin, true);
        if ('\n' == text.charAt(0)) {
            text = text.substring(1);
        }
        this.text = text;
        this.offline = offline;
    }

    
    public PlainMessage(Protocol protocol, Contact rcvr, long date, String text) {
        super(date, protocol, rcvr, false);
        this.text = StringConvertor.notNull(text);
        this.offline = false;
    }

    public boolean isOffline() {
        return offline;
    }

    
    public String getText() {
        return text;
    }

    public boolean isWakeUp() {
        return text.startsWith(PlainMessage.CMD_WAKEUP)
                && getRcvr().isSingleUserContact();
    }

    public String getProcessedText() {
        String messageText = text;
        if (isWakeUp()) {
            if (isIncoming()) {
                messageText = PlainMessage.CMD_ME + JLocale.getString("wake_you_up");
            } else {
                messageText = PlainMessage.CMD_ME + JLocale.getString("wake_up");
            }
        }
        return messageText;
    }

    public void setMessageId(int id) {
        messageId = id;
    }

    public int getMessageId() {
        return messageId;
    }
}

