package dev.softbinder.config;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** The module's config file */
final class Store {
    static final String CONF = "/data/system/softbinder.json";
    static final String LOG = "/data/system/softbinder.log";

    static final class Data {
        final List<Bind> binds = new ArrayList<Bind>();
        boolean debug;
    }

    private Store() {
    }

    static Data load() throws IOException {
        Data d = new Data();
        String text = Root.run("if [ -f " + CONF + " ]; then cat " + CONF + "; fi").trim();
        if (text.isEmpty())
            return d;
        try {
            JSONObject root = new JSONObject(text);
            d.debug = root.optBoolean("debug");
            JSONArray list = root.optJSONArray("binds");
            for (int i = 0; list != null && i < list.length(); i++) {
                JSONObject o = list.optJSONObject(i);
                if (o != null)
                    d.binds.add(Bind.fromJson(o));
            }
        } catch (JSONException e) {
            throw new IOException("config is not valid JSON");
        }
        return d;
    }

    /**
     * Writes next to the target and renames over it, so the module never reads a
     * half-written file.
     */
    static void save(Context ctx, Data d) throws IOException {
        StringBuilder s = new StringBuilder("{\n  \"debug\": ").append(d.debug).append(",\n  \"binds\": [");
        for (int i = 0; i < d.binds.size(); i++)
            s.append(i == 0 ? "\n    " : ",\n    ").append(d.binds.get(i).json(i + 1));
        s.append(d.binds.isEmpty() ? "]\n}\n" : "\n  ]\n}\n");
        File tmp = new File(ctx.getCacheDir(), "softbinder.json");
        FileOutputStream o = new FileOutputStream(tmp);
        try {
            o.write(s.toString().getBytes("UTF-8"));
        } finally {
            o.close();
        }
        String next = CONF + ".new";
        Root.run("cat " + tmp + " > " + next + " && chown system:system " + next + " && chmod 600 " + next
                + " && chcon u:object_r:system_data_file:s0 " + next + " && mv " + next + " " + CONF);
    }

    static String log() throws IOException {
        return Root.run("if [ -f " + LOG + " ]; then tail -n 80 " + LOG + "; fi");
    }
}
