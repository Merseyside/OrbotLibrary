/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package org.torproject.android.service.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import org.torproject.android.service.OrbotConstants;
import org.torproject.android.service.R;
import org.torproject.android.service.TorServiceConstants;

import java.io.*;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TorResourceInstaller implements TorServiceConstants {

    
    File installFolder;
    Context context;
    
    public TorResourceInstaller(Context context, File installFolder)
    {
        this.installFolder = installFolder;
        
        this.context = context;
    }
    
    public void deleteDirectory(File file) {
        if( file.exists() ) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
                
            file.delete();
        }
    }
    
    private final static String COMMAND_RM_FORCE = "rm -f ";
    private final static String MP3_EXT = ".mp3";
    //        
    /*
     * Extract the Tor resources from the APK file using ZIP
     */
    public boolean installResources () throws IOException, FileNotFoundException, TimeoutException
    {
        
        InputStream is;
        File outFile;

        String cpuPath = "armeabi";
            
        if (Build.CPU_ABI.contains("x86"))
        	cpuPath = "x86";

        deleteDirectory(installFolder);
        
        installFolder.mkdirs();

        is = context.getResources().openRawResource(R.raw.torrc);
        outFile = new File(installFolder, TORRC_ASSET_KEY);
        streamToFile(is,outFile, false, false);
        
        is = context.getResources().openRawResource(R.raw.torpolipo);
        outFile = new File(installFolder, POLIPOCONFIG_ASSET_KEY);
        streamToFile(is,outFile, false, false);

        is = context.getAssets().open(cpuPath + '/' + OBFSCLIENT_ASSET_KEY + MP3_EXT);
        outFile = new File(installFolder, OBFSCLIENT_ASSET_KEY);
        streamToFile(is,outFile, false, true);
        setExecutable(outFile);

        is = context.getAssets().open(cpuPath + '/' + TOR_ASSET_KEY + MP3_EXT);
        outFile = new File(installFolder, TOR_ASSET_KEY);
        streamToFile(is,outFile, false, true);
        setExecutable(outFile);
    
        is = context.getAssets().open(cpuPath + '/' + POLIPO_ASSET_KEY + MP3_EXT);
        outFile = new File(installFolder, POLIPO_ASSET_KEY);
        streamToFile(is,outFile, false, true);
        setExecutable(outFile);
    
        is = context.getAssets().open(cpuPath + '/' + IPTABLES_ASSET_KEY + MP3_EXT);
        outFile = new File(installFolder, IPTABLES_ASSET_KEY);
        streamToFile(is,outFile, false, true);
        setExecutable(outFile);
        
        is = context.getAssets().open(cpuPath + '/' + PDNSD_ASSET_KEY + MP3_EXT);
        outFile = new File(installFolder, PDNSD_ASSET_KEY);
        streamToFile(is,outFile, false, true);
        setExecutable(outFile);
        
        installGeoIP();
    
        return true;
    }
    
    public boolean updateTorConfigCustom (File fileTorRcCustom, String extraLines) throws IOException, FileNotFoundException, TimeoutException
    {
    	if (fileTorRcCustom.exists())
    	{
    		fileTorRcCustom.delete();
    		Log.d("torResources","deleting existing torrc.custom");
    	}
    	else
    		fileTorRcCustom.createNewFile();
    	
    	FileOutputStream fos = new FileOutputStream(fileTorRcCustom, false);
    	PrintStream ps = new PrintStream(fos);
    	ps.print(extraLines);
    	ps.close();
    	
        return true;
    }
    
    public boolean updatePolipoConfig (File filePolipo, String extraLines) throws IOException, FileNotFoundException, TimeoutException
    {
        
        InputStream is;
        

        is = context.getResources().openRawResource(R.raw.torpolipo);
        streamToFile(is,filePolipo, false, false);

        if (extraLines != null && extraLines.length() > 0)
        {
            StringBufferInputStream sbis = new StringBufferInputStream('\n' + extraLines + '\n');
            streamToFile(sbis,filePolipo,true,false);
        }
        

        return true;
    }
    
    public boolean installPolipoConf () throws IOException, FileNotFoundException, TimeoutException
    {
        
        InputStream is;
        File outFile;
        

        is = context.getResources().openRawResource(R.raw.torpolipo);
        outFile = new File(installFolder, POLIPOCONFIG_ASSET_KEY);
        streamToFile(is,outFile, false, false);
        
        return true;
    }
    
    /*
     * Extract the Tor binary from the APK file using ZIP
     */
    
    private boolean installGeoIP () throws IOException, FileNotFoundException
    {
        
        InputStream is;
        File outFile;
        
        outFile = new File(installFolder, GEOIP_ASSET_KEY);
        is = context.getResources().openRawResource(R.raw.geoip);
        streamToFile(is, outFile, false, true);
        
        is = context.getResources().openRawResource(R.raw.geoip6);
        outFile = new File(installFolder, GEOIP6_ASSET_KEY);
        streamToFile(is, outFile, false, true);
    
        return true;
    }
    
    /*
    private static void copyAssetFile(Context ctx, String asset, File file) throws IOException, InterruptedException
    {
        
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        InputStream is = new GZIPInputStream(ctx.getAssets().open(asset));
        
        byte buf[] = new byte[8172];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
    }*/
    
    /*
     * Write the inputstream contents to the file
     */
    public static boolean streamToFile(InputStream stm, File outFile, boolean append, boolean zip) throws IOException

    {
        byte[] buffer = new byte[FILE_WRITE_BUFFER_SIZE];

        int bytecount;

        OutputStream stmOut = new FileOutputStream(outFile.getAbsolutePath(), append);
        ZipInputStream zis = null;
        
        if (zip)
        {
            zis = new ZipInputStream(stm);            
            ZipEntry ze = zis.getNextEntry();
            stm = zis;
            
        }
        
        while ((bytecount = stm.read(buffer)) > 0)
        {

            stmOut.write(buffer, 0, bytecount);

        }

        stmOut.close();
        stm.close();
        
        if (zis != null)
            zis.close();
        
        
        return true;

    }
    
    //copy the file from inputstream to File output - alternative impl
    public static boolean copyFile (InputStream is, File outputFile)
    {
        
        try {
        	if (outputFile.exists())
        		outputFile.delete();
        	
            boolean newFile = outputFile.createNewFile();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
            DataInputStream in = new DataInputStream(is);
            
            int b = -1;
            byte[] data = new byte[1024];
            
            while ((b = in.read(data)) != -1) {
                out.write(data);
            }
            
            if (b == -1); //rejoice
            
            //
            out.flush();
            out.close();
            in.close();
            // chmod?
            
            return newFile;
            
            
        } catch (IOException ex) {
            Log.e(OrbotConstants.TAG, "error copying binary", ex);
            return false;
        }

    }
    
    

   
    /**
     * Copies a raw resource file, given its ID to the given location
     * @param ctx context
     * @param resid resource id
     * @param file destination file
     * @param mode file permissions (E.g.: "755")
     * @throws IOException on error
     * @throws InterruptedException when interrupted
     */
    public static void copyRawFile(Context ctx, int resid, File file, String mode, boolean isZipd) throws IOException, InterruptedException
    {
        final String abspath = file.getAbsolutePath();
        // Write the iptables binary
        final FileOutputStream out = new FileOutputStream(file);
        InputStream is = ctx.getResources().openRawResource(resid);
        
        if (isZipd)
        {
            ZipInputStream zis = new ZipInputStream(is);            
            ZipEntry ze = zis.getNextEntry();
            is = zis;
        }
        
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        // Change the permissions
        Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
    }
    /**
     * Asserts that the binary files are installed in the bin directory.
     * @param ctx context
     * @param showErrors indicates if errors should be alerted
     * @return false if the binary files could not be installed
     */
    /*
    public static boolean assertIpTablesBinaries(Context ctx, boolean showErrors) throws Exception {
        boolean changed = false;
        
        // Check iptables_g1
        File file = new File(ctx.getDir("bin",0), "iptables");
        copyRawFile(ctx, R.raw.iptables, file, CHMOD_EXEC, false);
                
        return true;
    }*/
    

    private void setExecutable(File fileBin) {
        fileBin.setReadable(true);
        fileBin.setExecutable(true);
        fileBin.setWritable(false);
        fileBin.setWritable(true, true);
    }

}
