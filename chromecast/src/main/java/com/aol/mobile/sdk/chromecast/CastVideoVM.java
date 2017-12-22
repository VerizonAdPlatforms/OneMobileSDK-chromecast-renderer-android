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

import android.support.annotation.Nullable;

public class CastVideoVM {
    @Nullable
    public String videoUrl;
    @Nullable
    public String title;
    @Nullable
    public Long seekPosition;
    @Nullable
    public Long currentPosition;
    public boolean isMuted;
    public boolean shouldPlay;
    public boolean isLive;
    public boolean isActive;
    public boolean isCasting;
    @Nullable
    public Callbacks callbacks;

    public interface Callbacks {

        /**
         * Fired when duration of video is set.
         *
         * @param duration time in milliseconds, may be 0 if duration is not defined (live stream)
         */
        void onDurationReceived(long duration);

        /**
         * Fired when new position of playback reached
         *
         * @param position time in milliseconds
         */
        void onVideoPositionUpdated(long position);

        /**
         * Fired when actual playback flag is changed
         *
         * @param isActuallyPlaying <b>true</b> if video is actually playing, <b>false</b> otherwise
         */
        void onVideoPlaybackFlagUpdated(boolean isActuallyPlaying);

        /**
         * Fired when video reached its end
         */
        void onVideoEnded();

        /**
         * Fired when seek if performed.
         */
        void onSeekPerformed();
    }
}
