package com.itvillage.dev.torchlight_volumebutton;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


/**
 * Created by monirozzamanroni on 6/21/2019.
 */

public class backgroudRunningService extends Service {
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    Camera camera;
    Camera.Parameters parameters;
    int count = 0;
    MediaSessionCompat mediaSession;
    AudioManager audioManager;
    int volume = 50;
    Handler handler;
    Runnable r;
    private InterstitialAd mInterstitialAd;

    public backgroudRunningService() {

        super();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        MobileAds.initialize(this,
                getString(R.string.appId));
        // Interstitial Ads One
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_1));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        createNotificationC();

        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        camera = Camera.open();
        parameters = camera.getParameters();
        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProviderCompat myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, 100, volume) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        //volume down key
                        if (direction == -1) {
                            setCurrentVolume(getCurrentVolume() - 1);
                            count++;
                            if (count == 2) {
                                if (mInterstitialAd.isLoaded()) {

                                    mInterstitialAd.show();
                                } else {
                                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                                }
                                /*Torch Mode On*/
                                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                camera.setParameters(parameters);
                                camera.startPreview();
                                count = 0;

                            }

                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    count = 0;
                                }
                            }, 100);
                        }
                        //volume up key
                        if (direction == 1) {

                            setCurrentVolume(getCurrentVolume() + 1);
                            count++;
                            if (count == 2) {

                                if (parameters.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_TORCH)) {

                                    /*Torch Mode Off*/
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                    camera.setParameters(parameters);
                                    camera.startPreview();
                                    count = 0;
                                }

                            }

                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    count = 0;
                                }
                            }, 100);


                        }
                    }

                    //  -1 -- volume down
                    //   1 -- volume up
                    //   0 -- volume button released

                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground();
        return START_STICKY;
    }

    private void startForeground() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.loho2)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Enable Torch Light Mode")
                .setContentIntent(pendingIntent)
                .build());
    }

    @Override
    public void onDestroy() {
        /*
        * kill Background whole process
        * */
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);

        super.onDestroy();
    }

    private void createNotificationC() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIF_CHANNEL_ID,
                    "Channel_Id", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }


}


