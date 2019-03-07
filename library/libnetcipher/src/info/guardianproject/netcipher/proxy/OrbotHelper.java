
package info.guardianproject.netcipher.proxy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class OrbotHelper implements ProxyHelper {

    private final static int REQUEST_CODE_STATUS = 100;

    public final static String ORBOT_PACKAGE_NAME = "tor.view";
    public final static String ORBOT_MARKET_URI = "market://details?id=" + ORBOT_PACKAGE_NAME;
    public final static String ORBOT_FDROID_URI = "https://f-droid.org/repository/browse/?fdid="
            + ORBOT_PACKAGE_NAME;
    public final static String ORBOT_PLAY_URI = "https://play.google.com/store/apps/details?id="
            + ORBOT_PACKAGE_NAME;


    public final static String ACTION_START = "org.torproject.android.intent.action.START";

    public final static String ACTION_STATUS = "org.torproject.android.intent.action.STATUS";

    public final static String EXTRA_STATUS = "org.torproject.android.intent.extra.STATUS";

    public final static String EXTRA_PACKAGE_NAME = "org.torproject.android.intent.extra.PACKAGE_NAME";


    public final static String STATUS_OFF = "OFF";

    public final static String STATUS_ON = "ON";
    public final static String STATUS_STARTING = "STARTING";
    public final static String STATUS_STOPPING = "STOPPING";

    public final static String STATUS_STARTS_DISABLED = "STARTS_DISABLED";

    public final static String ACTION_START_TOR = "org.torproject.android.START_TOR";
    public final static String ACTION_REQUEST_HS = "org.torproject.android.REQUEST_HS_PORT";
    public final static int START_TOR_RESULT = 0x9234;
    public final static int HS_REQUEST_CODE = 9999;


    private OrbotHelper() {
        // only static utility methods, do not instantiate
    }

    /**
     * Test whether a {@link URL} is a Tor Hidden Service host name, also known
     * as an ".onion address".
     *
     * @return whether the host name is a Tor .onion address
     */
    public static boolean isOnionAddress(URL url) {
        return url.getHost().endsWith(".onion");
    }


    public static boolean isOnionAddress(String urlString) {
        try {
            return isOnionAddress(new URL(urlString));
        } catch (MalformedURLException e) {
            return false;
        }
    }


    public static boolean isOnionAddress(Uri uri) {
        return uri.getHost().endsWith(".onion");
    }

    /*
    @Deprecated
    public static boolean isOrbotRunning(Context context) {
        int procId = TorServiceUtils.findProcessId(context);

        return (procId != -1);
    }*/






    public static boolean isOrbotInstalled(Context context) {
        //return isAppInstalled(context, ORBOT_PACKAGE_NAME);
        return true;
    }

    private static boolean isAppInstalled(Context context, String uri) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void requestHiddenServiceOnPort(AppCompatActivity activity, int port) {
        Intent intent = new Intent(ACTION_REQUEST_HS);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.putExtra("hs_port", port);

        activity.startActivityForResult(intent, HS_REQUEST_CODE);
    }


    public static boolean requestStartTor(Context context) {
        if (OrbotHelper.isOrbotInstalled(context)) {
            Log.i("OrbotHelper", "requestStartTor " + context.getPackageName());
            Intent intent = getOrbotStartIntent(context);
            context.sendBroadcast(intent);
            return true;
        }
        return false;
    }




    public static Intent getOrbotStartIntent(Context context) {
        Intent intent = new Intent(ACTION_START);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.putExtra(EXTRA_PACKAGE_NAME, context.getPackageName());
        return intent;
    }


    @Deprecated
    public static Intent getOrbotStartIntent() {
        Intent intent = new Intent(ACTION_START);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        return intent;
    }


    public static boolean requestShowOrbotStart(AppCompatActivity activity) {
        if (OrbotHelper.isOrbotInstalled(activity)) {
            /*if (!OrbotHelper.isOrbotRunning(activity)) {
                Intent intent = getShowOrbotStartIntent();
                activity.startActivityForResult(intent, START_TOR_RESULT);
                return true;
            }*/
        }
        return false;
    }

    public static Intent getShowOrbotStartIntent() {
        Intent intent = new Intent(ACTION_START_TOR);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getOrbotInstallIntent(Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ORBOT_MARKET_URI));

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(intent, 0);

        String foundPackageName = null;
        for (ResolveInfo r : resInfos) {
            Log.i("OrbotHelper", "market: " + r.activityInfo.packageName);
            if (TextUtils.equals(r.activityInfo.packageName, FDROID_PACKAGE_NAME)
                    || TextUtils.equals(r.activityInfo.packageName, PLAY_PACKAGE_NAME)) {
                foundPackageName = r.activityInfo.packageName;
                break;
            }
        }

        if (foundPackageName == null) {
            intent.setData(Uri.parse(ORBOT_FDROID_URI));
        } else {
            intent.setPackage(foundPackageName);
        }
        return intent;
    }

	@Override
	public boolean isInstalled(Context context) {
		return isOrbotInstalled(context);
	}

	@Override
	public void requestStatus(Context context) { 
		//isOrbotRunning(context);
	}

	@Override
	public boolean requestStart(Context context) {
		return requestStartTor(context);
	}

	@Override
	public Intent getInstallIntent(Context context) {
		return getOrbotInstallIntent(context);
	}

	@Override
	public Intent getStartIntent(Context context) {
		return getOrbotStartIntent();
	}
	
	@Override
	public String getName() {
		return "Orbot";
	}
}
