package dev.softbinder.config;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    private static final int BAR = 0xFFE0E0E0, BADGE = 0xFF4A4044, LINE = 0xFFC8C8C8, FOCUS = 0xFFC5E1FF;

    private Ui() {
    }

    static int dp(Activity a, float v) {
        return (int) (v * a.getResources().getDisplayMetrics().density);
    }

    static TextView header(Activity a, String title) {
        TextView t = new TextView(a);
        t.setText(title);
        t.setTextSize(20);
        t.setTextColor(Color.BLACK);
        t.setBackgroundColor(BAR);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(a, 12), dp(a, 10), dp(a, 12), dp(a, 10));
        return t;
    }

    /**
     * @param number   1-9 for a badge, anything else for none
     * @param icon     shown after the badge, or null
     * @param subtitle a smaller grey second line, or null
     */
    static View row(Activity a, int number, Drawable icon, String title, float titleSize, String subtitle,
            final Runnable onClick) {
        LinearLayout r = new LinearLayout(a);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(a, 8), dp(a, 10), dp(a, 8), dp(a, 10));
        r.setFocusable(true);
        r.setClickable(true);
        r.setDefaultFocusHighlightEnabled(false);
        r.setBackgroundColor(Color.WHITE);
        r.setOnFocusChangeListener((v, focused) -> v.setBackgroundColor(focused ? FOCUS : Color.WHITE));
        r.setOnClickListener(v -> onClick.run());

        TextView badge = new TextView(a);
        int box = dp(a, 30);
        if (number >= 1 && number <= 9) {
            badge.setText(String.valueOf(number));
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(17);
            badge.setIncludeFontPadding(false);
            badge.setGravity(Gravity.CENTER);
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(BADGE);
            shape.setCornerRadius(dp(a, 3));
            badge.setBackground(shape);
        }
        r.addView(badge, new LinearLayout.LayoutParams(box, box));

        if (icon != null) {
            ImageView image = new ImageView(a);
            image.setImageDrawable(icon);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(a, 36), dp(a, 36));
            lp.leftMargin = dp(a, 10);
            r.addView(image, lp);
        }

        LinearLayout text = new LinearLayout(a);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(dp(a, 12), 0, 0, 0);
        TextView t = new TextView(a);
        t.setText(title);
        t.setTextSize(titleSize);
        t.setTextColor(Color.BLACK);
        text.addView(t);
        if (subtitle != null) {
            TextView sub = new TextView(a);
            sub.setText(subtitle);
            sub.setTextSize(15);
            sub.setTextColor(Color.DKGRAY);
            text.addView(sub);
        }
        r.addView(text, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return r;
    }

    static View divider(Activity a) {
        View v = new View(a);
        v.setBackgroundColor(LINE);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return v;
    }
}
