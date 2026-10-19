package app.foldhome;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * Android blocks activity starts from the background. An app holding "Appear on
 * top" is exempt while it has an overlay window on screen, so we show an
 * invisible 1px overlay just long enough to open the launcher.
 */
final class BackgroundLaunch {
    private static final String TAG = "FoldHome";
    private static final long OVERLAY_LINGER_MS = 500;

    static void openLauncher(Context ctx) {
        if (!Settings.canDrawOverlays(ctx)) {
            Log.w(TAG, "No 'Appear on top' permission; launch will likely be blocked");
            Launcher.open(ctx);
            return;
        }

        WindowManager wm = ctx.getSystemService(WindowManager.class);
        View pixel = new View(ctx);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.alpha = 0f;

        // Launch only once the overlay's first frame is on screen, so the system sees it as visible.
        pixel.getViewTreeObserver().registerFrameCommitCallback(() -> {
            Launcher.open(ctx);
            new Handler(Looper.getMainLooper()).postDelayed(() -> wm.removeView(pixel), OVERLAY_LINGER_MS);
        });
        wm.addView(pixel, lp);
    }

    private BackgroundLaunch() {}
}
