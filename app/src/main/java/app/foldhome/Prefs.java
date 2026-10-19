package app.foldhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/** Stores the launcher chosen for each fold state. */
final class Prefs {
    static final String FOLDED = "folded";
    static final String UNFOLDED = "unfolded";

    /** Cover screen is phone-sized; the inner screen is tablet-sized (sw >= 600dp). */
    static boolean isUnfolded(Configuration config) {
        return config.smallestScreenWidthDp >= 600;
    }

    static String stateKey(Configuration config) {
        return isUnfolded(config) ? UNFOLDED : FOLDED;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("launchers", Context.MODE_PRIVATE);
    }

    static ComponentName get(Context ctx, String key) {
        String flat = prefs(ctx).getString(key, null);
        return flat == null ? null : ComponentName.unflattenFromString(flat);
    }

    static void set(Context ctx, String key, ComponentName component) {
        prefs(ctx).edit().putString(key, component.flattenToString()).apply();
    }

    private Prefs() {}
}
