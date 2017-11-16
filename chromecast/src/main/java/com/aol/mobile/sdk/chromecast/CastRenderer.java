package com.aol.mobile.sdk.chromecast;

import android.support.annotation.NonNull;
import android.view.View;

public interface CastRenderer {

    @NonNull
    void render(@NonNull CastVideoVM videoVM);

    @NonNull
    View getViewport();
}
