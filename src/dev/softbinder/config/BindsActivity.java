package dev.softbinder.config;

import android.os.Bundle;

import java.io.IOException;

/** The bind list: one numbered row per bind, then Add bind. */
public class BindsActivity extends Screen {
    private Store.Data data = new Store.Data();
    private String error;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setup("Binds");
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        for (final Bind b : data.binds) {
            String name = Apps.label(this, b.pkg);
            String shown = b.key + "  " + (char) 0xB7 + "  " + (b.label.isEmpty() ? "(no label)" : b.label);
            item(Apps.icon(this, b.pkg), (StockApps.warning(this, b.pkg) != null ? "(!) " : "") + name, 18,
                    shown + "\n" + Presets.describe(this, b.action, b.param),
                    () -> BindEditor.edit(this, data, b, this::render));
        }
        item("Add bind", () -> BindEditor.edit(this, data, null, this::render));
        if (error != null)
            note("Root unavailable: " + error);
    }
}
