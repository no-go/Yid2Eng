package de.digisocken.yid2eng;

import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntryAdapter extends BaseAdapter {
    public String squery;
    public ArrayList<DicEntry> dicEntries = new ArrayList<>();
    Activity activity;

    EntryAdapter(Activity context) {
        super();
        squery = null;
        activity = context;
    }

    public void addItem(DicEntry item) {
        dicEntries.add(item);
    }

    public void clear() {
        dicEntries.clear();
    }

    public void filter(String query, EntryAdapter entryAdapter) {
        clear();
        squery = query;
        query = query.toLowerCase();
        MainActivity.data_total = entryAdapter.getCount();
        MainActivity.data_line = 0;

        for (int i = 0; i < MainActivity.data_total; i++) {
            DicEntry dicEntry = (DicEntry) entryAdapter.getItem(i);
            if (dicEntry.title.toLowerCase().contains(query) || dicEntry.body.toLowerCase().contains(query) || dicEntry.yiddish.contains(query)) {
                addItem(dicEntry);
                MainActivity.data_line++;
            }
        }
        sort();
        MainActivity.data_line = 0;
    }

    @Override
    public int getCount() {
        return dicEntries.size();
    }

    @Override
    public Object getItem(int i) {
        return dicEntries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.entry_line, viewGroup, false);
        TextView tt = (TextView) view.findViewById(R.id.line_title);
        TextView t2 = (TextView) view.findViewById(R.id.line_title2);
        TextView tb = (TextView) view.findViewById(R.id.line_body);
        if (squery != null) {
            tt.setText(highlight(squery, dicEntries.get(i).title));
            t2.setText(highlight(squery, dicEntries.get(i).yiddish));
            tb.setText(highlight(squery, dicEntries.get(i).body));
        } else {
            tt.setText(dicEntries.get(i).title);
            t2.setText(dicEntries.get(i).yiddish);
            tb.setText(dicEntries.get(i).body);
        }
        if (i%2==0) {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.evenCol
            ));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.oddCol
            ));
        }
        return view;
    }

    public Spanned highlight(String key, String msg) {
        msg = msg.replaceAll(
                "((?i)"+key+")",
                "<b><font color='"
                        + ContextCompat.getColor(activity.getApplicationContext(),
                        R.color.colorAccent) +
                        "'>$1</font></b>"
        );
        return fromHtml(msg);
    }

    static public Spanned fromHtml(String str) {
        Spanned sp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sp = Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT, null, null);
        } else {
            sp = Html.fromHtml(str, null, null);
        }
        return sp;
    }

    public void sort() {
        //final String[] from = activity.getString(R.string.sortReplaceFrom).split(activity.getString(R.string.columnsplit));
        //final String[] to = activity.getString(R.string.sortReplaceTo).split(activity.getString(R.string.columnsplit));

        Collections.sort(dicEntries, new Comparator<DicEntry>() {
            @Override
            public int compare(DicEntry f1, DicEntry f2) {
                String s1 = f1.title;
                String s2 = f2.title;
                /*
                for (int i =0; i < from.length; i++) {
                    s1 = s1.replace(from[i], to[i]);
                    s2 = s2.replace(from[i], to[i]);
                }
                */
                return s1.compareTo(s2);
            }
        });
    }
}