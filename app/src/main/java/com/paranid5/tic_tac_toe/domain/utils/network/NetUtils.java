package com.paranid5.tic_tac_toe.domain.utils.network;

import androidx.annotation.NonNull;

public final class NetUtils {
    @NonNull
    public static byte[] parseBody(final @NonNull byte[] msg) {
        final byte[] body = new byte[msg.length - 1];
        System.arraycopy(msg, 1, body, 0, body.length);
        return body;
    }
}
