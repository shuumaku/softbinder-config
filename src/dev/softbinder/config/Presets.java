package dev.softbinder.config;

final class Presets {
    static final class Preset {
        final String name, label, action, param;

        Preset(String name, String label, String action, String param) {
            this.name = name;
            this.label = label;
            this.action = action;
            this.param = param;
        }
    }

    static final Preset[] ALL = {
            new Preset("Refresh / pull down", "Refresh", "swipe", "down"),
            new Preset("Scroll up", "Up", "swipe", "up"),
            new Preset("Back", "Back", "key", "KEYCODE_BACK"),
            new Preset("Home", "Home", "key", "KEYCODE_HOME"),
            new Preset("Stopwatch", "Stopwatch", "launch", "com.android.deskclock/.StopwatchActivity"),
            new Preset("Timer", "Timer", "launch", "com.android.deskclock/.TimerListActivity"),
            new Preset("Alarms", "Alarms", "launch", "com.android.deskclock/.AlarmListActivity"),
            new Preset("World clock", "Clock", "launch", "com.android.deskclock/.worldclock.afp.WorldClockAfpActivity"),
            new Preset("Calculator", "Calc", "launch", "com.kyocera.calculator2/.Calculator"),
            new Preset("Dialer", "Dialer", "intent", "android.intent.action.DIAL tel:"),
            new Preset("Camera", "Camera", "intent", "android.media.action.STILL_IMAGE_CAMERA"),
            new Preset("New message", "Message", "intent", "android.intent.action.SENDTO sms:"),
            new Preset("Downloads", "Downloads", "intent", "android.intent.action.VIEW_DOWNLOADS"),
            new Preset("Wi-Fi settings", "Wi-Fi", "intent", "android.settings.WIFI_SETTINGS"),
            new Preset("APN settings", "APN", "intent", "android.settings.APN_SETTINGS"),
            new Preset("Display settings", "Display", "launch",
                    "com.android.settings/.Settings$DisplaySettingsActivity"),
            new Preset("Date & time settings", "Date", "launch",
                    "com.android.settings/.Settings$DateTimeSettingsActivity"),
            new Preset("Sound recorder", "Recorder", "launch", "jp.kyocera.kc_soundrecorder/.MainMenuActivity"),
            new Preset("FM radio", "Radio", "launch", "jp.kyocera.kc_fmradio/.MainActivity"),
            new Preset("Memo list", "Memos", "launch", "jp.kyocera.kcmushroom/.quotememo.MemoListActivity"),
            new Preset("Schedule: day", "Day", "launch", "com.nttdocomo.android.schedulememo/.DayActivity"),
            new Preset("Schedule: month", "Month", "launch", "com.nttdocomo.android.schedulememo/.MonthActivity"),
    };

    private Presets() {
    }

    static String describe(android.content.Context ctx, String action, String param) {
        for (Preset p : ALL)
            if (p.action.equals(action) && p.param.equals(param))
                return p.name;
        switch (action) {
            case "launch":
                if (param.contains("://"))
                    return "Open " + param;
                int slash = param.indexOf('/');
                if (slash < 0)
                    return "Open " + Apps.label(ctx, param);
                String cls = param.substring(Math.max(param.lastIndexOf('.'), param.lastIndexOf('$')) + 1);
                return "Open " + Apps.label(ctx, param.substring(0, slash)) + " (" + cls + ")";
            case "intent":
                return "Start " + param;
            case "broadcast":
                return "Broadcast " + param;
            case "swipe":
                return "Swipe " + param;
            case "tap":
                return "Tap " + param;
            default:
                return "Press " + param;
        }
    }
}
