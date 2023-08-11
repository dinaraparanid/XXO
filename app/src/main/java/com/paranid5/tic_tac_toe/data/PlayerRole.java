package com.paranid5.tic_tac_toe.data;

import androidx.annotation.NonNull;

public enum PlayerRole {
    CROSS, ZERO;

    @NonNull
    public PlayerRole nextRole() {
        if (this.equals(CROSS)) return ZERO;
        return CROSS;
    }
}
