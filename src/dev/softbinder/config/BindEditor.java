package dev.softbinder.config;

import android.app.AlertDialog;

import java.io.IOException;
import java.util.Iterator;

/**
 * Add / edit / delete a bind, with the stock-app warning, writing the result
 * straight to the config file.
 */
final class BindEditor {
    private BindEditor() {
    }

    static void edit(final Screen s, final Store.Data data, final Bind existing, final Runnable onChanged) {
        BindDialog.show(s, existing, new BindDialog.Listener() {
            @Override
            public void onSave(final Bind b, final Runnable close) {
                if (b.pkg.isEmpty()) {
                    s.toast("Choose an app");
                    return;
                }
                if (b.param.isEmpty()) {
                    s.toast("The action needs a parameter");
                    return;
                }
                if (b.action.equals("tap") && !b.param.matches("\\d+\\s+\\d+")) {
                    s.toast("Tap needs x and y, e.g. 540 1200");
                    return;
                }
                String warning = StockApps.warning(s, b.pkg);
                if (warning == null) {
                    commit(s, data, b, existing, onChanged);
                    close.run();
                    return;
                }
                new AlertDialog.Builder(s).setTitle("Stock OEM app").setMessage(warning)
                        .setPositiveButton("Save anyway", (d, w) -> {
                            commit(s, data, b, existing, onChanged);
                            close.run();
                        })
                        .setNegativeButton("Cancel", null).show();
            }

            @Override
            public void onDelete() {
                data.binds.remove(existing);
                persist(s, data, onChanged);
            }
        });
    }

    /** One bind per (package, key, window, activity): the newest wins. */
    private static void commit(Screen s, Store.Data data, Bind b, Bind replaced, Runnable onChanged) {
        data.binds.remove(replaced);
        for (Iterator<Bind> it = data.binds.iterator(); it.hasNext();) {
            Bind o = it.next();
            if (o.pkg.equals(b.pkg) && o.key.equals(b.key) && o.window.equals(b.window)
                    && o.activity.equals(b.activity))
                it.remove();
        }
        data.binds.add(b);
        persist(s, data, onChanged);
    }

    static void persist(final Screen s, Store.Data data, final Runnable onChanged) {
        final Store.Data snapshot = new Store.Data();
        snapshot.binds.addAll(data.binds);
        snapshot.debug = data.debug;
        onChanged.run();
        s.io.execute(() -> {
            try {
                Store.save(s, snapshot);
                s.runOnUiThread(() -> s.toast("Saved"));
            } catch (IOException e) {
                s.runOnUiThread(() -> s.toast("Save failed: " + e.getMessage()));
            }
        });
    }
}
