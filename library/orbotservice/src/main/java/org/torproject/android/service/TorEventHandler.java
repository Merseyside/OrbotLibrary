package org.torproject.android.service;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;


import org.torproject.android.control.EventHandler;
import org.torproject.android.service.util.Prefs;

/**
 * Created by n8fr8 on 9/25/16.
 */
public class TorEventHandler implements EventHandler, TorServiceConstants {

    private TorService mService;


    private long lastRead = -1;
    private long lastWritten = -1;
    private long mTotalTrafficWritten = 0;
    private long mTotalTrafficRead = 0;

    private NumberFormat mNumberFormat = null;


    private HashMap<String,Node> hmBuiltNodes = new HashMap<String,Node>();

    public class Node
    {
        String status;
        String id;
        String name;
        String ipAddress;
        String country;
        String organization;
    }

    public HashMap<String,Node> getNodes ()
    {
        return hmBuiltNodes;
    }

    public TorEventHandler (TorService service)
    {
        mService = service;
        mNumberFormat = NumberFormat.getInstance(Locale.getDefault()); //localized numbers!

    }

    @Override
    public void message(String severity, String msg) {
        mService.logNotice(severity + ": " + msg);
    }

    @Override
    public void newDescriptors(List<String> orList) {
    }

    @Override
    public void orConnStatus(String status, String orName) {

        StringBuilder sb = new StringBuilder();
        sb.append("orConnStatus (");
        sb.append(parseNodeName(orName) );
        sb.append("): ");
        sb.append(status);

        mService.debug(sb.toString());
    }

    @Override
    public void streamStatus(String status, String streamID, String target) {

        StringBuilder sb = new StringBuilder();
        sb.append("StreamStatus (");
        sb.append((streamID));
        sb.append("): ");
        sb.append(status);

        mService.logNotice(sb.toString());
    }

    @Override
    public void unrecognized(String type, String msg) {

        StringBuilder sb = new StringBuilder();
        sb.append("Message (");
        sb.append(type);
        sb.append("): ");
        sb.append(msg);

        mService.logNotice(sb.toString());
    }

    @Override
    public void bandwidthUsed(long read, long written) {

        if (read != lastRead || written != lastWritten)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(formatCount(read));
            sb.append(" \u2193");
            sb.append(" / ");
            sb.append(formatCount(written));
            sb.append(" \u2191");

            int iconId = R.drawable.ic_stat_tor;

            if (read > 0 || written > 0)
                iconId = R.drawable.ic_stat_tor_xfer;

            mService.showToolbarNotification(sb.toString(), mService.getNotifyId(), iconId);

            mTotalTrafficWritten += written;
            mTotalTrafficRead += read;
        }

        lastWritten = written;
        lastRead = read;

        mService.sendCallbackBandwidth(lastWritten, lastRead, mTotalTrafficWritten, mTotalTrafficRead);
    }

    private String formatCount(long count) {
        // Converts the supplied argument into a string.

        // Under 2Mb, returns "xxx.xKb"
        // Over 2Mb, returns "xxx.xxMb"
        if (mNumberFormat != null)
            if (count < 1e6)
                return mNumberFormat.format(Math.round((float)((int)(count*10/1024))/10)) + "kbps";
            else
                return mNumberFormat.format(Math.round((float)((int)(count*100/1024/1024))/100)) + "mbps";
        else
            return "";

        //return count+" kB";
    }

    public void circuitStatus(String status, String circID, String path) {

        /* once the first circuit is complete, then announce that Orbot is on*/
        if (mService.getCurrentStatus() == STATUS_STARTING && TextUtils.equals(status, "BUILT"))
            mService.sendCallbackStatus(STATUS_ON);

        StringBuilder sb = new StringBuilder();
        sb.append("Circuit (");
        sb.append((circID));
        sb.append(") ");
        sb.append(status);
        sb.append(": ");

        StringTokenizer st = new StringTokenizer(path,",");
        Node node = null;

        while (st.hasMoreTokens())
        {
            String nodePath = st.nextToken();
            node = new Node();

            String[] nodeParts;

            if (nodePath.contains("="))
                nodeParts = nodePath.split("=");
            else
                nodeParts = nodePath.split("~");

            if (nodeParts.length == 1)
            {
                node.id = nodeParts[0].substring(1);
                node.name = node.id;
            }
            else if (nodeParts.length == 2)
            {
                node.id = nodeParts[0].substring(1);
                node.name = nodeParts[1];
             }

            node.status = status;

            sb.append(node.name);

            if (st.hasMoreTokens())
                sb.append (" > ");
        }

        if (Prefs.useDebugLogging())
            mService.debug(sb.toString());
        else if(status.equals("BUILT"))
            mService.logNotice(sb.toString());
        else if (status.equals("CLOSED"))
            mService.logNotice(sb.toString());

        if (Prefs.expandedNotifications())
        {
            //get IP from last nodename
            if(status.equals("BUILT")){

               // if (node.ipAddress == null)
                 //   mService.exec(new ExternalIPFetcher(node));

                hmBuiltNodes.put(circID, node);
            }

            if (status.equals("CLOSED"))
            {
                hmBuiltNodes.remove(circID);

            }
        }

    }

    private String parseNodeName(String node)
    {
        if (node.indexOf('=')!=-1)
        {
            return (node.substring(node.indexOf("=")+1));
        }
        else if (node.indexOf('~')!=-1)
        {
            return (node.substring(node.indexOf("~")+1));
        }
        else
            return node;
    }
}
