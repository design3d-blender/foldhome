package app.foldhome;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Picks a launcher per fold state and makes FoldHome the default home app. */
public class SettingsActivity extends Activity {
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.setFitsSystemWindows(true);
        scroll.addView(root);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WatchService.start(this);
        build();
    }

    private void build() {
        root.removeAllViews();
        List<ResolveInfo> launchers = otherLaunchers();

        addSection("Folded (cover screen)", Prefs.FOLDED, launchers);
        addSection("Unfolded (inner screen)", Prefs.UNFOLDED, launchers);

        RoleManager roles = getSystemService(RoleManager.class);
        boolean isHome = roles.isRoleHeld(RoleManager.ROLE_HOME);
        TextView status = new TextView(this);
        status.setText(isHome ? "FoldHome is the default home app." : "FoldHome is not the default home app yet.");
        status.setPadding(0, dp(24), 0, dp(8));
        root.addView(status);

        if (!isHome) {
            Button makeDefault = new Button(this);
            makeDefault.setText("Set FoldHome as default home");
            makeDefault.setOnClickListener(v ->
                    startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_HOME)));
            root.addView(makeDefault);
        }

        AppOpsManager appOps = getSystemService(AppOpsManager.class);
        boolean hasUsage = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
        if (!hasUsage) {
            TextView usage = new TextView(this);
            usage.setText("Grant Usage access so folding on the home screen switches launcher instantly.");
            usage.setPadding(0, dp(16), 0, dp(8));
            root.addView(usage);
            Button grant = new Button(this);
            grant.setText("Open Usage access settings");
            grant.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
            root.addView(grant);
        }

        if (!Settings.canDrawOverlays(this)) {
            TextView overlay = new TextView(this);
            overlay.setText("Allow 'Appear on top' so FoldHome can open the other launcher when you fold.");
            overlay.setPadding(0, dp(16), 0, dp(8));
            root.addView(overlay);
            Button grant = new Button(this);
            grant.setText("Open Appear on top settings");
            grant.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()))));
            root.addView(grant);
        }
    }

    private void addSection(String title, String key, List<ResolveInfo> launchers) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        header.setPadding(0, dp(16), 0, dp(4));
        root.addView(header);

        ComponentName selected = Prefs.get(this, key);
        RadioGroup group = new RadioGroup(this);
        PackageManager pm = getPackageManager();
        for (ResolveInfo info : launchers) {
            ActivityInfo ai = info.activityInfo;
            ComponentName cn = new ComponentName(ai.packageName, ai.name);
            RadioButton button = new RadioButton(this);
            button.setId(View.generateViewId());
            button.setText(info.loadLabel(pm));
            button.setChecked(cn.equals(selected));
            button.setOnClickListener(v -> Prefs.set(this, key, cn));
            group.addView(button);
        }
        root.addView(group);
    }

    /** Every installed home activity except our own proxy and the system's boot-time FallbackHome. */
    private List<ResolveInfo> otherLaunchers() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> result = new ArrayList<>();
        for (ResolveInfo info : getPackageManager().queryIntentActivities(home, PackageManager.MATCH_ALL)) {
            if (!info.activityInfo.packageName.equals(getPackageName()) && info.priority >= 0) {
                result.add(info);
            }
        }
        return result;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
