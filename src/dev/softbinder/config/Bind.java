package dev.softbinder.config;

import org.json.JSONObject;

/** One entry of the module's "binds" list. */
final class Bind {
    static final String[] KEYS = { "F1", "F2", "F3", "F4" };
    static final String[] ACTIONS = { "launch", "intent", "broadcast", "swipe", "key" };
    static final String[] HINTS = {
            "package, package/.Class, or scheme://uri",
            "intent action, then an optional data uri",
            "intent action, then an optional package",
            "up, down, left or right",
            "KEYCODE_NAME or number" };

    String pkg = "", key = "F3", label = "", action = ACTIONS[0], param = "";

    static Bind fromJson(JSONObject o) {
        Bind b = new Bind();
        b.pkg = o.optString("app");
        b.key = o.optString("key", b.key);
        b.label = o.optString("label");
        b.action = o.optString("action", b.action);
        b.param = o.optString("param");
        return b;
    }

    /** One line of the file */
    String json(int index) {
        return "{\"index\": " + index + ", \"app\": " + quote(pkg) + ", \"key\": " + quote(key) + ", \"label\": "
                + quote(label)
                + ", \"action\": " + quote(action) + ", \"param\": " + quote(param) + "}";
    }

    static String quote(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\')
                b.append('\\').append(c);
            else if (c < 0x20)
                b.append(String.format("\\u%04x", (int) c));
            else
                b.append(c);
        }
        return b.append('"').toString();
    }

    static int indexOf(String[] values, String v) {
        for (int i = 0; i < values.length; i++)
            if (values[i].equals(v))
                return i;
        return 0;
    }
}
