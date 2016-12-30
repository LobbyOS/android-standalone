package com.lobby.installer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lobby.installer.Utils;

import java.net.URL;

public class LobbyActivity extends AppCompatActivity implements ViewTreeObserver.OnScrollChangedListener {

    String storageDir = null;
    String dataDir = null;

    String hostname = "127.0.0.1";
    String host = "127.0.0.1:2020";

    TextView preMSG;
    WebView webview;
    Bundle webviewBundle;

    SwipeRefreshLayout swipeLayout;

    boolean dontDestroyServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        storageDir = getFilesDir().getPath();
        dataDir = storageDir + "/Lobby";

        hostname = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_hostname", "127.0.0.1");
        host = hostname + ":" +
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_port", "2020");

        preMSG = (TextView) findViewById(R.id.preMSG);

        webview = (WebView) findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                updateProgress(progress);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result.getExtra();
                Context context = view.getContext();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                context.startActivity(browserIntent);
                return false;
            }
        });

        webview.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Boolean override = false;
                try {
                    URL u = new URL(url);
                    if (u.getHost().equals(hostname)) {
                        view.loadUrl(url);
                    } else {
                        Context context = view.getContext();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                        override = true;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return override;
            }
        });

        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeLayout.setEnabled(false);

        webview.getViewTreeObserver().addOnScrollChangedListener(this);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
                swipeLayout.setRefreshing(false);
            }
        });

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            webview.restoreState(savedInstanceState.getBundle("webviewBundle"));
            preMSG.setVisibility(View.GONE);
            webview.setVisibility(View.VISIBLE);
        }else {
            launchLobby();
        }
    }

    @Override
    public void onScrollChanged() {
        Log.d("s", String.valueOf(webview.getScrollY()));
        if(webview.getScrollY() > 0)
            swipeLayout.setEnabled(false);
        else
            swipeLayout.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dontDestroyServer = true;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.refresh:
                preMSG.setVisibility(View.GONE);
                webview.reload();
                return true;
            case R.id.openInBrowser:
                String url = "http://" + host;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
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
    }

        /**
    @Override
    public void onDestroy(){
        super.onDestroy();

        if(!dontDestroyServer)
            stopServer();
    }
         */

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
        preMSG.setVisibility(View.VISIBLE);
        startServer(dataDir, host);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        webview.loadUrl("http://" + host);
                        preMSG.setVisibility(View.GONE);
                        webview.setVisibility(View.VISIBLE);
                    }
                },
                1000);
    }

    public static void startServer(String dataDir1, String host1){
        final String dataDir = dataDir1;
        final String host = host1;

        Log.d("cccc", "bbbb");

        Runnable myRunnable = new Runnable() {
            public void run() {
                Utils.executeCommand("sh", dataDir + "/php/start-server.sh", host);
            }
        };
        new Thread(myRunnable).start();
    }

}
