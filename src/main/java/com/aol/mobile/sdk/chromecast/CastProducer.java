package com.aol.mobile.sdk.chromecast;

import android.content.Context;
import android.support.annotation.NonNull;

import com.aol.mobile.sdk.renderer.VideoRenderer;

@SuppressWarnings("unused")
public class CastProducer implements VideoRenderer.Producer {
    @NonNull
    @Override
    public VideoRenderer createRenderer(@NonNull Context context) {
        return new CastRenderer(context);
    }
}
