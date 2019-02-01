package org.torproject.android.service.transproxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;
import org.torproject.android.service.OrbotConstants;
import org.torproject.android.service.TorService;
import org.torproject.android.service.TorServiceConstants;
import org.torproject.android.service.util.Prefs;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TorTransProxy implements TorServiceConstants {
	
	private String mSysIptables = null;
	private TorService mTorService = null;
	private File mFileXtables = null;
	
	private final static String ALLOW_LOCAL = " ! -d 127.0.0.1";

	private int mTransProxyPort = TOR_TRANSPROXY_PORT_DEFAULT;
	private int mDNSPort = TOR_DNS_PORT_DEFAULT;

	private Shell mShell;

    public TorTransProxy (TorService torService, File fileXTables) throws IOException
	{
		mTorService = torService;
		mFileXtables = fileXTables;

		// start root shell
		mShell = Shell.startRootShell();

	}

    public static boolean testRoot () throws IOException
    {
        Runtime.getRuntime().exec("su");
        return true;
    }
	
	public void setTransProxyPort (int transProxyPort)
	{
		mTransProxyPort = transProxyPort;
	}
	
	public void setDNSPort (int dnsPort)
	{
		mDNSPort = dnsPort;
	}
	
	public String getIpTablesPath (Context context)
	{

		String ipTablesPath = null;

		if (Prefs.useSystemIpTables())
		{
			ipTablesPath = findSystemIPTables();
		}
		else
		{
			ipTablesPath = mFileXtables.getAbsolutePath();
			ipTablesPath += " iptables"; //append subcommand since we are using xtables now
			
		}
			
		return ipTablesPath;
	}
	
	public String getIp6TablesPath (Context context)
	{

		String ipTablesPath = null;
		
		if (Prefs.useSystemIpTables())
		{
			ipTablesPath = findSystemIP6Tables();
		}
		else
		{
			ipTablesPath = mFileXtables.getAbsolutePath();
			ipTablesPath += " ip6tables"; //append subcommand since we are using xtables now
			
		}
			
		return ipTablesPath;
	
	}
	
	private String findSystemIPTables ()
	{
		if (mSysIptables == null) {
			//if the user wants us to use the built-in iptables, then we have to find it
			File fileIpt = new File("/system/xbin/iptables");

			if (fileIpt.exists())
				mSysIptables = fileIpt.getAbsolutePath();
			else {

				fileIpt = new File("/system/bin/iptables");

				if (fileIpt.exists())
					mSysIptables = fileIpt.getAbsolutePath();
			}
		}

		return mSysIptables;
	}
	

	
	private String findSystemIP6Tables ()
	{
		
		//if the user wants us to use the built-in iptables, then we have to find it
		File fileIpt = new File("/system/xbin/ip6tables");
		
		if (fileIpt.exists())
			mSysIptables = fileIpt.getAbsolutePath();
		else
		{
		
			fileIpt = new File("/system/bin/ip6tables");
			
			if (fileIpt.exists())
				mSysIptables = fileIpt.getAbsolutePath();
		}
		
		
		return mSysIptables;
	}
	
	/*
	public int flushIptablesAll(Context context) throws Exception {
		
		String ipTablesPath = getIpTablesPath(context);
	
    	final StringBuilder script = new StringBuilder();
    	
    	StringBuilder res = new StringBuilder();
    	int code = -1;

		script.append(ipTablesPath);
		script.append(" -t nat");
		script.append(" -F || exit\n");
	
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(" -F || exit\n");
    	
    	String[] cmd = {script.toString()};	    	
		code = TorServiceUtils.doShellCommand(cmd, res, true, true);		
		String msg = res.toString();
		
		TorService.logMessage(cmd[0] + ";errCode=" + code + ";resp=" + msg);
			
		
		return code;
	
	}*/
	
	/*
	public static int purgeIptablesByApp(Context context, TorifiedApp[] apps) throws Exception {

		//restoreDNSResolvConf(); //not working yet
		
		String ipTablesPath = new File(context.getDir("bin", 0),"iptables").getAbsolutePath();
		
    	final StringBuilder script = new StringBuilder();
    	
    	StringBuilder res = new StringBuilder();
    	int code = -1;
    	
		for (int i = 0; i < apps.length; i++)
		{
			//flush nat for every app
			script.append(ipTablesPath);
			script.append(" -t nat -m owner --uid-owner ");
			script.append(tApp.getUid());
			script.append(" -F || exit\n");
    public static ArrayList<TorifiedApp> getApps (Context context, SharedPreferences prefs)
    {

        String tordAppString = prefs.getString(PREFS_KEY_TORIFIED, "");
        String[] tordApps;

        StringTokenizer st = new StringTokenizer(tordAppString,"|");
        tordApps = new String[st.countTokens()];
        int tordIdx = 0;
        while (st.hasMoreTokens())
        {
            tordApps[tordIdx++] = st.nextToken();
        }

        Arrays.sort(tordApps);

        //else load the apps up
        PackageManager pMgr = context.getPackageManager();

        List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);

        Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();

        ArrayList<TorifiedApp> apps = new ArrayList<TorifiedApp>();

        ApplicationInfo aInfo = null;

        int appIdx = 0;
        TorifiedApp app = null;

        while (itAppInfo.hasNext())
        {
            aInfo = itAppInfo.next();

            app = new TorifiedApp();

            try {
                PackageInfo pInfo = pMgr.getPackageInfo(aInfo.packageName, PackageManager.GET_PERMISSIONS);

                if (pInfo != null && pInfo.requestedPermissions != null)
                {
                    for (String permInfo:pInfo.requestedPermissions)
                    {
                        if (permInfo.equals("android.permission.INTERNET"))
                        {
                            app.setUsesInternet(true);

                        }
                    }

                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if ((aInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
            {
                //System app
                app.setUsesInternet(true);
            }


            if (!app.usesInternet())
                continue;
            else
            {
                apps.add(app);
            }


            app.setEnabled(aInfo.enabled);
            app.setUid(aInfo.uid);
            app.setUsername(pMgr.getNameForUid(app.getUid()));
            app.setProcname(aInfo.processName);
            app.setPackageName(aInfo.packageName);

            try
            {
                app.setName(pMgr.getApplicationLabel(aInfo).toString());
            }
            catch (Exception e)
            {
                app.setName(aInfo.packageName);
            }


            //app.setIcon(pMgr.getApplicationIcon(aInfo));

            // check if this application is allowed
            if (Arrays.binarySearch(tordApps, app.getUsername()) >= 0) {
                app.setTorified(true);
            }
            else
            {
                app.setTorified(false);
            }

            appIdx++;
        }

        Collections.sort(apps);

        return apps;
    }

		
			script.append(ipTablesPath);
			script.append(" -t filter -m owner --uid-owner ");
			script.append(tApp.getUid());
			script.append(" -F || exit\n");
				
		}
		
    	
    	String[] cmd = {script.toString()};	    	
		code = TorServiceUtils.doShellCommand(cmd, res, true, true);		
		String msg = res.toString();
		logNotice(cmd[0] + ";errCode=" + code + ";resp=" + msg);
			
		
		return code;
		
	}*/
	
	
	/*
	 // 9/19/2010 - NF This code is in process... /etc path on System partition
	 // is read-only on Android for now.
	public static int redirectDNSResolvConf () throws Exception
	{
    	StringBuilder script = new StringBuilder();
    	StringBuilder res = new StringBuilder();
    	int code = -1;
    	
		//mv resolv.conf to resolve.conf.bak
		String cmd = "mv /etc/resolv.conf /etc/resolv.conf.bak";
		script.append(cmd);
		
		//create new resolve.conf pointing to localhost/127.0.0.1
		cmd = "echo \"nameserver 127.0.0.1\" > /etc/resolv.conf";
		script.append(cmd);
		
		String[] cmdFlush = {script.toString()};
		code = TorServiceUtils.doShellCommand(cmdFlush, res, true, true);
		//String msg = res.toString(); //get stdout from command
		
		
		return code;
	}
	
	public static int restoreDNSResolvConf () throws Exception
	{
		StringBuilder script = new StringBuilder();
    	StringBuilder res = new StringBuilder();
    	int code = -1;
    	
		//mv resolv.conf to resolve.conf.bak
		String cmd = "mv /etc/resolv.conf.bak /etc/resolv.conf";
		script.append(cmd);
		script.append(" || exit\n");
		
		String[] cmdFlush = {script.toString()};
		code = TorServiceUtils.doShellCommand(cmdFlush, res, true, true);
		//String msg = res.toString(); //get stdout from command
		
		return code;
	}
	*/
	/*
	public int testOwnerModule(Context context, String ipTablesPath) throws Exception
	{

		TorBinaryInstaller.assertIpTablesBinaries(context, false);
		
		boolean runRoot = true;
    	boolean waitFor = true;
    	
    	int torUid = context.getApplicationInfo().uid;

    	StringBuilder script = new StringBuilder();
    	
    	StringBuilder res = new StringBuilder();
    	int code = -1;
    	
    	// Allow everything for Tor
		script.append(ipTablesPath);
		script.append(" -A OUTPUT");
		script.append(" -t filter");
		script.append(" -m owner --uid-owner ");
		script.append(torUid);
		script.append(" -j ACCEPT");
		script.append(" || exit\n");
		
		script.append(ipTablesPath);
		script.append(" -D OUTPUT");
		script.append(" -t filter");
		script.append(" -m owner --uid-owner ");
		script.append(torUid);
		script.append(" -j ACCEPT");
		script.append(" || exit\n");
		
		String[] cmdAdd = {script.toString()};    	
    	
		code = TorServiceUtils.doShellCommand(cmdAdd, res, runRoot, waitFor);
		String msg = res.toString();
		
		if (mTorService != null)
		logMessage(cmdAdd[0] + ";errCode=" + code + ";resp=" + msg);
		
		
		return code;
    }	
	*/
	
	/*
	public int clearTransparentProxyingByApp (Context context, ArrayList<TorifiedApp> apps) throws Exception
	{
		boolean runRoot = true;
    	boolean waitFor = true;
    	
		String ipTablesPath = getIpTablesPath(context);
		
    	StringBuilder script = new StringBuilder();
    	
    	StringBuilder res = new StringBuilder();
    	int code = -1;
    	
    	String chainName = "ORBOT";
		String jumpChainName = "OUTPUT";
		
		script.append(ipTablesPath);
    	script.append(" --flush ").append(chainName); //delete previous user-defined chain
    	script.append(" || exit\n");
    	
		script.append(ipTablesPath);
    	script.append(" -D ").append(jumpChainName);
    	script.append(" -j ").append(chainName);
    	script.append(" || exit\n");
    	
    	script.append(ipTablesPath);
    	script.append(" -X ").append(chainName); //delete previous user-defined chain
    	script.append(" || exit\n");

		String[] cmdAdd = {script.toString()};    	
    		
		code = TorServiceUtils.doShellCommand(cmdAdd, res, runRoot, waitFor);
		String msg = res.toString();
		
		logMessage(cmdAdd[0] + ";errCode=" + code + ";resp=" + msg);
		
		return code;
	}*/
	
	public int setTransparentProxyingByApp(Context context, ArrayList<TorifiedApp> apps, boolean enableRule) throws Exception
	{
		String ipTablesPath = getIpTablesPath(context);
		
    	//StringBuilder script = new StringBuilder();
    	
		String action = " -A ";
    	String srcChainName = "OUTPUT";

		if (!enableRule)
			action = " -D ";
		
    	//run the delete commands in a separate process as it might error out
    	//String[] cmdExecClear = {script.toString()};    	    	
		//code = TorServiceUtils.doShellCommand(cmdExecClear, res, runRoot, waitFor);
		
		//reset script
		
    	int lastExit = -1;
    	StringBuilder script;    	
    	

		// Same for DNS
		script = new StringBuilder();
		script.append(ipTablesPath);
		script.append(" -t nat");
		script.append(action).append(srcChainName);
		script.append(" -p udp");
		//script.append(" -m owner --uid-owner ");
		//script.append(tApp.getUid());
		//script.append(" -m udp --dport "); 
		script.append(" --dport ");
		script.append(STANDARD_DNS_PORT);
		script.append(" -j REDIRECT --to-ports ");
		script.append(mDNSPort);
		executeCommand (script.toString());
		
    	// Allow everything for Tor
    	
		//build up array of shell cmds to execute under one root context
		for (TorifiedApp tApp:apps)
		{

			if (((!enableRule) || tApp.isTorified())
					&& (!tApp.getUsername().equals(TOR_APP_USERNAME))
					) //if app is set to true
			{
				
				
				logMessage("transproxy for app: " + tApp.getUsername() + " (" + tApp.getUid() + "): enable=" + enableRule);
				
				dropAllIPv6Traffic(context, tApp.getUid(),enableRule);
				
		    	script = new StringBuilder();

				// Allow loopback
		    	/**
				script.append(ipTablesPath);
				script.append(" -t filter");
		        script.append(action).append(srcChainName);
				script.append(" -m owner --uid-owner ");
				script.append(tApp.getUid());
				script.append(" -o lo");
				script.append(" -j ACCEPT");

				executeCommand (shell, script.toString());
				script = new StringBuilder();
				**/
				
				// Set up port redirection
		    	script.append(ipTablesPath);
		    	script.append(" -t nat");
		    	script.append(action).append(srcChainName);				
				script.append(" -p tcp");
				script.append(ALLOW_LOCAL);
				script.append(" -m owner --uid-owner ");
				script.append(tApp.getUid());
				script.append(" -m tcp --syn");
				script.append(" -j REDIRECT --to-ports ");
				script.append(mTransProxyPort);
				
				executeCommand (script.toString());
				
				
				script = new StringBuilder();
				
				// Reject all other outbound packets
				script.append(ipTablesPath);
				script.append(" -t filter");
		        script.append(action).append(srcChainName);
				script.append(" -m owner --uid-owner ");
				script.append(tApp.getUid());				
				script.append(ALLOW_LOCAL);
				script.append(" -j REJECT");

				lastExit = executeCommand (script.toString());
				
		
			}		
		}		
		
		return lastExit;
    }	
	
	private int executeCommand (String cmdString) throws Exception {

		SimpleCommand command = new SimpleCommand(cmdString);

		mShell.add(command).waitForFinish();

		logMessage("Command Exec: " + cmdString);
		logMessage("Output: " + command.getOutput());
		logMessage("Exit code: " + command.getExitCode());
		
		return 0;
	}

    public void closeShell () throws IOException
    {
        mShell.close();
    }

	public int enableTetheringRules (Context context) throws Exception
	{
		
		String ipTablesPath = getIpTablesPath(context);
		
    	StringBuilder script = new StringBuilder();
    
    	String[] hwinterfaces = {"usb0","wl0.1"};
    	
    	
    	int lastExit = -1;
    	
    	for (int i = 0; i < hwinterfaces.length; i++)
    	{

			script = new StringBuilder();
	    	script.append(ipTablesPath);
			script.append(" -t nat -A PREROUTING -i ");
			script.append(hwinterfaces[i]);
			script.append(" -p udp --dport 53 -j REDIRECT --to-ports ");
			script.append(mDNSPort);
			
			executeCommand (script.toString());
			script = new StringBuilder();
			
			
			script = new StringBuilder();
			script.append(ipTablesPath);
			script.append(" -t nat -A PREROUTING -i ");
			script.append(hwinterfaces[i]);
			script.append(" -p tcp -j REDIRECT --to-ports ");
			script.append(mTransProxyPort);
			
			lastExit = executeCommand (script.toString());
			script = new StringBuilder();
			
			
    	}
    	
		return lastExit;
	}
	
	private void logMessage (String msg)
	{
		if (mTorService != null)
			mTorService.debug(msg);
	}
	

	
	public int fixTransproxyLeak (Context context) throws Exception
	{
		String ipTablesPath = getIpTablesPath(context);
		
    	StringBuilder script = new StringBuilder();
    	script.append(ipTablesPath);
		script.append(" -I OUTPUT ! -o lo ! -d 127.0.0.1 ! -s 127.0.0.1 -p tcp -m tcp --tcp-flags ACK,FIN ACK,FIN -j DROP");
		
		executeCommand (script.toString());
		script = new StringBuilder();
		
		script = new StringBuilder();
		script.append(ipTablesPath);
		script.append(" -I OUTPUT ! -o lo ! -d 127.0.0.1 ! -s 127.0.0.1 -p tcp -m tcp --tcp-flags ACK,RST ACK,RST -j DROP");
		
		int lastExit = executeCommand (script.toString());
		script = new StringBuilder();
		
		return lastExit;
		 
	}
	
	public int dropAllIPv6Traffic (Context context, int appUid, boolean enableDrop) throws Exception {

		String action = " -A ";
		String chain = "OUTPUT";
		
		if (!enableDrop)
			action = " -D ";
		
		String ip6tablesPath = getIp6TablesPath(context);
    	
    	StringBuilder script;

		script = new StringBuilder();
		script.append(ip6tablesPath);			
		script.append(action);
		script.append(chain);

		if (appUid != -1)
		{
			script.append(" -m owner --uid-owner ");
			script.append(appUid);	
		}
		
		script.append(" -j DROP");
		
		int lastExit = executeCommand (script.toString());
		
		return lastExit;
	}
	
	/*
	public int clearAllIPv6Filters (Context context) throws Exception
	{

		String ip6tablesPath = getIp6TablesPath(context);
		Shell shell = Shell.startRootShell();
    	
    	StringBuilder script;

		script = new StringBuilder();
		script.append(ip6tablesPath);			
		script.append(" -t filter");
		script.append(" -F OUTPUT");
		int lastExit = executeCommand (shell, script.toString());
		
		shell.close();
		
		return lastExit;
	}*/
	
	public int flushTransproxyRules (Context context) throws Exception {
		int exit = -1;
		
		String ipTablesPath = getIpTablesPath(context);

		StringBuilder script = new StringBuilder();
		script.append(ipTablesPath);			
		script.append(" -t nat ");
		script.append(" -F ");
		
    	executeCommand (script.toString());
		
		script = new StringBuilder();
		script.append(ipTablesPath);			
		script.append(" -t filter ");
		script.append(" -F ");
		executeCommand (script.toString());
		
		dropAllIPv6Traffic(context,-1,false);
		dropAllIPv6Traffic(context,-1,false);

		
		return exit;
	}
	
	public int setTransparentProxyingAll(Context context, boolean enable) throws Exception
	{
	  	
		String action = " -A ";
    	String srcChainName = "OUTPUT";

		if (!enable)
			action = " -D ";

		dropAllIPv6Traffic(context,-1,enable);
		
		String ipTablesPath = getIpTablesPath(context);
		
    	
    	int torUid = context.getApplicationInfo().uid;
    	
    	StringBuilder script = new StringBuilder();
    	
		// Allow everything for Tor
    	
		script.append(ipTablesPath);			
		script.append(" -t nat");
		script.append(action).append(srcChainName);
		script.append(" -m owner --uid-owner ");
		script.append(torUid);
		script.append(" -j ACCEPT");
		
		executeCommand (script.toString());
		script = new StringBuilder();

		// Allow loopback
		
		script.append(ipTablesPath);
		script.append(" -t nat");
		script.append(action).append(srcChainName);
		script.append(" -o lo");
		script.append(" -j ACCEPT");

		executeCommand (script.toString());
		script = new StringBuilder();
		
    	// Set up port redirection    	
		script.append(ipTablesPath);		
		script.append(" -t nat");
		script.append(action).append(srcChainName);
		script.append(" -p tcp");
		script.append(ALLOW_LOCAL); //allow access to localhost
		script.append(" -m owner ! --uid-owner ");
		script.append(torUid);
		script.append(" -m tcp --syn");
		script.append(" -j REDIRECT --to-ports ");
		script.append(mTransProxyPort);

		executeCommand (script.toString());
		script = new StringBuilder();
		
		// Same for DNS
		script.append(ipTablesPath);
		script.append(" -t nat");
		script.append(action).append(srcChainName);
		script.append(" -p udp");
		script.append(ALLOW_LOCAL); //allow access to localhost
		script.append(" -m owner ! --uid-owner ");
		script.append(torUid);
		//script.append(" -m udp --dport "); 
		script.append(" --dport ");
		script.append(STANDARD_DNS_PORT);
		script.append(" -j REDIRECT --to-ports ");
		script.append(mDNSPort);

		executeCommand (script.toString());
		script = new StringBuilder();
		

        /**
		if (Prefs.useDebugLogging())
		{
			//XXX: Comment the following rules for non-debug builds
			script.append(ipTablesPath);			
			script.append(" -t filter");
			script.append(action).append(srcChainName);
			script.append(" -p udp");
			script.append(" --dport ");
			script.append(STANDARD_DNS_PORT);
			script.append(" -j LOG");
			script.append(" --log-prefix='ORBOT_DNSLEAK_PROTECTION'");
			script.append(" --log-uid");

			executeCommand (script.toString());
			script = new StringBuilder();
			
			script.append(ipTablesPath);			
			script.append(" -t filter");
			script.append(action).append(srcChainName);
	    	script.append(" -p tcp");
			script.append(" -j LOG");
			script.append(" --log-prefix='ORBOT_TCPLEAK_PROTECTION'");
			script.append(" --log-uid");

			executeCommand (script.toString());
			script = new StringBuilder();

		}**/

		//allow access to transproxy port
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(action).append(srcChainName);
		script.append(" -p tcp");
		script.append(" -m tcp");
		script.append(" --dport ").append(mTransProxyPort);
		script.append(" -j ACCEPT");

		executeCommand (script.toString());
		script = new StringBuilder();
		
		//allow access to local HTTP port
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(action).append(srcChainName);
		script.append(" -p tcp");
		script.append(" -m tcp");
		script.append(" --dport ").append(mTorService.getHTTPPort());
		script.append(" -j ACCEPT");

		executeCommand (script.toString());
		script = new StringBuilder();
		
		//allow access to local SOCKS port
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(action).append(srcChainName);
		script.append(" -p tcp");
		script.append(" -m tcp");
		script.append(" --dport ").append(mTorService.getSOCKSPort());
		script.append(" -j ACCEPT");

		executeCommand (script.toString());
		script = new StringBuilder();
		
		//allow access to local DNS port
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(action).append(srcChainName);
		script.append(" -p udp");
		script.append(" -m udp");
		script.append(" --dport ").append(mDNSPort);
		script.append(" -j ACCEPT");

		executeCommand (script.toString());
		script = new StringBuilder();
		
		// Reject all other packets
		script.append(ipTablesPath);
		script.append(" -t filter");
		script.append(action).append(srcChainName);
		script.append(" -m owner ! --uid-owner ");
		script.append(torUid);
		script.append(ALLOW_LOCAL); //allow access to localhost
		script.append(" -j REJECT");

		int lastExit = executeCommand (script.toString());
		
	//	fixTransproxyLeak (context);
		
    	return lastExit;
	}


	public static ArrayList<TorifiedApp> getApps (Context context, SharedPreferences prefs)
	{

		String tordAppString = prefs.getString(OrbotConstants.PREFS_KEY_TORIFIED, "");
		String[] tordApps;

		StringTokenizer st = new StringTokenizer(tordAppString,"|");
		tordApps = new String[st.countTokens()];
		int tordIdx = 0;
		while (st.hasMoreTokens())
		{
			tordApps[tordIdx++] = st.nextToken();
		}

		Arrays.sort(tordApps);

		//else load the apps up
		PackageManager pMgr = context.getPackageManager();

		List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);

		Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();

		ArrayList<TorifiedApp> apps = new ArrayList<TorifiedApp>();

		ApplicationInfo aInfo = null;

		int appIdx = 0;
		TorifiedApp app = null;

		while (itAppInfo.hasNext())
		{
			aInfo = itAppInfo.next();

			app = new TorifiedApp();

			try {
				PackageInfo pInfo = pMgr.getPackageInfo(aInfo.packageName, PackageManager.GET_PERMISSIONS);

				if (pInfo != null && pInfo.requestedPermissions != null)
				{
					for (String permInfo:pInfo.requestedPermissions)
					{
						if (permInfo.equals("android.permission.INTERNET"))
						{
							app.setUsesInternet(true);

						}
					}

				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if ((aInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
			{
				//System app
				app.setUsesInternet(true);
			}


			if (!app.usesInternet())
				continue;
			else
			{
				apps.add(app);
			}


			app.setEnabled(aInfo.enabled);
			app.setUid(aInfo.uid);
			app.setUsername(pMgr.getNameForUid(app.getUid()));
			app.setProcname(aInfo.processName);
			app.setPackageName(aInfo.packageName);

			try
			{
				app.setName(pMgr.getApplicationLabel(aInfo).toString());
			}
			catch (Exception e)
			{
				app.setName(aInfo.packageName);
			}


			//app.setIcon(pMgr.getApplicationIcon(aInfo));

			// check if this application is allowed
			if (Arrays.binarySearch(tordApps, app.getUsername()) >= 0) {
				app.setTorified(true);
			}
			else
			{
				app.setTorified(false);
			}

			appIdx++;
		}

		Collections.sort(apps);

		return apps;
	}

}
