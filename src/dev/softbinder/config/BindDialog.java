package dev.softbinder.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The add/edit form for one bind. */
final class BindDialog {
    interface Listener {
        void onSave(Bind bind);

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
        final Button app = button(a, "", null);
        app.setOnClickListener(v -> pickApp(a, pkg[0], chosen -> {
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

        form.addView(text(a, "Action"));
        final Spinner action = spinner(a, Bind.ACTIONS, b.action);
        form.addView(action);
        final TextView hint = text(a, Bind.HINTS[action.getSelectedItemPosition()]);
        form.addView(hint);
        final EditText param = field(a, b.param);
        form.addView(param);
        action.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int i, long id) {
                hint.setText(Bind.HINTS[i]);
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
                .setPositiveButton("Save", (x, w) -> {
                    Bind out = new Bind();
                    out.pkg = pkg[0].trim();
                    out.key = Bind.KEYS[key.getSelectedItemPosition()];
                    out.label = label.getText().toString().trim();
                    out.action = Bind.ACTIONS[action.getSelectedItemPosition()];
                    out.param = param.getText().toString().trim();
                    listener.onSave(out);
                })
                .setNegativeButton("Cancel", null);
        if (existing != null)
            d.setNeutralButton("Delete", (x, w) -> listener.onDelete());
        d.show();
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

    /**
     * Launchable apps with their icons; the first row types a package name by hand.
     */
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

    private static Spinner spinner(Activity a, String[] values, String selected) {
        Spinner s = new Spinner(a);
        s.setAdapter(new ArrayAdapter<String>(a, android.R.layout.simple_spinner_dropdown_item, values));
        s.setSelection(Bind.indexOf(values, selected));
        return s;
    }
}
