package dev.softbinder.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Installed-app lookups; the package name stands in for anything that isn't
 * installed.
 */
final class Apps {
    private Apps() {
    }

    static String label(Context ctx, String pkg) {
        try {
            PackageManager pm = ctx.getPackageManager();
            return String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)));
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    /** @return the app's icon, or null if it isn't installed. */
    static Drawable icon(Context ctx, String pkg) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return ai.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
