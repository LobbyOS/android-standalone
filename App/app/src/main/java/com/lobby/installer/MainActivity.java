package com.lobby.installer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    String storageDir = null;
    String dataDir = null;

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageDir = getFilesDir().getPath();
        dataDir = storageDir + "/Lobby";

        openLobby();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    runInstallLobby();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Can't Install Lobby")
                            .setMessage("Can't install Lobby as permission to write to storage was denied")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    // permission denied, boo!
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void showMSG(String msg){
        TextView tv = (TextView) findViewById(R.id.msg);
        tv.setText(msg);
    }

    /**
     * See what to do : install Lobby or run Lobby
     * @return string install | launch
     */
    public String whatToDo() {
        /**
         * Checks if external storage is available for read and write
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                showMSG("Cannot write to external storage");
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                // is to know is we can neither read nor write
                showMSG(state);
            }
        } else {
            if (isLobbyInstalled()) {
                return "launch";
            } else {
                /**
                 * Ask for Storage permission
                 */
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Permissions is granted
                    return "install";
                } else {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        showMSG("We really need the storage permission to run this app.");

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        // PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                        return "wait";
                    }
                }
            }
        }
        return "wait";
    }

    public void openLobby() {
        String status = whatToDo();

        if(status.equals("install")){
            runInstallLobby();
        }else if(status.equals("launch")){
            Intent intent = new Intent(this, LobbyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            overridePendingTransition(0, 0);
            startActivityForResult(intent, 0);
            finish();
        }
    }

    /**
     * Get PHP command line
     * @return command to run PHP
     */
    public static String executePHPCommand(String dataDir, String... args){
        ArrayList<String> command = new ArrayList<String>();
        command.add("sh");
        command.add(dataDir + "/php/php");

        for (int i=0;i < args.length;i++) {
            command.add(args[i]);
        }

        String[] commandArray = command.toArray(new String[0]);
        return Utils.executeCommand(commandArray);
    }

    public void runInstallLobby(){
        showMSG("Installing Lobby... This might take about a maximum of 15 seconds.");
        runOnUiThread(
                new Runnable() {
                    public void run() {
                        installLobby();
                    }
                }
        );
        checkInstallCompleted();
    }

    public boolean isLobbyInstalled(){
        File file = new File(dataDir + "/lobby/config.php");
        return file.exists();
    }

    /**
     * Check if Lobby has been installed
     * If installed, launch Lobby
     */
    public void checkInstallCompleted(){
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (isLobbyInstalled())
                                    openLobby();
                                else
                                    checkInstallCompleted();
                            }
                        }
                );
            }
        }, 1000);
    }

    /**
     * Copy assets to data dir and make scripts executable
     */
    public void installLobby() {
        Log.d("Install", "Copying assets");

        copyAssets(storageDir);

        try {
            Utils.unzip(new File(storageDir + "/Lobby.zip"), new File(storageDir));
        }catch(Exception e){
            e.printStackTrace();
        }

        File file = new File(storageDir + "/Lobby.zip");
        file.delete();

        if(!new File(dataDir + "/php/php").exists()) {
            Intent intent = new Intent(this, PHPInstallActivity.class);
            intent.putExtra("dataDir", dataDir);
            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }
            );
            startActivity(intent);
        }else {
            finishInstallLobby(dataDir);
        }
    }

    public static void finishInstallLobby(String dataDir){
        try {
            Utils.replaceInFile(dataDir + "/php/php", "<?PHP_DIR?>", dataDir + "/php");
            Utils.replaceInFile(dataDir + "/php/php.ini", "<?DATA_DIR?>", dataDir);
            Utils.replaceInFile(dataDir + "/php/start-server.sh", "<?DATA_DIR?>", dataDir);

            Utils.executeCommand("chmod", "777",
                    dataDir + "/php/php",
                    dataDir + "/php/php-cli",
                    dataDir + "/php/start-server.sh",
                    dataDir + "/php/stop-server.sh"
            );

            /**
             * Install Lobby i.e make config.php
             */
            executePHPCommand(dataDir, dataDir + "/lobby/lobby.php", "install");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyAssets(String destination) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(destination, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.lobby.installer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.lobby.installer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
