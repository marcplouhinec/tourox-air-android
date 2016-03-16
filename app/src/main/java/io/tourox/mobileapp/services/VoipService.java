package io.tourox.mobileapp.services;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.IOException;

import io.tourox.mobileapp.enums.VoipConnectionState;

/**
 * Handle communication with the VoIP router.
 * Created by Marc Plouhinec on 06/03/16.
 */
public interface VoipService {

    /**
     * Initialize the internal SIP library. This method must be called first!
     */
    void initialize(Context context) throws IOException, PackageManager.NameNotFoundException;

    /**
     * Destroy the SIP library resources.
     */
    void destroy();

    /**
     * Register the given listener.
     */
    void registerVoipConnectionStateChangedListener(VoipConnectionStateChangedListener listener);

    /**
     * Open a connection with the VoIP router.
     */
    void openConnection(String username, String password, String hostname) throws IOException;

    /**
     * Close the connection with the router.
     */
    void closeConnection();

    /**
     * @return Current connection state
     */
    VoipConnectionState getVoipConnectionState();

    /**
     * Listener for changes in the VoIP connection state.
     */
    interface VoipConnectionStateChangedListener {

        /**
         * Called method with the new state.
         */
        void onVoipConnectionStateChanged(VoipConnectionState state);
    }
}
