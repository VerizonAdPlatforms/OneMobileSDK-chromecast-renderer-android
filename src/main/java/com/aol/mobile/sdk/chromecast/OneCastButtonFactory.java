package com.aol.mobile.sdk.chromecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.MediaRouteButton;
import android.view.LayoutInflater;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class OneCastButtonFactory {

    public static MediaRouteButton getCastButton(@NonNull Context context) {
        CastContext.getSharedInstance(context);
        MediaRouteButton castButton = (MediaRouteButton) LayoutInflater.from(context).inflate(R.layout.cast_button, null);
        CastButtonFactory.setUpMediaRouteButton(context, castButton);
        return castButton;
    }

    public static void addCastButtonListener(@NonNull Context context, final @NonNull CastButtonListener listener) {
        CastContext castContext = CastContext.getSharedInstance(context);
        castContext.getSessionManager().addSessionManagerListener(new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionStarted(CastSession session, String s) {
                if (listener == null) return;
                listener.enableCast();
            }

            @Override
            public void onSessionStartFailed(CastSession session, int i) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionEnded(CastSession session, int i) {
                if (listener == null) return;
                listener.disableCast();
            }

            @Override
            public void onSessionResuming(CastSession session, String s) {
            }

            @Override
            public void onSessionResumed(CastSession session, boolean b) {
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int i) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int i) {
            }

        }, CastSession.class);
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (session != null && session.isConnected()) {
            if (listener != null) {
                listener.enableCast();
            }
        }
    }

    public interface CastButtonListener {

        void enableCast();

        void disableCast();
    }

}
