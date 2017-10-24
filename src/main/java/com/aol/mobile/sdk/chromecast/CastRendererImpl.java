package com.aol.mobile.sdk.chromecast;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.aol.mobile.sdk.renderer.CastRenderer;
import com.aol.mobile.sdk.renderer.viewmodel.VideoVM;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

public final class CastRendererImpl implements CastRenderer {
    @Nullable
    protected VideoVM.Callbacks callbacks;
    @NonNull
    private RemoteMediaClient remoteMediaClient;
    @NonNull
    private ImageView view;
    @Nullable
    private String videoUrl;

    private int playbackState;
    private long duration;
    private long position;
    private boolean isMuted = false;
    private boolean shouldPlay;
    private boolean isPlaybackStarted;

    public CastRendererImpl(Context context) {
        view = new ImageView(context);
        view.setImageResource(R.drawable.quantum_ic_cast_white_36);
        view.setScaleType(ImageView.ScaleType.CENTER);
        view.setBackgroundColor(0xFF000000);
        remoteMediaClient = CastContext.getSharedInstance(context).getSessionManager()
                .getCurrentCastSession().getRemoteMediaClient();
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                assert remoteMediaClient != null;
                int playbackState = remoteMediaClient.getPlayerState();
                if (CastRendererImpl.this.playbackState != playbackState) {
                    CastRendererImpl.this.playbackState = playbackState;

                    switch (playbackState) {
                        case MediaStatus.PLAYER_STATE_IDLE:
                            if (callbacks == null) return;
                            callbacks.onVideoPlaybackFlagUpdated(false);
                            if (remoteMediaClient.getIdleReason() == MediaStatus.IDLE_REASON_FINISHED && isPlaybackStarted) {
                                isPlaybackStarted = false;
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

    private void renderCallbacks(final @NonNull VideoVM.Callbacks callbacks) {
        if (this.callbacks != callbacks) {
            this.callbacks = callbacks;
            remoteMediaClient.addProgressListener(new RemoteMediaClient.ProgressListener() {
                @Override
                public void onProgressUpdated(long p, long d) {
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
    public VideoVM render(@NonNull VideoVM videoVM) {
        renderCallbacks(videoVM.callbacks);
        if (videoVM.videoUrl != null && !videoVM.videoUrl.equals(videoUrl)) {
            playVideo(videoVM);
        } else if (videoVM.videoUrl == null && videoUrl != null) {
            playVideo(null);
        }
        Long seekPos = videoVM.seekPosition;
        if (!isPlaybackStarted && seekPos != null) {
            replay(videoVM);
        }
        if (playbackState == MediaStatus.PLAYER_STATE_UNKNOWN || playbackState == MediaStatus.PLAYER_STATE_IDLE)
            return updateVideoVM(videoVM);
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
        return updateVideoVM(videoVM);
    }

    private VideoVM updateVideoVM(@NonNull VideoVM videoVM) {
        videoVM.shouldPlay = false;
        return videoVM;
    }

    private void playVideo(VideoVM videoVM) {
        if (videoVM != null) {
            this.videoUrl = videoVM.videoUrl;

            MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            movieMetadata.putString(MediaMetadata.KEY_TITLE, videoVM.title);
            MediaInfo mediaInfo = new MediaInfo.Builder(videoVM.videoUrl)
                    .setStreamType(videoVM.isLive ? MediaInfo.STREAM_TYPE_LIVE : MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(getUrlType(videoVM.videoUrl))
                    .setMetadata(movieMetadata)
                    .build();
            remoteMediaClient.load(mediaInfo, false, videoVM.currentPosition == null ? 0 : videoVM.currentPosition);
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

    private void replay(VideoVM videoVM) {
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

    private static String getUrlType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String type = "videos/mp4";
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
