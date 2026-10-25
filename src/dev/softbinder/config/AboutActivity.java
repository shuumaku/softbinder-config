package dev.softbinder.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Laid out like the stock apps' About page: icon, then label/value pairs, then
 * the licence button.
 */
public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(android.graphics.Color.WHITE);
        root.addView(Ui.header(this, "About"));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding((int) (12 * dp), (int) (8 * dp), (int) (12 * dp), (int) (8 * dp));

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(getApplicationInfo().loadIcon(getPackageManager()));
        page.addView(icon, new LinearLayout.LayoutParams((int) (72 * dp), (int) (72 * dp)));

        page.addView(row("Application name", appName()));
        page.addView(row("Version", version()));
        page.addView(row("License", "GNU GPL v3"));

        Button license = new Button(this);
        license.setAllCaps(false);
        license.setText("Open source license");
        license.setTextColor(Color.BLACK);
        license.setBackgroundColor(0xFFDDDDDD);
        license.setOnClickListener(v -> showLicense());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (16 * dp);
        page.addView(license, lp);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        root.addView(scroll);
        setContentView(root);
    }

    private LinearLayout row(String label, String value) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setGravity(Gravity.CENTER_HORIZONTAL);
        r.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(15);
        l.setTextColor(Color.GRAY);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(19);
        v.setTextColor(Color.BLACK);
        v.setGravity(Gravity.CENTER_HORIZONTAL);
        r.addView(l);
        r.addView(v);
        return r;
    }

    private String appName() {
        return String.valueOf(getApplicationInfo().loadLabel(getPackageManager()));
    }

    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private void showLicense() {
        new Thread(() -> {
            String text;
            try {
                InputStream in = getAssets().open("LICENSE.txt");
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    for (int n; (n = in.read(buf)) > 0;)
                        out.write(buf, 0, n);
                    text = out.toString("UTF-8");
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                text = "License text unavailable.";
            }
            final String shown = text;
            runOnUiThread(() -> {
                TextView t = new TextView(this);
                t.setTextSize(10);
                t.setText(shown);
                ScrollView s = new ScrollView(this);
                s.addView(t);
                new AlertDialog.Builder(this).setView(s).setPositiveButton("Close", null).show();
            });
        }).start();
    }
}
