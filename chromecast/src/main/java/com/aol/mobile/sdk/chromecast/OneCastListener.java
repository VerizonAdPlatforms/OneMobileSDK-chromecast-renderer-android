package com.aol.mobile.sdk.chromecast;

import com.aol.mobile.sdk.player.Player;

public class OneCastListener implements OneCastButtonFactory.CastButtonListener {

    private Player player;

    public OneCastListener(Player player) {
        this.player = player;
    }

    @Override
    public void enableCast() {
        player.enableCast();
    }

    @Override
    public void disableCast() {
        player.disableCast();
    }

}
