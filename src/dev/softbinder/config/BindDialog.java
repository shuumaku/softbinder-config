package dev.softbinder.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The add/edit form for one bind. */
final class BindDialog {
    interface Listener {
        void onSave(Bind bind, Runnable close);

        void onDelete();
    }

    private BindDialog() {
    }

    /**
     * @param existing the bind being edited, or null for a new one (the dialog
     *                 never modifies it).
     */
    static void show(final Activity a, Bind existing, final Listener listener) {
        Bind b = existing != null ? existing : new Bind();
        LinearLayout form = new LinearLayout(a);
        form.setOrientation(LinearLayout.VERTICAL);

        form.addView(text(a, "App"));
        final String[] pkg = { b.pkg };
        final String[] activity = { b.activity };
        final Button activityButton = button(a, "", null);
        final Button app = button(a, "", null);
        app.setOnClickListener(v -> pickApp(a, pkg[0], chosen -> {
            if (!chosen.equals(pkg[0])) {
                activity[0] = "";
                showActivity(activityButton, activity[0]);
            }
            pkg[0] = chosen;
            showApp(a, app, chosen);
        }));
        showApp(a, app, pkg[0]);
        form.addView(app);

        form.addView(text(a, "Key (F1 is skipped in apps that use the system menu)"));
        final Spinner key = spinner(a, Bind.KEYS, b.key);
        form.addView(key);

        form.addView(text(a, "Label on the bar"));
        final EditText label = field(a, b.label);
        label.setFilters(new InputFilter[] { new InputFilter.LengthFilter(10) });
        form.addView(label);

        form.addView(text(a, "When"));
        final Spinner when = spinnerAt(a, Bind.WINDOW_NAMES, Bind.indexOf(Bind.WINDOWS, b.window));
        form.addView(when);
        form.addView(text(a, "Only in activity (optional)"));
        activityButton.setOnClickListener(v -> pickActivity(a, pkg[0], activity[0], chosen -> {
            activity[0] = chosen;
            showActivity(activityButton, chosen);
        }));
        showActivity(activityButton, activity[0]);
        form.addView(activityButton);

        form.addView(text(a, "Action"));
        final Spinner action = spinner(a, Bind.ACTIONS, b.action);
        form.addView(action);
        form.addView(text(a, "Parameter"));
        final EditText param = field(a, b.param);
        param.setHint(Bind.HINTS[action.getSelectedItemPosition()]);
        final Button launchPick = button(a, "Choose what to launch...", v -> pickLaunch(a, pkg[0], param));
        form.addView(launchPick);
        final Button tapPick = button(a, "Pick from screenshot...", v -> ShotPicker.start(a, pkg[0], param));
        form.addView(tapPick);
        form.addView(param);
        launchPick.setVisibility(Bind.ACTIONS[action.getSelectedItemPosition()].equals("launch") ? View.VISIBLE : View.GONE);
        tapPick.setVisibility(Bind.ACTIONS[action.getSelectedItemPosition()].equals("tap") ? View.VISIBLE : View.GONE);
        action.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int i, long id) {
                param.setHint(Bind.HINTS[i]);
                launchPick.setVisibility(Bind.ACTIONS[i].equals("launch") ? View.VISIBLE : View.GONE);
                tapPick.setVisibility(Bind.ACTIONS[i].equals("tap") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> p) {
            }
        });

        form.addView(button(a, "Fill from preset...", v -> {
            String[] names = new String[Presets.ALL.length];
            for (int i = 0; i < names.length; i++)
                names[i] = Presets.ALL[i].name;
            new AlertDialog.Builder(a).setItems(names, (d, i) -> {
                Presets.Preset p = Presets.ALL[i];
                if (label.getText().length() == 0)
                    label.setText(p.label);
                action.setSelection(Bind.indexOf(Bind.ACTIONS, p.action));
                param.setText(p.param);
            }).show();
        }));

        ScrollView scroll = new ScrollView(a);
        scroll.addView(form);
        AlertDialog.Builder d = new AlertDialog.Builder(a).setView(scroll)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null);
        if (existing != null)
            d.setNeutralButton("Delete", (x, w) -> listener.onDelete());
        final AlertDialog dialog = d.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        // Set after show() so a rejected save leaves the dialog (and its input) open.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Bind out = new Bind();
            out.pkg = pkg[0].trim();
            out.key = Bind.KEYS[key.getSelectedItemPosition()];
            out.label = label.getText().toString().trim();
            out.action = Bind.ACTIONS[action.getSelectedItemPosition()];
            out.param = param.getText().toString().trim();
            out.window = Bind.WINDOWS[when.getSelectedItemPosition()];
            out.activity = activity[0].trim();
            listener.onSave(out, dialog::dismiss);
        });
    }

    /**
     * The selected app: its name, with the package under it; just the package if
     * the app isn't installed.
     */
    private static void showApp(Activity a, Button button, String pkg) {
        String name = Apps.label(a, pkg);
        if (pkg.isEmpty()) {
            button.setText("Choose app...");
            return;
        }
        if (name.equals(pkg)) {
            button.setText(pkg);
            return;
        }
        SpannableString s = new SpannableString(name + "\n" + pkg);
        s.setSpan(new RelativeSizeSpan(0.7f), name.length() + 1, s.length(), 0);
        button.setText(s);
    }

    /** Full class names of the activities the app declares */
    private static List<String> activitiesOf(Activity a, String pkg) {
        List<String> out = new ArrayList<String>();
        try {
            PackageInfo pi = a.getPackageManager().getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            if (pi.activities != null)
                for (ActivityInfo ai : pi.activities)
                    out.add(ai.name);
        } catch (PackageManager.NameNotFoundException e) {
            // not installed
        }
        return out;
    }

    /**
     * Fills the launch parameter
     */
    private static void pickLaunch(final Activity a, final String pkg, final EditText param) {
        String[] items = { "Installed apps", "Current app activities", "Other (scheme://uri)" };
        new AlertDialog.Builder(a).setItems(items, (d, i) -> {
            if (i == 0) {
                pickApp(a, "", chosen -> param.setText(chosen));
            } else if (i == 1) {
                if (pkg.isEmpty()) {
                    Toast.makeText(a, "Choose an app first", Toast.LENGTH_SHORT).show();
                    return;
                }
                final List<String> full = activitiesOf(a, pkg);
                if (full.isEmpty()) {
                    Toast.makeText(a, "No activities found (is the app installed?)", Toast.LENGTH_SHORT).show();
                    return;
                }
                Collections.sort(full, String::compareToIgnoreCase);
                final String[] shown = new String[full.size()];
                for (int n = 0; n < shown.length; n++)
                    shown[n] = full.get(n).startsWith(pkg + ".") ? full.get(n).substring(pkg.length()) : full.get(n);
                new AlertDialog.Builder(a).setItems(shown, (x, n) -> param.setText(pkg + "/" + shown[n])).show();
            } else {
                final EditText typed = field(a, param.getText().toString().contains("://")
                        ? param.getText().toString() : "");
                typed.setHint("scheme://uri");
                new AlertDialog.Builder(a).setTitle("Scheme / URI").setView(typed)
                        .setPositiveButton("OK", (x, w) -> param.setText(typed.getText().toString().trim()))
                        .setNegativeButton("Cancel", null).show();
            }
        }).show();
    }

    private static void showActivity(Button button, String activity) {
        button.setText(activity.isEmpty() ? "Any activity" : activity);
    }

    private static void pickActivity(final Activity a, final String pkg, final String current,
            final Consumer<String> chosen) {
        final List<String> names = new ArrayList<String>();
        for (String full : activitiesOf(a, pkg))
            names.add(full.startsWith(pkg + ".") ? full.substring(pkg.length()) : full);
        Collections.sort(names, String::compareToIgnoreCase);
        final String[] items = new String[names.size() + 2];
        items[0] = "Any activity";
        for (int i = 0; i < names.size(); i++)
            items[i + 1] = names.get(i);
        items[items.length - 1] = "Other...";
        new AlertDialog.Builder(a).setItems(items, (d, i) -> {
            if (i == 0) {
                chosen.accept("");
            } else if (i < items.length - 1) {
                chosen.accept(items[i]);
            } else {
                final EditText typed = field(a, current);
                typed.selectAll();
                new AlertDialog.Builder(a).setTitle("Activity (end of its name, e.g. .SearchActivity)")
                    .setView(typed)
                    .setPositiveButton("OK", (x, w) -> chosen.accept(typed.getText().toString().trim()))
                    .setNegativeButton("Cancel", null).show();
            }
        }).show();
    }

    private static void pickApp(final Activity a, final String current, final Consumer<String> chosen) {
        final PackageManager pm = a.getPackageManager();
        final List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
        final Map<String, String> labels = new HashMap<String, String>();
        for (ApplicationInfo ai : pm.getInstalledApplications(0)) {
            if (pm.getLaunchIntentForPackage(ai.packageName) == null)
                continue;
            apps.add(ai);
            labels.put(ai.packageName, String.valueOf(pm.getApplicationLabel(ai)));
        }
        Collections.sort(apps, (x, y) -> labels.get(x.packageName).compareToIgnoreCase(labels.get(y.packageName)));
        final float dp = a.getResources().getDisplayMetrics().density;
        final int iconSize = (int) (40 * dp), pad = (int) (8 * dp);

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return apps.size() + 1;
            }

            @Override
            public Object getItem(int i) {
                return i == 0 ? null : apps.get(i - 1);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View row, ViewGroup parent) {
                if (row == null) {
                    LinearLayout r = new LinearLayout(parent.getContext());
                    r.setGravity(Gravity.CENTER_VERTICAL);
                    r.setPadding(pad, pad / 2, pad, pad / 2);
                    r.addView(new ImageView(parent.getContext()), new LinearLayout.LayoutParams(iconSize, iconSize));
                    LinearLayout names = new LinearLayout(parent.getContext());
                    names.setOrientation(LinearLayout.VERTICAL);
                    names.setPadding(pad, 0, 0, 0);
                    TextView name = new TextView(parent.getContext());
                    name.setTextSize(16);
                    name.setSingleLine();
                    TextView sub = new TextView(parent.getContext());
                    sub.setTextSize(11);
                    sub.setTextColor(Color.GRAY);
                    sub.setSingleLine();
                    names.addView(name);
                    names.addView(sub);
                    r.addView(names);
                    r.setTag(new TextView[] { name, sub });
                    row = r;
                }
                TextView[] t = (TextView[]) row.getTag();
                ImageView icon = (ImageView) ((LinearLayout) row).getChildAt(0);
                if (i == 0) {
                    t[0].setText("Other package...");
                    t[1].setText("type the package name");
                    icon.setImageDrawable(null);
                } else {
                    ApplicationInfo ai = apps.get(i - 1);
                    t[0].setText(labels.get(ai.packageName));
                    t[1].setText(ai.packageName);
                    icon.setImageDrawable(ai.loadIcon(pm));
                }
                return row;
            }
        };
        new AlertDialog.Builder(a).setAdapter(adapter, (d, i) -> {
            if (i > 0) {
                chosen.accept(apps.get(i - 1).packageName);
                return;
            }
            final EditText typed = field(a, current);
            typed.selectAll();
            new AlertDialog.Builder(a).setTitle("Package name").setView(typed)
                    .setPositiveButton("OK", (x, w) -> chosen.accept(typed.getText().toString().trim()))
                    .setNegativeButton("Cancel", null).show();
        }).show();
    }

    private static TextView text(Activity a, String s) {
        TextView t = new TextView(a);
        t.setText(s);
        return t;
    }

    private static EditText field(Activity a, String value) {
        EditText e = new EditText(a);
        e.setSingleLine();
        e.setText(value);
        return e;
    }

    private static Button button(Activity a, String s, View.OnClickListener l) {
        Button b = new Button(a);
        b.setAllCaps(false);
        b.setText(s);
        b.setOnClickListener(l);
        return b;
    }

    private static Spinner spinnerAt(Activity a, String[] values, int selected) {
        Spinner s = new Spinner(a);
        s.setAdapter(new ArrayAdapter<String>(a, android.R.layout.simple_spinner_dropdown_item, values));
        s.setSelection(selected);
        return s;
    }

    private static Spinner spinner(Activity a, String[] values, String selected) {
        Spinner s = new Spinner(a);
        s.setAdapter(new ArrayAdapter<String>(a, android.R.layout.simple_spinner_dropdown_item, values));
        s.setSelection(Bind.indexOf(values, selected));
        return s;
    }
}
