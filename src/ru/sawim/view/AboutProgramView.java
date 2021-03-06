package ru.sawim.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.sawim.General;
import ru.sawim.R;
import ru.sawim.Scheme;

/**
 * Created with IntelliJ IDEA.
 * User: Gerc
 * Date: 29.08.13
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class AboutProgramView extends DialogFragment {

    public static final String TAG = "AboutProgramView";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.about_program);
        View v = inflater.inflate(R.layout.about_program, container, false);
        LinearLayout aboutLayout = (LinearLayout) v.findViewById(R.id.about_linear);
        aboutLayout.setBackgroundColor(Scheme.getColor(Scheme.THEME_BACKGROUND));
        TextView about = new TextView(getActivity());
        about.setTextSize(General.getFontSize());
        about.setText(R.string.about_program_desc);
        about.setTextColor(Scheme.getColor(Scheme.THEME_TEXT));
        about.setTypeface(Typeface.DEFAULT_BOLD);
        aboutLayout.addView(about);

        TextView version = new TextView(getActivity());
        version.setTextSize(General.getFontSize());
        version.setText(getString(R.string.version) + ": " + General.VERSION);
        version.setTextColor(Scheme.getColor(Scheme.THEME_TEXT));
        version.setTypeface(Typeface.DEFAULT);
        aboutLayout.addView(version);

        TextView author = new TextView(getActivity());
        author.setTextSize(General.getFontSize());
        author.setText(getString(R.string.author) + ": Gerc (Gorbachev Sergey)");
        author.setTextColor(Scheme.getColor(Scheme.THEME_TEXT));
        author.setTypeface(Typeface.DEFAULT);
        aboutLayout.addView(author);
        return v;
    }
}
