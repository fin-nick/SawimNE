package protocol.jabber;

import android.view.ContextMenu;
import android.view.Menu;
import protocol.Contact;
import protocol.ContactMenu;
import protocol.Protocol;
import protocol.StatusInfo;
import ru.sawim.R;
import ru.sawim.SawimApplication;
import ru.sawim.models.RosterAdapter;
import ru.sawim.view.menu.MyMenu;
import sawim.Options;
import sawim.chat.message.SystemNotice;
import sawim.roster.Roster;
import sawim.comm.StringConvertor;
import sawim.modules.tracking.Tracking;
import sawim.util.JLocale;

import java.util.Vector;

public class JabberServiceContact extends JabberContact {

    public static final byte AFFILIATION_NONE = (byte) 0;
    public static final byte AFFILIATION_MEMBER = (byte) 1;
    public static final byte AFFILIATION_ADMIN = (byte) 2;
    public static final byte AFFILIATION_OWNER = (byte) 3;
    public static final byte ROLE_VISITOR = (byte) -1;
    public static final byte ROLE_PARTICIPANT = (byte) 0;
    public static final byte ROLE_MODERATOR = (byte) 1;

    public boolean warning = false;
    private boolean isPrivate;
    private boolean isConference;
    private boolean isGate;
    private boolean autojoin;
    private String password;
    private String myNick;
    private String baseMyNick;

    public JabberServiceContact(String jid, String name) {
        super(jid, name);

        isGate = Jid.isGate(jid);
        if (isGate) {
            return;
        }

        isPrivate = (-1 != jid.indexOf('/'));
        if (isPrivate) {
            String resource = Jid.getResource(jid, "");
            setName(resource + "@" + Jid.getNick(jid));
            return;
        }

        isConference = Jid.isConference(jid);
        if (isConference) {
            setMyName("_");
            if (jid.equals(name)) {
                setName(Jid.getNick(jid));
            }
        }
    }

    public static final int getAffiliationName(byte index) {
        switch (index) {
            case AFFILIATION_OWNER:
                return 0;
            case AFFILIATION_ADMIN:
                return 1;
            case AFFILIATION_MEMBER:
                return 2;
            case AFFILIATION_NONE:
                return 3;
        }
        return 0;
    };

    public boolean isAutoJoin() {
        return autojoin;
    }

