package com.lobby.lobby;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.lobby.lobby.Utils;

public class LobbyActivity extends AppCompatActivity {

    String storageDir = null;
    String dataDir = null;

    String host = "127.0.0.1:2020";

    WebView webview;
    Bundle webviewBundle;

    boolean openInBrowser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        storageDir = getFilesDir().getPath();
        dataDir = storageDir + "/Lobby";
        host = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_hostname", "127.0.0.1") + ":" +
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_port", "2020");

        webview = (WebView) findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                updateProgress(progress);
            }
        });

        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            webview.restoreState(savedInstanceState.getBundle("webviewBundle"));
        }else {
            launchLobby();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.openInBrowser:
                openInBrowser = true;
                String url = "http://" + host;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            case R.id.restartServer:
                stopServer();
                launchLobby();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webview.canGoBack()) {
                        webview.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getSupportActionBar().show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        webviewBundle = new Bundle();
        webview.saveState(webviewBundle);
        if(openInBrowser)
            stopServer();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current Lobby page
        webviewBundle = new Bundle();
        webview.saveState(webviewBundle);
        savedInstanceState.putBundle("webviewBundle", webviewBundle);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateProgress(int progress){
        final ProgressBar Pbar = (ProgressBar) findViewById(R.id.browserProgress);
        if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE){
            Pbar.setVisibility(ProgressBar.VISIBLE);
        }

        Pbar.setProgress(progress);
        if(progress == 100) {
            Pbar.setVisibility(ProgressBar.GONE);
        }
    }

    public void stopServer(){
        Utils.executeCommand("sh", dataDir + "/php/stop-server.sh");
    }

    /**
     * Start server and load webview
     */
    public void launchLobby(){
        webview.loadData("Starting server...", "text/html; charset=utf-8", "UTF-8");
        Runnable myRunnable = new Runnable() {
            public void run() {
                Utils.executeCommand("sh", dataDir + "/php/start-server.sh", host);
            }
        };
        new Thread(myRunnable).start();
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        webview.loadUrl("http://" + host);
    }
}
