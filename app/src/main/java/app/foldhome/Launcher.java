package app.foldhome;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/** Starts the launcher configured for the current fold state. */
final class Launcher {
    private static final String TAG = "FoldHome";

    /** Returns false if nothing is configured or the launcher can't be started. */
    static boolean open(Context ctx) {
        String key = Prefs.stateKey(ctx.getResources().getConfiguration());
        ComponentName target = Prefs.get(ctx, key);
        if (target == null) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setComponent(target)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        try {
            ctx.startActivity(intent, ActivityOptions.makeCustomAnimation(ctx, 0, 0).toBundle());
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "Cannot launch " + target + " for " + key, e);
            return false;
        }
    }

    private Launcher() {}
}
