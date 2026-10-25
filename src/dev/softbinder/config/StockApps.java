package dev.softbinder.config;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Stock OEM apps that use the softkey bar (assets/stock_apps.json: [{"app",
 * "risk", "note"}]).
 */
final class StockApps {
    private static Map<String, String[]> apps;

    private StockApps() {
    }

    /** @return a warning for a stock app, or null for any other package. */
    static synchronized String warning(Context ctx, String pkg) {
        if (apps == null)
            apps = load(ctx);
        String[] e = apps.get(pkg);
        if (e == null)
            return null;
        return pkg + " is a stock OEM app that uses the softkey bar (risk: " + e[0]
                + "). Binding it may break functionality, "
                + "such as a key the app handles itself.\n\n" + e[1];
    }

    private static Map<String, String[]> load(Context ctx) {
        Map<String, String[]> m = new HashMap<String, String[]>();
        try {
            InputStream in = ctx.getAssets().open("stock_apps.json");
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) > 0;)
                    out.write(buf, 0, n);
                JSONArray list = new JSONArray(out.toString("UTF-8"));
                for (int i = 0; i < list.length(); i++) {
                    JSONObject o = list.getJSONObject(i);
                    m.put(o.getString("app"), new String[] { o.getString("risk"), o.getString("note") });
                }
            } finally {
                in.close();
            }
        } catch (IOException | JSONException ignored) {
        }
        return m;
    }
}
