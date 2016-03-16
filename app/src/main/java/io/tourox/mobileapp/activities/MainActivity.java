package io.tourox.mobileapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.linphone.compatibility.Compatibility;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.tourox.mobileapp.R;
import io.tourox.mobileapp.activities.stepcarousel.StepCarouselAdapter;
import io.tourox.mobileapp.enums.VoipConnectionState;
import io.tourox.mobileapp.services.ApplicationServices;
import io.tourox.mobileapp.services.HeadphoneService;
import io.tourox.mobileapp.services.VoipService;

/**
 * Main activity.
 * Created by Marc Plouhinec on 05/03/16.
 */
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler();

    private ViewPager stepCarouselViewPager;
    private TextView stepDescriptionTextView;
    private ContentObserver settingsContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize widgets
        stepDescriptionTextView = (TextView) findViewById(R.id.stepDescriptionTextView);
        initializeStepCarousel();
        initializeVolumeSeekBar();
    }

    /**
     * Thanks to https://github.com/mrleolink/SimpleInfiniteCarousel/.
     */
    private void initializeStepCarousel() {
        stepCarouselViewPager = (ViewPager) findViewById(R.id.step_carousel);
        StepCarouselAdapter stepCarouselAdapter = new StepCarouselAdapter(this, stepDescriptionTextView, stepCarouselViewPager, this.getSupportFragmentManager());
        stepCarouselViewPager.setAdapter(stepCarouselAdapter);
        stepCarouselViewPager.setOnPageChangeListener(stepCarouselAdapter);
        stepCarouselViewPager.setCurrentItem(0);
        stepCarouselViewPager.setOffscreenPageLimit(3);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        stepCarouselViewPager.setPageMargin(-metrics.widthPixels / 2);

        // Avoid the ViewPager to be manually scrollable
        stepCarouselViewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });
    }

    private void initializeVolumeSeekBar() {
        // Set the audio manager in call mode
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Compatibility.setAudioManagerInCallMode(audioManager);

        // Handle the volume seek bar
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        final SeekBar volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Listen to system volume change
        settingsContentObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            }
        };
        getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, settingsContentObserver);

        ApplicationServices.getHeadphoneService().registerHeadphoneListener(new HeadphoneService.HeadphoneListener() {
            @Override
            public void onPlugStatusChange(boolean plugged) {
                Log.i(TAG, "Headphones " + (plugged ? "plugged" : "unplugged"));
                volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            }
        });
    }

    private void showUnrecoverableErrorDialog(int titleStringResourceId, int messageStringResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(titleStringResourceId)
                .setMessage(messageStringResourceId)
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.ignore_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog ignoreDialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.ignore_error_title)
                                .setMessage(R.string.ignore_error_message)
                                .setCancelable(false)
                                .setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .setNegativeButton(R.string.exit_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MainActivity.this.finish();
                                        System.exit(0);
                                    }
                                })
                                .create();
                        ignoreDialog.show();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the current IP address of the WIFI connection
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        if (ipAddress == 0) {
            showUnrecoverableErrorDialog(R.string.no_wifi_connection_error_title, R.string.no_wifi_connection_error_message);
            return;
        }
        if (!String.valueOf(connectionInfo.getSSID()).replace("\"", "").toLowerCase().startsWith("tourox")) {
            showUnrecoverableErrorDialog(R.string.wrong_wifi_connection_error_title, R.string.wrong_wifi_connection_error_message);
            return;
        }

        final VoipService voipService = ApplicationServices.getVoipService();
        if (voipService.getVoipConnectionState() == VoipConnectionState.NOT_CONNECTED) {
            // Compute the configuration
            String formattedIp = Formatter.formatIpAddress(ipAddress);
            final String username = "u" + formattedIp.substring(formattedIp.lastIndexOf('.') + 1);
            final String hostname = formattedIp.substring(0, formattedIp.lastIndexOf('.')) + ".254";
            Log.i(TAG, "Computed configuration: username = " + username + ", hostname = " + hostname);

            // Open the connection with the VoIP server
            try {
                voipService.initialize(this);
                voipService.registerVoipConnectionStateChangedListener(new VoipService.VoipConnectionStateChangedListener() {
                    @Override
                    public void onVoipConnectionStateChanged(final VoipConnectionState state) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.onVoipConnectionStateChanged(state);
                            }
                        });
                    }
                });
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            voipService.openConnection(username, "pass", hostname);
                        } catch (Exception e) {
                            Log.e(TAG, "Enable to open a connection with the VoIP service: " + e.getMessage(), e);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showUnrecoverableErrorDialog(R.string.unable_to_open_connection_to_voip_service_error_title, R.string.unable_to_open_connection_to_voip_service_error_message);
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Enable to load the VoIp service: " + e.getMessage(), e);
                showUnrecoverableErrorDialog(R.string.unable_to_load_voip_service_error_title, R.string.unable_to_load_voip_service_error_message);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ApplicationServices.getHeadphoneService().setEnabled(true, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ApplicationServices.getHeadphoneService().setEnabled(false, this);
    }

    @Override
    protected void onDestroy() {
        if (settingsContentObserver != null)
            getContentResolver().unregisterContentObserver(settingsContentObserver);

        VoipService voipService = ApplicationServices.getVoipService();
        voipService.closeConnection();
        voipService.destroy();

        super.onDestroy();
    }

    private void onVoipConnectionStateChanged(VoipConnectionState state) {
        switch (state) {
            case NOT_CONNECTED:
                stepCarouselViewPager.setCurrentItem(0);
                break;
            case UNABLE_TO_CONNECT:
                stepCarouselViewPager.setCurrentItem(0);
                stepDescriptionTextView.setText(R.string.step1_error_description);
                break;
            case CONNECTED_WAITING_FOR_CALL:
                stepCarouselViewPager.setCurrentItem(1);
                break;
            case ONGOING_CALL:
                stepCarouselViewPager.setCurrentItem(2);
                break;
        }
    }

}
