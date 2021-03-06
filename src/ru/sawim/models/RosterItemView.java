package ru.sawim.models;

import DrawControls.icons.Icon;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import protocol.Contact;
import protocol.Group;
import protocol.Protocol;
import protocol.XStatusInfo;
import ru.sawim.General;
import ru.sawim.Scheme;
import sawim.chat.ChatHistory;
import sawim.chat.message.Message;
import sawim.modules.tracking.Tracking;
import sawim.roster.Roster;

/**
 * Created with IntelliJ IDEA.
 * User: Gerc
 * Date: 03.08.13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class RosterItemView extends RelativeLayout {

    public TextView itemName;
    public TextView itemDescriptionText;
    public ImageView itemFirstImage;
    public ImageView itemSecondImage;
    public ImageView itemThirdImage;
    public ImageView itemFourthImage;
    public ImageView itemFifthImage;

    public RosterItemView(Context context) {
        super(context);
        setPadding(15, 15, 15, 15);
        itemFirstImage = new ImageView(context);
        itemFifthImage = new ImageView(context);
        itemSecondImage = new ImageView(context);
        itemThirdImage = new ImageView(context);
        itemName = new TextView(context);
        itemDescriptionText = new TextView(context);
        itemFourthImage = new ImageView(context);
        build();
    }

    private void build() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = false;
        lp.addRule(CENTER_VERTICAL);
        itemFirstImage.setId(1);
        addView(itemFirstImage, lp);

        lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(CENTER_VERTICAL);
        lp.addRule(RIGHT_OF, itemFirstImage.getId());
        itemSecondImage.setPadding(6, 0, 0, 0);
        itemSecondImage.setId(2);
        addView(itemSecondImage, lp);

        lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(CENTER_VERTICAL);
        lp.addRule(RIGHT_OF, itemSecondImage.getId());
        itemThirdImage.setPadding(6, 0, 0, 0);
        itemThirdImage.setId(3);
        addView(itemThirdImage, lp);

        lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(RIGHT_OF, itemThirdImage.getId());
        lp.addRule(ALIGN_TOP, itemFifthImage.getId());
        lp.addRule(ALIGN_PARENT_TOP);
        itemName.setSingleLine(true);
        itemName.setPadding(5, 5, 5, 5);
        itemName.setId(4);
        addView(itemName, lp);

        lp = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(RIGHT_OF, itemThirdImage.getId());
        lp.addRule(BELOW, itemName.getId());
        itemDescriptionText.setSingleLine(true);
        itemDescriptionText.setPadding(5, 0, 0, 0);
        itemDescriptionText.setId(5);
        addView(itemDescriptionText, lp);

        lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.addRule(ALIGN_BOTTOM, itemDescriptionText.getId());
        lp.addRule(ALIGN_TOP, itemName.getId());
        itemFifthImage.setId(6);
        addView(itemFifthImage, lp);

        lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(LEFT_OF, itemFifthImage.getId());
        lp.addRule(ALIGN_BOTTOM, itemDescriptionText.getId());
        lp.addRule(ALIGN_TOP, itemName.getId());
        itemFourthImage.setId(7);
        addView(itemFourthImage, lp);
    }

    void populateFromGroup(Group g) {
        itemName.setTextSize(General.getFontSize());
        itemName.setText(g.getText());
        itemName.setTextColor(Scheme.getColor(Scheme.THEME_GROUP));
        itemName.setTypeface(Typeface.DEFAULT);

        Icon icGroup = g.getLeftIcon(null);
        if (icGroup == null)
            itemFirstImage.setVisibility(ImageView.GONE);
        else {
            itemFirstImage.setVisibility(ImageView.VISIBLE);
            itemFirstImage.setImageDrawable(icGroup.getImage());
        }

        itemDescriptionText.setVisibility(TextView.GONE);
        itemThirdImage.setVisibility(ImageView.GONE);
        itemSecondImage.setVisibility(ImageView.GONE);
        itemFifthImage.setVisibility(ImageView.GONE);

        Icon messIcon = ChatHistory.instance.getUnreadMessageIcon(g.getContacts());
        ImageView messImage = itemFourthImage;
        if (g.isExpanded() || messIcon == null) {
            messImage.setVisibility(ImageView.GONE);
        } else {
            messImage.setVisibility(ImageView.VISIBLE);
            messImage.setImageDrawable(messIcon.getImage());
        }
    }

    void populateFromContact(Roster roster, Protocol p, Contact item) {
        itemName.setTextSize(General.getFontSize());
        if (item.subcontactsS() == 0)
            itemName.setText(item.getText());
        else
            itemName.setText(item.getText() + " (" + item.subcontactsS() + ")");
        itemName.setTextColor(Scheme.getColor(item.getTextTheme()));
        itemName.setTypeface(item.hasChat() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        itemDescriptionText.setTextSize(General.getFontSize() - 2);
        if (General.showStatusLine) {
            itemDescriptionText.setVisibility(TextView.VISIBLE);
            itemDescriptionText.setText(roster.getStatusMessage(item));
            itemDescriptionText.setTextColor(Scheme.getColor(Scheme.THEME_CONTACT_STATUS));
        } else {
            itemDescriptionText.setVisibility(TextView.GONE);
        }

        Icon icStatus = item.getLeftIcon(p);
        if (icStatus == null) {
            itemFirstImage.setVisibility(ImageView.GONE);
        } else {
            itemFirstImage.setVisibility(ImageView.VISIBLE);
            itemFirstImage.setImageDrawable(icStatus.getImage());
        }
        if (item.isTyping()) {
            itemFirstImage.setImageDrawable(Message.msgIcons.iconAt(Message.ICON_TYPE).getImage());
        } else {
            Icon icMess = Message.msgIcons.iconAt(item.getUnreadMessageIcon());
            if (icMess != null)
                itemFirstImage.setImageDrawable(icMess.getImage());
        }

        if (item.getXStatusIndex() == XStatusInfo.XSTATUS_NONE) {
            itemSecondImage.setVisibility(ImageView.GONE);
        } else {
            itemSecondImage.setVisibility(ImageView.VISIBLE);
            itemSecondImage.setImageDrawable(p.getXStatusInfo().getIcon(item.getXStatusIndex()).getImage());
        }

        if (!item.isTemp()) {
            Icon icAuth = item.authIcon.iconAt(0);
            itemThirdImage.setVisibility(ImageView.VISIBLE);
            if (item.isAuth()) {
                int privacyList = -1;
                if (item.inIgnoreList()) {
                    privacyList = 0;
                } else if (item.inInvisibleList()) {
                    privacyList = 1;
                } else if (item.inVisibleList()) {
                    privacyList = 2;
                }
                if (privacyList != -1)
                    itemThirdImage.setImageDrawable(item.serverListsIcons.iconAt(privacyList).getImage());
                else
                    itemThirdImage.setVisibility(ImageView.GONE);
            } else {
                itemThirdImage.setImageDrawable(icAuth.getImage());
            }
        }

        Icon icClient = (null != p.clientInfo) ? p.clientInfo.getIcon(item.clientIndex) : null;
        if (icClient != null && !General.hideIconsClient) {
            itemFourthImage.setVisibility(ImageView.VISIBLE);
            itemFourthImage.setImageDrawable(icClient.getImage());
        } else {
            itemFourthImage.setVisibility(ImageView.GONE);
        }

        String id = item.getUserId();
        if (Tracking.isTrackingEvent(id, Tracking.GLOBAL) == Tracking.TRUE) {
            itemFifthImage.setVisibility(ImageView.VISIBLE);
            itemFifthImage.setImageDrawable(Tracking.getTrackIcon(id).getImage());
        } else {
            itemFifthImage.setVisibility(ImageView.GONE);
        }
    }
}
