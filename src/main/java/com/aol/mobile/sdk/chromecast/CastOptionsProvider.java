package com.aol.mobile.sdk.chromecast;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.Arrays;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    @Override
    public CastOptions getCastOptions(Context context) {
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(Arrays.asList(
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING)
                        , new int[]{0, 1})
                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .build();

        String receiverApplicationId = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            receiverApplicationId = ai.metaData.getString("com.aol.mobile.sdk.chromecast.ReceiverApplicationId");
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (receiverApplicationId == null){
            receiverApplicationId = "4F8B3483"; // Aol default cast id
        }

        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(receiverApplicationId)
                .setCastMediaOptions(mediaOptions)
                .setEnableReconnectionService(true)
                .build();
        return castOptions;
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

}