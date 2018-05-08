/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.chromecast;

import android.support.annotation.NonNull;
import android.view.View;

import com.aol.mobile.sdk.annotations.PublicApi;

@PublicApi
public interface CastRenderer {

    void render(@NonNull CastVideoVM videoVM);

    @NonNull
    View getViewport();
}
