package app.foldhome;

import android.app.Application;
import android.app.role.RoleManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * As the default home app this process stays resident, so it can watch for
 * fold/unfold itself. When the fold state flips while a launcher is on screen,
 * it swaps to the other launcher right away. Inside any other app it does
 * nothing; the new launcher shows up on the next Home gesture.
 */
public class FoldHomeApp extends Application {
    private static final String TAG = "FoldHome";
    /** The fold animation fires several config changes; act once it settles. */
    private static final long SETTLE_MS = 350;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable onFoldChanged = this::swapIfOnLauncher;
    private boolean unfolded;

    @Override
    public void onCreate() {
        super.onCreate();
        unfolded = Prefs.isUnfolded(getResources().getConfiguration());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean nowUnfolded = Prefs.isUnfolded(newConfig);
        if (nowUnfolded != unfolded) {
            unfolded = nowUnfolded;
            handler.removeCallbacks(onFoldChanged);
            handler.postDelayed(onFoldChanged, SETTLE_MS);
        }
    }

    private void swapIfOnLauncher() {
        // Paused while another launcher holds the home role (e.g. editing One UI Home's layout).
        if (!getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_HOME)) {
            return;
        }
        String front = foregroundPackage();
        if (front == null || !isLauncher(front)) {
            return;
        }
        ComponentName target = Prefs.get(this, Prefs.stateKey(getResources().getConfiguration()));
        if (target != null && !target.getPackageName().equals(front)) {
            BackgroundLaunch.openLauncher(this);
        }
    }

    private boolean isLauncher(String pkg) {
        if (pkg.equals(getPackageName())) {
            return true;
        }
        for (String key : new String[] {Prefs.FOLDED, Prefs.UNFOLDED}) {
            ComponentName cn = Prefs.get(this, key);
            if (cn != null && cn.getPackageName().equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /** Package of the most recently resumed activity; needs Usage Access. Null if unavailable. */
    private String foregroundPackage() {
        UsageStatsManager usm = getSystemService(UsageStatsManager.class);
        long now = System.currentTimeMillis();
        UsageEvents events = usm.queryEvents(now - 60 * 60 * 1000, now);
        UsageEvents.Event event = new UsageEvents.Event();
        String last = null;
        while (events.getNextEvent(event)) {
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                last = event.getPackageName();
            }
        }
        if (last == null) {
            Log.w(TAG, "No usage events; is Usage Access granted?");
        }
        return last;
    }
}
