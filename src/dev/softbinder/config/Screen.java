package dev.softbinder.config;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A numbered list screen: rows are numbered in order, and pressing a digit key
 * activates the matching row.
 */
abstract class Screen extends Activity {
    final ExecutorService io = Executors.newSingleThreadExecutor();
    private LinearLayout list;
    private final List<Runnable> actions = new ArrayList<Runnable>();

    /** Call from onCreate. */
    void setup(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(android.graphics.Color.WHITE);
        root.addView(Ui.header(this, title));
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.setDefaultFocusHighlightEnabled(false);
        scroll.addView(list);
        root.addView(scroll);
        setContentView(root);
    }

    void clear() {
        list.removeAllViews();
        actions.clear();
    }

    void item(String text, Runnable onClick) {
        item(null, text, 20, null, onClick);
    }

    void item(Drawable icon, String title, float titleSize, String subtitle, Runnable onClick) {
        list.addView(Ui.row(this, actions.size() + 1, icon, title, titleSize, subtitle, onClick));
        list.addView(Ui.divider(this));
        actions.add(onClick);
    }

    void note(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), 0);
        list.addView(t);
    }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int i = keyCode - KeyEvent.KEYCODE_1;
        if (i >= 0 && i < 9 && i < actions.size()) {
            actions.get(i).run();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        io.shutdown();
        super.onDestroy();
    }
}