    public void setAutoJoin(boolean auto) {
        autojoin = auto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public void setXStatus(String id, String text) {
    }

    public boolean isSingleUserContact() {
        return isPrivate || isGate;
    }

    public boolean isConference() {
        return isConference;
    }

    public boolean isVisibleInContactList() {
        if (Roster.getInstance().getCurrPage() != RosterAdapter.ONLINE_CONTACTS
        /*!Options.getBoolean(Options.OPTION_CL_HIDE_OFFLINE)*/)
            return true;
        return isConference() || isGate ? super.isVisibleInContactList() : true;
    }

    public String getMyName() {
        return (isConference || isPrivate) ? myNick : null;
    }

    public final void setMyName(String nick) {
        if (!StringConvertor.isEmpty(nick)) {
            myNick = nick;
            if (!isOnline()) {
                baseMyNick = myNick;
            }
        }
    }

    void doJoining() {
        setStatus(StatusInfo.STATUS_AWAY, "");
    }

    public byte isPresence() {
        if (Tracking.isTracking(getUserId(), Tracking.GLOBAL) == Tracking.TRUE) {
            if (Tracking.beginTrackActionItem(this, Tracking.ACTION_PRESENCE) == Tracking.TRUE) {
                return Tracking.TRUE;
            }
        }
        return Tracking.FALSE;
    }

    public void addPresence(Jabber jabber, String nick, String text) {
        if (isPresence() != (byte) 1)
            return;
        jabber.getChat(this).addPresence(new SystemNotice(jabber,
                SystemNotice.SYS_NOTICE_PRESENCE, getUserId(), nick, text));
    }

    void nickChainged(Jabber jabber, String oldNick, String newNick) {
        if (isConference) {
            if (baseMyNick.equals(oldNick)) {
                setMyName(newNick);
                baseMyNick = newNick;
            }
            String jid = Jid.realJidToSawimJid(getUserId() + "/" + oldNick);
            JabberServiceContact c = (JabberServiceContact) jabber.getItemByUIN(jid);
            if (null != c) {
                c.nickChainged(jabber, oldNick, newNick);
            }
        } else if (isPrivate) {
            userId = Jid.getBareJid(userId) + "/" + newNick;
            setName(newNick + "@" + Jid.getNick(getUserId()));
            setOfflineStatus();
        }
    }

    void nickOnline(Jabber jabber, String nick, String tempRang, String tempRole, String role_Xstatus) {
        if (hasChat()) {
            jabber.getChat(this).setWritable(canWrite());
        }
        if (myNick.equals(nick)) {
            JabberContact.SubContact c = getContact(getMyName());
            setStatus(c.status, getStatusText());
            jabber.addRejoin(getUserId());
        }
        SubContact sc = getExistSubContact(nick);
        if (null != sc) {
            if (isPresence() == (byte) 1) {
                StringBuffer prsnsText = new StringBuffer(0);
                prsnsText.append(": ").append(jabber.getStatusInfo().getName(sc.status)).append(" ");
                prsnsText.append(tempRang).append("/").append(tempRole).append(" ").append(role_Xstatus);
                addPresence(jabber, nick, prsnsText.toString());
            }
        }
    }

    void nickError(Jabber jabber, String nick, int code, String reasone) {
        boolean isConnected = (StatusInfo.STATUS_ONLINE == getStatusIndex());
        if (409 == code) {
            if (!StringConvertor.isEmpty(reasone)) {
                jabber.addMessage(new SystemNotice(jabber,
                        SystemNotice.SYS_NOTICE_ERROR, getUserId(), reasone));
            }
            if (!myNick.equals(baseMyNick)) {
                myNick = baseMyNick;
                return;
            }
        } else {
            jabber.addMessage(new SystemNotice(jabber,
                    SystemNotice.SYS_NOTICE_ERROR, getUserId(), reasone));
        }
        if (myNick.equals(nick)) {
            if (isConnected) {
                jabber.leave(this);
            } else {
                nickOffline(jabber, nick, 0, "", "");
            }
        } else {
            nickOffline(jabber, nick, 0, "", "");
        }
    }

    private void playSoundEye() {
        String id = getUserId();
        if (Tracking.isTrackingEvent(id, Tracking.GLOBAL) == Tracking.TRUE) {
            if (Tracking.isTracking(id, Tracking.EVENT_ENTER) == Tracking.TRUE) {
                if (Tracking.beginTrackActionItem(this, Tracking.SOUND_EYE) == Tracking.TRUE) {

                }
            }
        } else if (Tracking.isTracking(id, Tracking.EVENT_ENTER) == Tracking.FALSE) {
        }
    }

    void nickOffline(Jabber jabber, String nick, int code, String reasone, String presenceUnavailable) {
        StringBuffer textPresence = new StringBuffer();
        textPresence.append(": ").append(jabber.getStatusInfo().getName(StatusInfo.STATUS_OFFLINE)).append(" ").append(presenceUnavailable);
        if (getMyName().equals(nick)) {
            if (isOnline()) {
                jabber.removeRejoin(getUserId());
            }
            String text = null;
            if (301 == code) {
                text = "you_was_baned";
            } else if (307 == code) {
                text = "you_was_kicked";
            } else if (404 == code) {
                text = "error";
            }
            if (null != text) {
                text = JLocale.getString(text);
                if (!StringConvertor.isEmpty(reasone)) {
                    text += " (" + reasone + ")";
                }
                text += '.';
                playSoundEye();
                textPresence.append(text).append(" ").append(reasone);
                sawim.modules.MagicEye.addAction(getProtocol(), getUserId(), nick + " " + text, reasone);
                jabber.addMessage(new SystemNotice(jabber,
                        SystemNotice.SYS_NOTICE_ERROR, getUserId(), text));
            }
            for (int i = 0; i < subcontacts.size(); ++i) {
                ((SubContact) subcontacts.elementAt(i)).status = StatusInfo.STATUS_OFFLINE;
            }
            String startUin = getUserId() + '/';
            Vector contactList = jabber.getContactItems();
            for (int i = contactList.size() - 1; 0 <= i; --i) {
                Contact c = (Contact) contactList.elementAt(i);
                if (c.getUserId().startsWith(startUin)) {
                    c.setOfflineStatus();
                }
            }
            setOfflineStatus();
            jabber.ui_changeContactStatus(this);
        } else {
            String event = null;
            if (301 == code) {
                event = "was_baned";
                playSoundEye();
            } else if (307 == code) {
                event = "was_kicked";
                playSoundEye();
            }
            if (null != event) {
                event = JLocale.getString(event);
                textPresence.append(event).append(" ").append(reasone);
                sawim.modules.MagicEye.addAction(jabber, getUserId(), nick + " " + event, reasone);
            }
        }
        addPresence(jabber, nick, textPresence.toString());
        if (hasChat()) {
            jabber.getChat(this).setWritable(canWrite());
        }
    }

    String getRealJid(String nick) {
        SubContact sc = getExistSubContact(nick);
        return (null == sc) ? null : sc.realJid;
    }

    public final String getDefaultGroupName() {
        if (isConference) {
            return JLocale.getString(Jabber.CONFERENCE_GROUP);
        }
        if (isGate) {
            return JLocale.getString(Jabber.GATE_GROUP);
        }
        return null;
    }

    public Contact getPrivateContact(String nick) {
        String jid = Jid.realJidToSawimJid(getUserId() + "/" + nick);
        return getProtocol().createTempContact(jid);
    }

    public void setSubject(String subject) {
        JabberContact.SubContact c = getContact(getMyName());
        if (isConference && isOnline()) {
            setStatus(c.status, subject);
        }
    }

    public JabberContact.SubContact getContact(String nick) {
        if (StringConvertor.isEmpty(nick)) {
            return null;
        }
        for (int i = 0; i < subcontacts.size(); ++i) {
            JabberContact.SubContact contact = (JabberContact.SubContact) subcontacts.elementAt(i);
            if (nick.equals(contact.resource)) {
                return contact;
            }
        }
        return null;
    }

    protected void initContextMenu(Protocol protocol, ContextMenu contactMenu) {
        if (isGate) {
            if (isOnline()) {
                contactMenu.add(Menu.NONE, ContactMenu.GATE_DISCONNECT, Menu.NONE, R.string.disconnect);
                contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_ADHOC, Menu.NONE, R.string.adhoc);
            } else {
                contactMenu.add(Menu.NONE, ContactMenu.GATE_CONNECT, Menu.NONE, R.string.connect);
                contactMenu.add(Menu.NONE, ContactMenu.GATE_REGISTER, Menu.NONE, R.string.register);
                contactMenu.add(Menu.NONE, ContactMenu.GATE_UNREGISTER, Menu.NONE, R.string.unregister);
            }
            contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_TRACK_CONF, Menu.NONE, R.string.extra_settings);
        }
        if (isConference) {
            if (isOnline()) {
                contactMenu.add(Menu.NONE, ContactMenu.CONFERENCE_DISCONNECT, Menu.NONE, R.string.leave_chat);
            } else {
                contactMenu.add(Menu.NONE, ContactMenu.CONFERENCE_CONNECT, Menu.NONE, R.string.connect);
            }
            if (!isOnline()) {//
                contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_USERS_LIST, Menu.NONE, R.string.list_of_users);
            }
            contactMenu.add(Menu.NONE, ContactMenu.CONFERENCE_OPTIONS, Menu.NONE, R.string.options);
            if (isOnline()) {
                SubContact my = getContact(getMyName());
                if (null != my) {
                    if (AFFILIATION_OWNER == my.priorityA) {
                        contactMenu.add(Menu.NONE, ContactMenu.CONFERENCE_OWNER_OPTIONS, Menu.NONE, R.string.owner_options);
                    }
                    if (AFFILIATION_ADMIN <= my.priorityA) {
                        contactMenu.add(Menu.NONE, ContactMenu.CONFERENCE_AFFILIATION_LIST, Menu.NONE, R.string.conf_aff_list);
                        contactMenu.add(Menu.NONE, ContactMenu.COMMAND_TITLE, Menu.NONE, R.string.set_theme_conference);
                    }
                }
            }
            contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_TRACK_CONF, Menu.NONE, R.string.extra_settings);
        }
        if ((isOnline() && isConference && canWrite()) || isPrivate) {
            addChatItems(contactMenu);
        }
        if (isPrivate || isGate) {
            contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_USER_INFO, Menu.NONE, R.string.info);
        }
        if (!isPrivate) {
            contactMenu.add(Menu.NONE, ContactMenu.USER_MANAGE_CONTACT, Menu.NONE, R.string.manage);
            if (!isTemp()) {
                contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_HISTORY, Menu.NONE, R.string.history);
            }
        }
        if (isOnline()) {
            contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_STATUSES, Menu.NONE, R.string.user_statuses);
        }
        if (isPrivate) {
            contactMenu.add(Menu.NONE, ContactMenu.USER_MANAGE_CONTACT, Menu.NONE, R.string.manage);
        }
        if (hasChat()) {
            contactMenu.add(Menu.NONE, ContactMenu.USER_MENU_CLOSE_CHAT, Menu.NONE, R.string.close);
        }
    }

    protected void initManageContactMenu(Protocol protocol, MyMenu menu) {
        if (protocol.isConnected()) {
            if (isOnline() && isPrivate) {
                menu.add(SawimApplication.getContext().getString(R.string.adhoc), ContactMenu.USER_MENU_ADHOC);
            }
            if (isConference && isTemp()) {
                menu.add(SawimApplication.getContext().getString(R.string.add_user), ContactMenu.CONFERENCE_ADD);
            }
            if (isGate) {
                if ((1 < protocol.getGroupItems().size()) && !isTemp()) {
                    menu.add(SawimApplication.getContext().getString(R.string.move_to_group), ContactMenu.USER_MENU_MOVE);
                }
                if (!isAuth()) {
                    menu.add(SawimApplication.getContext().getString(R.string.requauth), ContactMenu.USER_MENU_REQU_AUTH);
                }
                if (!protocol.getGroupItems().isEmpty()) {
                    menu.add(SawimApplication.getContext().getString(R.string.add_user), ContactMenu.GATE_ADD);
                }
                menu.add(SawimApplication.getContext().getString(R.string.remove_me), ContactMenu.USER_MENU_REMOVE_ME);
            }
        }
        if (protocol.inContactList(this)) {
            if (!isPrivate) {
                menu.add(SawimApplication.getContext().getString(R.string.rename), ContactMenu.USER_MENU_RENAME);
            }
            menu.add(SawimApplication.getContext().getString(R.string.remove), ContactMenu.USER_MENU_USER_REMOVE);
        }
    }

    public String getNick(String resource) {
        SubContact c = getExistSubContact(resource);
        return (null == c) ? resource : c.resource;
    }

    boolean canWrite() {
        if (isOnline()) {
            if (isConference) {
                SubContact sc = getExistSubContact(getMyName());
                return (null != sc) && (ROLE_VISITOR != sc.priority);
            }
            return true;
        }
        return !isPrivate;
    }

    public void activate(Protocol p) {
        if (isOnline() || isPrivate || hasChat()) {
            super.activate(p);

        } else if (isConference && p.isConnected()) {
            new ContactMenu(p, this).doAction(ContactMenu.CONFERENCE_CONNECT);
        }
    }

    public boolean hasHistory() {
        return Options.getBoolean(Options.OPTION_HISTORY) && Tracking.isTracking(getUserId(), Tracking.EVENT_ENTER) == Tracking.TRUE;
    }

    public final void setPrivateContactStatus(JabberServiceContact conf) {
        String nick = Jid.getResource(getUserId(), "");
        SubContact sc = (null == conf) ? null : conf.getExistSubContact(nick);
        if (null == sc) {
            setOfflineStatus();
            setClient(JabberClient.CLIENT_NONE, null);
        } else {
            if (subcontacts.isEmpty()) {
                subcontacts.addElement(sc);
            } else {
                subcontacts.setElementAt(sc, 0);
            }
            setStatus(sc.status, sc.statusText);
            setClient(sc.client, null);
        }
    }
}