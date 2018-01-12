/*
 * Copyright (c) 2017. Oath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aol.mobile.sdk.chromecast;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.MediaRouteButton;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

public class OneCastManager {

    private SessionManagerListener sessionManagerListener;

    public static View getCastButton(@NonNull Context context) {
        CastContext.getSharedInstance(context);
        final MediaRouteButton castButton = (MediaRouteButton) LayoutInflater.from(context).inflate(R.layout.cast_button, null);
        CastButtonFactory.setUpMediaRouteButton(context, castButton);
        castButton.setBackground(getCastBackground(context));
        return castButton;
    }

    private static Drawable getCastBackground(@NonNull Context context) {
        Resources resources = context.getResources();
        StateListDrawable background = new StateListDrawable();
        Bitmap enabledBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_cast_background);
        Drawable drawable = resources.getDrawable(R.drawable.ic_cast_background);
        Bitmap disabledBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);
        Paint paint = new Paint();
        paint.setAlpha(77);
        canvas.drawBitmap(enabledBitmap, 0, 0, paint);
        BitmapDrawable disabled = new BitmapDrawable(resources, disabledBitmap);
        background.addState(new int[]{-android.R.attr.state_enabled}, disabled);
        background.addState(StateSet.WILD_CARD, drawable);
        return background;
    }

    public void addCastButtonListener(@NonNull Context context, final CastButtonListener listener) {
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

    public interface CastButtonListener {

        void enableCast();

        void disableCast();
    }

    public static final void stopCasting(Context context) {
        CastContext.getSharedInstance(context).getSessionManager().endCurrentSession(true);
    }

}
