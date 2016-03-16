package io.tourox.mobileapp.services.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.tourox.mobileapp.services.HeadphoneService;

/**
 * Default implementation of {@link HeadphoneService}.
 * Thanks to http://blog.urvatechlabs.com/detect-programatically-if-headphone-or-bluetooth-headsets-attached-with-android-phone/
 * Created by Marc Plouhinec on 12/03/16.
 */
public class HeadphoneServiceImpl implements HeadphoneService {

    private final List<HeadphoneListener> headphoneListenerListeners = new CopyOnWriteArrayList<>();
    private final HeadphoneBroadcastReceiver headphoneBroadcastReceiver = new HeadphoneBroadcastReceiver();
    private boolean isEnabled = false;

    @Override
    public synchronized void setEnabled(boolean enabled, Context context) {
        if (isEnabled == enabled)
            return;

        if (enabled) {
            context.registerReceiver(headphoneBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            isEnabled = true;
        } else {
            context.unregisterReceiver(headphoneBroadcastReceiver);
            isEnabled = false;
        }
    }

    @Override
    public void registerHeadphoneListener(HeadphoneListener listener) {
        headphoneListenerListeners.add(listener);
    }

    private class HeadphoneBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ("android.intent.action.HEADSET_PLUG".equals(action)) {
                int state = intent.getIntExtra("state", -1);
                if (state == 0) {
                    for (HeadphoneListener listener : headphoneListenerListeners)
                        listener.onPlugStatusChange(false);
                } else if (state == 1) {
                    for (HeadphoneListener listener : headphoneListenerListeners)
                        listener.onPlugStatusChange(true);
                }
            }
        }
    }
}
