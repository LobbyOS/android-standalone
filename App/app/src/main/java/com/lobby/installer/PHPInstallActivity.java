package com.lobby.installer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Arrays;

public class PHPInstallActivity extends AppCompatActivity {

    String dataDir = null;

    String filesURL = null;
    String[] hashes = null;
    Future<File> downloadPHP = null;

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phpinstall);

        dataDir = getIntent().getStringExtra("dataDir");

        spinner = (Spinner) findViewById(R.id.archs);
        adapter = ArrayAdapter.createFromResource(this, R.array.archs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /**
         * Remember NikiC, Tautvydas Andrikys, ztguang [NTZ]
         */
        filesURL = getString(R.string.lobbyServer) + "/services/android";

        getHashes();
        installPHP();
    }

    public void showMSG(String msg){
        TextView tv = (TextView) findViewById(R.id.msg);
        tv.setText(msg);
    }

    public void installPHP(){
        String arch = Utils.getArch();
        spinner.setSelection(adapter.getPosition(arch));
        showMSG(String.format(getResources().getString(R.string.arch_info), arch));
    }

    public void startLobby(){
        Intent intent = new Intent(PHPInstallActivity.this, MainActivity.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
        startActivity(intent);
    }

    public void setHashes(String[] inputHashes){
        hashes = inputHashes;
    }

    private void getHashes(){
        Ion.with(getApplicationContext())
                .load(filesURL + "/MD5SUMS").asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String r) {
                        final String result = r;
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              try {
                                                  setHashes(result.split("\\s+"));
                                              }catch(Exception e2){
                                                  setHashes(null);
                                              }
                                          }
                                      }
                        );
                    }
                });
    }

    public void onDownloadClick(View btn) {
        getHashes();

        String fileName = "php-arm";
        if(spinner.getSelectedItem().equals("x86")){
            fileName = "php-x86";
        }

        /**
         * A bug exist on API 22. So a separate PHP is available for it
         */
        if(Build.VERSION.SDK_INT == 22)
            fileName += "-22";

        fileName += ".zip";

        final String saveTo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        final String url = filesURL + "/" + fileName;
        final TextView tv = (TextView) findViewById(R.id.downloadText);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        if(hashes == null) {
            tv.setText("Internet connection not available. Make sure you are connected to the internet.");
        }else {
            /**
             * File Hash
             */
            String requiredHash = null;

            if(fileName.equals("php-arm.zip")) {
                requiredHash = hashes[0];
            }else if(fileName.equals("php-arm-22.zip")) {
                requiredHash = hashes[2];
            }else if(fileName.equals("php-x86.zip")) {
                requiredHash = hashes[4];
            }else{
                requiredHash = hashes[6];
            }

            final String fileRequiredHash = requiredHash;

            File userDownloaded = new File(saveTo);
            if (userDownloaded.exists() && MD5.checkMD5(fileRequiredHash, userDownloaded)) {
                extractPHP(dataDir, userDownloaded);
                MainActivity.finishInstallLobby(dataDir);
                startLobby();
            }

            Log.d("downloading", "Downloading from " + url + " to " + saveTo);

            if(downloadPHP != null)
                downloadPHP.cancel();

            downloadPHP = Ion.with(getApplicationContext())
                    .load(url)
                    .progress(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            if (total == -1)
                                total = 7000000;

                            final String downloadedF = Utils.humanReadableSize(downloaded, true);
                            final String totalF = Utils.humanReadableSize(total, true);

                            final long numerator = downloaded;
                            final long denominator = total;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((ProgressBar) findViewById(R.id.downloadProgress))
                                            .setProgress((int) (numerator * 100.0 / denominator + 0.5));
                                    tv.setText(String.format(
                                            getResources().getString(R.string.downloaded),
                                            downloadedF,
                                            totalF
                                    ));
                                }
                            });
                        }
                    })
                    .write(new File(saveTo))
                    .setCallback(new FutureCallback<File>() {
                        @Override
                        public void onCompleted(Exception e, File response) {
                            File file = new File(saveTo);
                            if (e == null && MD5.checkMD5(fileRequiredHash, file)) {
                                extractPHP(dataDir, file);
                                MainActivity.finishInstallLobby(dataDir);
                                startLobby();
                            } else {
                                file.delete();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setText(Html.fromHtml(String.format(
                                                getResources().getString(R.string.download_failure),
                                                url,
                                                saveTo
                                        )));
                                    }
                                });
                                // e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public static void extractPHP(String dataDir, File file){
        try {
            Utils.unzip(file, new File(dataDir));
            file.delete();
        }catch(Exception ue){
            ue.printStackTrace();
        }
    }

}
