package app.foldhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restarts the fold watcher after a reboot or an app update. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WatchService.start(context);
    }
}
