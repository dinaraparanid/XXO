package com.paranid5.tic_tac_toe.domain.network;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ClientLauncher {
    @NonNull
    public static Completable launch(final @NonNull String host) {
        return Completable.fromCallable(() -> {
            launchClient(host);
            return true;
        }).subscribeOn(Schedulers.io());
    }

    private static void launchClient(final @NonNull String host) throws IOException {
        try (
                final Socket client = new Socket(host, 8080);
                final BufferedInputStream reader = new BufferedInputStream(client.getInputStream());
                final BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream())
        ) {
            while (true) {
                final byte[] data = new byte[2];
                reader.read(data);

                // TODO: Handle server requests
            }
        }
    }
}
