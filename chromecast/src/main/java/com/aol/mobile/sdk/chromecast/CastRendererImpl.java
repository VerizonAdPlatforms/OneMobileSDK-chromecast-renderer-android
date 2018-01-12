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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

public final class CastRendererImpl implements CastRenderer {

    @NonNull
    private final Context context;
    @Nullable
    private CastVideoVM.Callbacks callbacks;
    @NonNull
    private RemoteMediaClient remoteMediaClient;
    @NonNull
    private View view;
    @NonNull
    private View castIcon;
    @Nullable
    private String videoUrl;

    private int playbackState;
    private long duration;
    private long position;
    private boolean isMuted = false;
    private boolean shouldPlay;
    private boolean isPlaybackStarted;
    private boolean isActive;

    public CastRendererImpl(@NonNull Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.cast_view, null);
        castIcon = view.findViewById(R.id.cast_icon);
        remoteMediaClient = CastContext.getSharedInstance(context).getSessionManager()
                .getCurrentCastSession().getRemoteMediaClient();
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                assert remoteMediaClient != null;
                if (!isActive) return;
                int playbackState = remoteMediaClient.getPlayerState();
                if (CastRendererImpl.this.playbackState != playbackState) {
                    CastRendererImpl.this.playbackState = playbackState;

                    switch (playbackState) {
                        case MediaStatus.PLAYER_STATE_IDLE:
                            if (callbacks == null) return;
                            callbacks.onVideoPlaybackFlagUpdated(false);
                            if (remoteMediaClient.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED && isPlaybackStarted) {
                                isPlaybackStarted = false;
                                if (duration != 0) {
                                    callbacks.onVideoPositionUpdated(duration);
                                }
                                callbacks.onVideoEnded();
                            }
                            break;
                        case MediaStatus.PLAYER_STATE_BUFFERING:
                            if (callbacks == null) return;
                            callbacks.onVideoPlaybackFlagUpdated(false);
                            break;
                        case MediaStatus.PLAYER_STATE_PAUSED:
                            if (callbacks == null) return;
                            callbacks.onVideoPlaybackFlagUpdated(false);
                            break;
                        case MediaStatus.PLAYER_STATE_PLAYING:
                            if (callbacks == null) return;
                            callbacks.onVideoPlaybackFlagUpdated(true);
                            break;
                    }
                }
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {
            }
        });
    }

    private static String getUrlType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension.equalsIgnoreCase("m3u8")) {
            return "application/x-mpegURL";
        }
        String type = null;
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void renderCallbacks(final @NonNull CastVideoVM.Callbacks callbacks) {
        if (this.callbacks != callbacks) {
            this.callbacks = callbacks;
            remoteMediaClient.addProgressListener(new RemoteMediaClient.ProgressListener() {
                @Override
                public void onProgressUpdated(long p, long d) {
                    if (!isActive) return;
                    if (duration != d) {
                        duration = d;
                        callbacks.onDurationReceived(duration);
                    }
                    position = p;
                    callbacks.onVideoPositionUpdated(position);
                }
            }, 200);
        }
    }

    @Override
    @NonNull
    public void render(@NonNull CastVideoVM videoVM) {
        castIcon.setVisibility(videoVM.isAd ? View.VISIBLE : View.INVISIBLE);
        renderCallbacks(videoVM.callbacks);
        boolean hasBecomeActive = !isActive && videoVM.isActive;
        if (videoVM.videoUrl != null && (!videoVM.videoUrl.equals(videoUrl) || hasBecomeActive) && videoVM.shouldPlay) {
            playVideo(videoVM);
        } else if (videoVM.videoUrl == null && videoUrl != null) {
            playVideo(null);
        }
        isActive = videoVM.isActive;
        Long seekPos = videoVM.seekPosition;
        if (!isPlaybackStarted && seekPos != null) {
            replay(videoVM);
        }
        if (playbackState == MediaStatus.PLAYER_STATE_UNKNOWN || playbackState == MediaStatus.PLAYER_STATE_IDLE)
            return;
        isPlaybackStarted = true;
        if (videoVM.shouldPlay) {
            resumePlayback();
        } else {
            pausePlayback();
        }
        if (seekPos != null) {
            seekTo(seekPos);
        }

        if (videoVM.isMuted != isMuted) {
            setMute(videoVM.isMuted);
        }
    }

    private void playVideo(CastVideoVM videoVM) {
        if (videoVM != null) {
            this.videoUrl = videoVM.videoUrl;

            MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            movieMetadata.putString(MediaMetadata.KEY_TITLE, videoVM.title);
            MediaInfo mediaInfo = new MediaInfo.Builder(videoVM.videoUrl)
                    .setStreamType(videoVM.isLive ? MediaInfo.STREAM_TYPE_LIVE : MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(getUrlType(videoVM.videoUrl))
                    .setMetadata(movieMetadata)
                    .build();
            MediaLoadOptions.Builder loadOptions = new MediaLoadOptions.Builder();
            loadOptions.setAutoplay(false);
            loadOptions.setPlayPosition(videoVM.currentPosition == null ? 0 : videoVM.currentPosition);
            remoteMediaClient.load(mediaInfo, loadOptions.build());
            playbackState = MediaStatus.PLAYER_STATE_UNKNOWN;
            isPlaybackStarted = false;
            shouldPlay = false;
        } else {
            this.videoUrl = null;
        }
    }

    private void seekTo(long position) {
        if (remoteMediaClient != null && Math.abs(this.position - position) > 500) {
            remoteMediaClient.seek(position);
        }

        if (callbacks != null) {
            callbacks.onSeekPerformed();
        }
    }

    private void replay(CastVideoVM videoVM) {
        playVideo(videoVM);
        if (callbacks != null) {
            callbacks.onSeekPerformed();
        }
    }

    private void setMute(boolean isMuted) {
        this.isMuted = isMuted;
        if (remoteMediaClient != null) {
            remoteMediaClient.setStreamMute(isMuted);
        }

    }

    private void pausePlayback() {
        if (shouldPlay) {
            shouldPlay = false;
            if (remoteMediaClient != null) {
                remoteMediaClient.pause();
            }
        }
    }

    private void resumePlayback() {
        if (!shouldPlay) {
            shouldPlay = true;
            if (remoteMediaClient != null) {
                remoteMediaClient.play();
            }
        }
    }

    @NonNull
    @Override
    public View getViewport() {
        return view;
    }

}
