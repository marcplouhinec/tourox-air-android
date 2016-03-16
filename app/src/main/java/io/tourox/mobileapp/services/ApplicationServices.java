package io.tourox.mobileapp.services;

import io.tourox.mobileapp.services.impl.HeadphoneServiceImpl;
import io.tourox.mobileapp.services.impl.VoipServiceImpl;

/**
 * Provide services to the other classes as singletons.
 * Created by Marc Plouhinec on 06/03/16.
 */
public class ApplicationServices {

    private static final VoipService VOIP_SERVICE = new VoipServiceImpl();
    private static final HeadphoneService HEADPHONE_SERVICE = new HeadphoneServiceImpl();

    /**
     * @return {@link VoipService}
     */
    public static VoipService getVoipService() {
        return VOIP_SERVICE;
    }

    /**
     * @return {@link HeadphoneService}
     */
    public static HeadphoneService getHeadphoneService() {
        return HEADPHONE_SERVICE;
    }
}
