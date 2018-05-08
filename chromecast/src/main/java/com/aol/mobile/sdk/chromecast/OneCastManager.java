/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.chromecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.MediaRouteButton;
import android.view.View;

import com.aol.mobile.sdk.annotations.PublicApi;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

@PublicApi
public class OneCastManager {
    @NonNull
    private Context context;
    @Nullable
    private SessionManagerListener sessionManagerListener;

    public OneCastManager(@NonNull Context context) {
        this.context = context;
    }

    public View constructCastButton() {
        CastContext.getSharedInstance(context);

        final MediaRouteButton castButton = new MediaRouteButton(context);
        castButton.setRemoteIndicatorDrawable(null);
        castButton.setBackground(ContextCompat.getDrawable(context, R.drawable.selector_cast_button));
        CastButtonFactory.setUpMediaRouteButton(context, castButton);

        return castButton;
    }

    public void stopCasting() {
        CastContext.getSharedInstance(context).getSessionManager().endCurrentSession(true);
    }

    public void setCastListener(final CastListener listener) {
        CastContext castContext = CastContext.getSharedInstance(context);
        if (sessionManagerListener != null) {
            castContext.getSessionManager().removeSessionManagerListener(sessionManagerListener, CastSession.class);
        }
        sessionManagerListener = new SessionManagerListener<CastSession>() {
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

        };
        castContext.getSessionManager().addSessionManagerListener(sessionManagerListener, CastSession.class);
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (session != null && session.isConnected()) {
            if (listener != null) {
                listener.enableCast();
            }
        }
    }

    public interface CastListener {
        void enableCast();

        void disableCast();
    }
}
