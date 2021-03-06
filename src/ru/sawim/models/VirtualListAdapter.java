package ru.sawim.models;

import android.content.Context;
import android.graphics.Color;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.sawim.General;
import ru.sawim.R;
import ru.sawim.models.form.VirtualListItem;
import ru.sawim.Scheme;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Gerc
 * Date: 11.06.13
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public class VirtualListAdapter extends BaseAdapter {

    private Context baseContext;
    private List<VirtualListItem> items;
    private int selectedItem = -1;

    public VirtualListAdapter(Context context, List<VirtualListItem> items) {
        this.baseContext = context;
        this.items = items;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public VirtualListItem getItem(int i) {
        if (items.size() == 0) return null;
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setSelectedItem(int position) {
        selectedItem = position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        VirtualListItem element = getItem(i);
        if (convertView == null) {
            LayoutInflater inf = LayoutInflater.from(baseContext);
            convertView = inf.inflate(R.layout.virtual_list_item, null);
            holder = new ViewHolder();
            holder.descriptionLayout = (LinearLayout) convertView.findViewById(R.id.descriptionLayout);
            holder.labelView = (TextView) convertView.findViewById(R.id.label);
            holder.descView = (TextView) holder.descriptionLayout.findViewById(R.id.description);
            holder.imageView = (ImageView) holder.descriptionLayout.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (element == null) return convertView;

        holder.labelView.setTextColor(Scheme.getColor(Scheme.THEME_TEXT));
        holder.descView.setTextColor(Scheme.getColor(Scheme.THEME_TEXT));
        if (element.isHasLinks())
            holder.descView.setAutoLinkMask(Linkify.ALL);

        holder.labelView.setVisibility(TextView.GONE);
        holder.descView.setVisibility(TextView.GONE);
        holder.imageView.setVisibility(ImageView.GONE);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.descriptionLayout.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.setMargins(element.getMarginLeft(), 0, 0, 0);
            holder.descriptionLayout.setLayoutParams(layoutParams);
        }
        if (element.getLabel() != null) {
            holder.labelView.setVisibility(TextView.VISIBLE);
            if (element.getThemeTextLabel() > -1) {
                holder.labelView.setTextColor(Scheme.getColor(element.getThemeTextLabel()));
            }
            holder.labelView.setTextSize(General.getFontSize());
            holder.labelView.setText(element.getLabel());
        }
        if (element.getDescStr() != null) {
            holder.descView.setVisibility(TextView.VISIBLE);
            if (element.getThemeTextDesc() > -1) {
                holder.descView.setTextColor(Scheme.getColor(element.getThemeTextDesc()));
            }
            holder.descView.setTextSize(General.getFontSize());
            holder.descView.setText(element.getDescStr());
        } else {
            if (element.getDescSpan() != null) {
                holder.descView.setVisibility(TextView.VISIBLE);
                holder.descView.setTextSize(General.getFontSize());
                holder.descView.setText(element.getDescSpan());
            }
        }
        if (element.getImage() != null) {
            holder.imageView.setVisibility(ImageView.VISIBLE);
            holder.imageView.setImageDrawable(element.getImage());
        }
        LinearLayout activeItem = (LinearLayout) convertView;
        if (i == selectedItem && selectedItem != -1) {
            activeItem.setBackgroundColor(Scheme.getInversColor(Scheme.THEME_BACKGROUND));
        } else {
            activeItem.setBackgroundColor(Scheme.getColor(Scheme.THEME_BACKGROUND));
        }
        return convertView;
    }

    private static class ViewHolder {
        LinearLayout descriptionLayout;
        TextView labelView;
        TextView descView;
        ImageView imageView;

    }
}