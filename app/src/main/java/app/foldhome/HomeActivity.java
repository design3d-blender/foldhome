package app.foldhome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * The default home app. It never shows UI of its own: whenever it comes to the
 * front (Home press, gesture, back from a launcher) it forwards to the launcher
 * configured for the current fold state.
 *
 * Never finish() this activity: the system would just relaunch home in a loop.
 */
public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No content view: the theme shows the wallpaper.
        WatchService.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onNewIntent (Home press) is always followed by onResume, so this covers every entry point.
        if (!Launcher.open(this)) {
            startActivity(new Intent(this, SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
