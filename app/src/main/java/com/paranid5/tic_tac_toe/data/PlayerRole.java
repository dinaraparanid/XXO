package com.paranid5.tic_tac_toe.data;

public enum PlayerRole {
    CROSS, ZERO;

    public PlayerRole nextRole() {
        if (this.equals(CROSS)) return ZERO;
        return CROSS;
    }
}
