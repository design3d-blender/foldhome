package app.foldhome;

import android.app.PendingIntent;
import android.app.role.RoleManager;
import android.content.Intent;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * One UI Home only allows removing icons/widgets while it is the default home.
 * This tile hands the home role to another launcher for editing (active), and
 * asks for it back when tapped again (inactive).
 */
public class EditTile extends TileService {

    private boolean isHome() {
        return getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_HOME);
    }

    @Override
    public void onStartListening() {
        Tile tile = getQsTile();
        boolean editing = !isHome();
        tile.setState(editing ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setSubtitle(editing ? "Tap when done" : "Off");
        tile.updateTile();
    }

    @Override
    public void onClick() {
        Intent intent = isHome()
                ? new Intent(Settings.ACTION_HOME_SETTINGS)
                : new Intent(this, RoleRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
