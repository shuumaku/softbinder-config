package dev.softbinder.config;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

/** Top-level menu. */
public class MainActivity extends Screen {
    private Store.Data data = new Store.Data();
    private String error;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setup("Softbinder");
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
        io.execute(() -> {
            try {
                Store.Data loaded = Store.load();
                runOnUiThread(() -> {
                    data = loaded;
                    error = null;
                    render();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    error = e.getMessage();
                    render();
                });
            }
        });
    }

    private void render() {
        clear();
        item("Binds (" + data.binds.size() + ")", () -> startActivity(new Intent(this, BindsActivity.class)));
        item("Add bind", () -> {
            if (error != null)
                toast("Root unavailable");
            else
                BindEditor.edit(this, data, null, this::render);
        });
        item("Debug log: " + (data.debug ? "on" : "off"), () -> {
            if (error != null) {
                toast("Root unavailable");
                return;
            }
            data.debug = !data.debug;
            BindEditor.persist(this, data, this::render);
        });
        item("View log", this::showLog);
        item("About", () -> startActivity(new Intent(this, AboutActivity.class)));
        if (error != null)
            note("Root unavailable: " + error);
    }

    private void showLog() {
        io.execute(() -> {
            String text;
            try {
                text = Store.log();
            } catch (IOException e) {
                text = e.getMessage();
            }
            final String shown = text.isEmpty() ? "(empty)" : text;
            runOnUiThread(() -> {
                TextView t = new TextView(this);
                t.setTextSize(10);
                t.setText(shown);
                ScrollView s = new ScrollView(this);
                s.addView(t);
                new AlertDialog.Builder(this).setView(s).setPositiveButton("Close", null).show();
            });
        });
    }
}
