package com.example.mp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ConditionVariable mCondition;
    EditText Link;
    ImageButton pause, play, stop;
    MediaPlayer song;
   
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Link = (EditText) findViewById(R.id.link);
        Button download = (Button) findViewById(R.id.download);
        play = (ImageButton) findViewById(R.id.play);
        pause = (ImageButton) findViewById(R.id.pause);
        stop = (ImageButton) findViewById(R.id.stop);


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.message);
        dialog.setTitle(null);
        final Button AUTORISER = (Button) dialog.findViewById(R.id.autoriser);
        final Button REFUSER = (Button) dialog.findViewById(R.id.refuser);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dialog.show();
                AUTORISER.setOnClickListener(new View.OnClickListener() {
                  @Override
                   public void onClick(View v) {
                        Uri uri = Uri.parse(Link.getText().toString());
                        new MyAsychTask().execute(uri);
                        dialog.dismiss();
                    }
                });
               REFUSER.setOnClickListener(null);
           }
       });


   }

    public class MyAsychTask extends AsyncTask<Uri, Integer, Integer> {

        @Override
        protected Integer doInBackground(Uri... uris) {
            int count = uris.length;
            for (int i = 0; i < count; i++) {
                DownloadData(uris[i],i);
                publishProgress(1);
            }
            return count;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Integer s) {

            Toast.makeText(getApplicationContext(), "file download", Toast.LENGTH_SHORT).show();
        }
    }

    private void DownloadData(Uri uri , int i) {

        DownloadManager downloadmanager = (DownloadManager)
                getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("song");
        request.setDescription("Downloading");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                i+".mp3");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        final long downloadId = downloadmanager.enqueue(request);

        mCondition = new ConditionVariable(false);
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == reference) mCondition.open();
            }
        };
        getApplicationContext().registerReceiver(receiver, filter);
        mCondition.block();
    }
}