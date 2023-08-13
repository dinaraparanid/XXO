package com.paranid5.tic_tac_toe.data;

import androidx.annotation.NonNull;

import java.io.Serializable;

public interface GameStatus extends Serializable {
    final class Victor implements GameStatus {
        public PlayerType victorType;

        public Victor(final @NonNull PlayerType victorType) {
            this.victorType = victorType;
        }
    }

    final class Draw implements GameStatus {
        public Draw() {}
    }

    final class Playing implements GameStatus {
        public Playing() {}
    }
}
