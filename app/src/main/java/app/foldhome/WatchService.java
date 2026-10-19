package app.foldhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;

/**
 * Does no work itself: it only keeps the process alive so FoldHomeApp's
 * config listener hears fold/unfold. We never ask for POST_NOTIFICATIONS, so
 * its notification stays hidden (it still appears in the Active apps list).
 */
public class WatchService extends Service {
    private static final String CHANNEL = "watch";

    static void start(Context ctx) {
        try {
            ctx.startForegroundService(new Intent(ctx, WatchService.class));
        } catch (IllegalStateException e) {
            // Background start not allowed right now; the next Home or boot retries.
            Log.w("FoldHome", "Cannot start watcher", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(CHANNEL, "Fold watcher", NotificationManager.IMPORTANCE_MIN));
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_status)
                .setContentTitle("Watching fold state")
                .build();
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
