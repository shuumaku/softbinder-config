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

    /** When a bind applies: the app's main window, a dialog or menu, with the keyboard up, or any window. */
    static final String[] WINDOWS = { "base", "dialog", "ime", "any" };
    static final String[] WINDOW_NAMES = { "Main window", "Dialog or menu", "Keyboard showing", "Any window" };

    String pkg = "", key = "F3", label = "", action = ACTIONS[0], param = "", window = WINDOWS[0], activity = "";

    static Bind fromJson(JSONObject o) {
        Bind b = new Bind();
        b.pkg = o.optString("app");
        b.key = o.optString("key", b.key);
        b.label = o.optString("label");
        b.action = o.optString("action", b.action);
        b.param = o.optString("param");
        b.window = o.optString("window", b.window);
        b.activity = o.optString("activity");
        return b;
    }

    /** One line of the file; "window" and "activity" are only written when they are not the default. */
    String json(int index) {
        return "{\"index\": " + index + ", \"app\": " + quote(pkg) + ", \"key\": " + quote(key) + ", \"label\": "
                + quote(label)
                + ", \"action\": " + quote(action) + ", \"param\": " + quote(param)
                + (window.equals(WINDOWS[0]) ? "" : ", \"window\": " + quote(window))
                + (activity.isEmpty() ? "" : ", \"activity\": " + quote(activity)) + "}";
    }

    /** Short description of when this bind applies, or "" for the default (main window, any activity). */
    String context() {
        String w = window.equals(WINDOWS[0]) ? "" : WINDOW_NAMES[indexOf(WINDOWS, window)];
        String a = activity.isEmpty() ? "" : activity.substring(activity.lastIndexOf('.') + 1);
        return w.isEmpty() ? a : a.isEmpty() ? w : w + ", " + a;
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
