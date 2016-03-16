package io.tourox.mobileapp.services.impl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import io.tourox.mobileapp.R;
import io.tourox.mobileapp.enums.VoipConnectionState;
import io.tourox.mobileapp.services.VoipService;

/**
 * Default implementation of {@link VoipService}.
 * Note: some of the code come from the Linphone library "LinphoneMiniAndroid" sample.
 * Created by Marc Plouhinec on 06/03/16.
 */
public class VoipServiceImpl implements VoipService {

    private static final String TAG = "VoipServiceImpl";

    private final List<VoipConnectionStateChangedListener> voipConnectionStateChangedListeners = new CopyOnWriteArrayList<>();
    private Timer timer = null;

    private LinphoneCore linphoneCore = null;
    private VoipConnectionState currentVoipConnectionState = VoipConnectionState.NOT_CONNECTED;

    @Override
    public void initialize(Context context) throws IOException, PackageManager.NameNotFoundException {
        String basePath = context.getFilesDir().getAbsolutePath();
        copyResourceIfNotExist(context, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        copyResourceIfNotExist(context, R.raw.ringback, basePath + "/ringback.wav");
        copyResourceIfNotExist(context, R.raw.toy_mono, basePath + "/toy_mono.wav");
        copyResourceIfNotExist(context, R.raw.linphonerc_default, basePath + "/.linphonerc");
        copyResourceFromPackage(context, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        copyResourceIfNotExist(context, R.raw.lpconfig, basePath + "/lpconfig.xsd");

        try {
            linphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(new InternalLinphoneCoreListener(), basePath + "/.linphonerc", basePath + "/linphonerc", null, context);
            linphoneCore.setContext(context);
            linphoneCore.setRing(null);
            linphoneCore.setPlayFile(basePath + "/toy_mono.wav");
            linphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");
            linphoneCore.setCpuCount(Runtime.getRuntime().availableProcessors());
            String versionName = String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            linphoneCore.setUserAgent("Tourox", versionName);
        } catch (Throwable e) {
            Log.e(TAG, "Unable to load the linphoneCore: " + e.getMessage(), e);
            throw new IOException("Unable to load the linphoneCore: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (linphoneCore != null)
            linphoneCore.destroy();
    }

    @Override
    public void registerVoipConnectionStateChangedListener(VoipConnectionStateChangedListener listener) {
        voipConnectionStateChangedListeners.add(listener);
    }

    @Override
    public void openConnection(String username, String password, String hostname) throws IOException {
        try {
            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(username, password, null, hostname);
            linphoneCore.addAuthInfo(authInfo);

            LinphoneProxyConfig proxyConfig = linphoneCore.createProxyConfig();
            proxyConfig.setIdentity("sip:" + username + "@" + hostname);
            proxyConfig.setProxy(hostname);
            proxyConfig.enableRegister(true);
            linphoneCore.addProxyConfig(proxyConfig);
        } catch (LinphoneCoreException e) {
            Log.e(TAG, "Unable to authenticate and setup the proxy configuration (username = " + username + ", password = " + password + ", hostname = " + hostname + "): " + e.getMessage(), e);
            throw new IOException("Unable to authenticate and setup the proxy configuration:" + e.getMessage());
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                linphoneCore.iterate();
            }
        };
        timer = new Timer("Tourox Linphone scheduler");
        timer.schedule(timerTask, 0, 20);

        linphoneCore.setNetworkReachable(true);
    }

    @Override
    public void closeConnection() {
        if (linphoneCore != null) {
            LinphoneCall currentCall = linphoneCore.getCurrentCall();
            if (currentCall != null)
                linphoneCore.terminateCall(currentCall);
        }

        if (timer != null)
            timer.cancel();

        if (linphoneCore != null)
            linphoneCore.setNetworkReachable(false);

        notifyVoipConnectionStateChange(VoipConnectionState.NOT_CONNECTED);
    }

    @Override
    public VoipConnectionState getVoipConnectionState() {
        return currentVoipConnectionState;
    }

    private void notifyVoipConnectionStateChange(VoipConnectionState state) {
        if (currentVoipConnectionState == state)
            return;

        Log.i(TAG, "notifyVoipConnectionStateChange : " + state);
        currentVoipConnectionState = state;

        for (VoipConnectionStateChangedListener listener : voipConnectionStateChangedListeners)
            listener.onVoipConnectionStateChanged(currentVoipConnectionState);
    }

    private void copyResourceIfNotExist(Context context, int resourceId, String target) throws IOException {
        File file = new File(target);
        if (!file.exists()) {
            copyResourceFromPackage(context, resourceId, file.getName());
        }
    }

    private void copyResourceFromPackage(Context context, int resourceId, String target) throws IOException {
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = context.openFileOutput(target, 0);
            inputStream = context.getResources().openRawResource(resourceId);
            int readByte;
            byte[] buff = new byte[8048];
            while ((readByte = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, readByte);
            }
            outputStream.flush();
        } finally {
            if (outputStream != null)
                outputStream.close();
            if (inputStream != null)
                inputStream.close();
        }
    }

    private class InternalLinphoneCoreListener extends LinphoneCoreListenerBase {

        @Override
        public void registrationState(LinphoneCore linphoneCore, LinphoneProxyConfig linphoneProxyConfig, LinphoneCore.RegistrationState state, String message) {
            Log.i(TAG, "Linphone registrationState: " + state + ", " + message);
            if (state == LinphoneCore.RegistrationState.RegistrationOk || state == LinphoneCore.RegistrationState.RegistrationProgress) {
                notifyVoipConnectionStateChange(VoipConnectionState.CONNECTED_WAITING_FOR_CALL);
            }
        }

        @Override
        public void callState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String message) {
            Log.i(TAG, "Linphone callState: " + state + ", " + message);
            if (state == LinphoneCall.State.IncomingReceived) {
                try {
                    linphoneCore.acceptCall(linphoneCall);

                    notifyVoipConnectionStateChange(VoipConnectionState.ONGOING_CALL);
                } catch (LinphoneCoreException e) {
                    Log.e(TAG, "Unable to accept a call: " + e.getMessage(), e);
                }
            } else if (state == LinphoneCall.State.Error || state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.CallReleased) {
                notifyVoipConnectionStateChange(VoipConnectionState.CONNECTED_WAITING_FOR_CALL);
            }
        }

        @Override
        public void displayStatus(LinphoneCore linphoneCore, String message) {
            Log.i(TAG, "Linphone status: " + message);
        }

        @Override
        public void displayMessage(LinphoneCore linphoneCore, String message) {
            Log.i(TAG, "Linphone message: " + message);
        }

        @Override
        public void displayWarning(LinphoneCore linphoneCore, String message) {
            Log.w(TAG, "Linphone warning: " + message);
        }
    }
}
