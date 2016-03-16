package io.tourox.mobileapp.services;

import android.content.Context;

/**
 * Detect automatically that headphones are plugged or unplugged.
 * Created by Marc Plouhinec on 12/03/16.
 */
public interface HeadphoneService {

    /**
     * Enable or disable the service.
     * Note: this method must be called when the main activity receives the onResume() and onPause() events.
     */
    void setEnabled(boolean enabled, Context context);

    /**
     * Register the given listener.
     */
    void registerHeadphoneListener(HeadphoneListener listener);

    /**
     * Listener called when the headphones are plugged or unplugged.
     */
    interface HeadphoneListener {

        /**
         * Called method with the new state.
         */
        void onPlugStatusChange(boolean plugged);
    }
}
