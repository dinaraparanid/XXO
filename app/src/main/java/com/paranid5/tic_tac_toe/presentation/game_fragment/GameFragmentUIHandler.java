package com.paranid5.tic_tac_toe.presentation.game_fragment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.domain.game_service.GameService;
import com.paranid5.tic_tac_toe.domain.network.ClientLauncher;
import com.paranid5.tic_tac_toe.domain.utils.network.DefaultDisposableCompletable;
import com.paranid5.tic_tac_toe.presentation.UIHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public final class GameFragmentUIHandler implements UIHandler {
    private static final String TAG = GameFragmentUIHandler.class.getSimpleName();

    @Inject
    public GameFragmentUIHandler() {}

    public void onCellClicked(
            final int cellPosition,
            final @NonNull GameFragmentViewModel viewModel,
            final @Nullable Socket client,
            final @NonNull Context context
    ) {
        final Integer[] cells = viewModel.getCellsState().getValue();

        if (cells[cellPosition] != null)
            return;

        cells[cellPosition] = viewModel.getPlayerRole().ordinal();
        viewModel.postCellsState(cells);

        if (viewModel.getPlayerType().equals(PlayerType.CLIENT))
            sendMoveToServer(Objects.requireNonNull(client), (byte) cellPosition);
        else
            sendMoveToService(context, cellPosition);
    }

    @NonNull
    private DisposableCompletableObserver sendMoveToServer(
            final @NonNull Socket client,
            final byte cellPosition
    ) {
        Log.d(TAG, String.format("Send %s move to server", cellPosition));

        return Completable
                .fromRunnable(() -> {
                    try {
                        ClientLauncher.sendMoveToServer(client, cellPosition);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribeWith(DefaultDisposableCompletable.disposableCompletableObserver());
    }

    private void sendMoveToService(
            final @NonNull Context context,
            final int cellPosition
    ) {
        context.sendBroadcast(
                new Intent(GameService.Broadcast_HOST_MOVED)
                        .putExtra(GameService.CELL_KEY, cellPosition)
        );
    }

    public void sendHostLeft(final @NonNull Context context) {
        Log.d(TAG, "Send host left to service");
        context.sendBroadcast(new Intent(GameService.Broadcast_HOST_LEFT));
    }

    @NonNull
    public Completable sendClientLeft(final @NonNull Socket client) {
        Log.d(TAG, "Send client left to server");

        return Completable
                .fromRunnable(() -> {
                    try {
                        ClientLauncher.sendLeftToServer(client);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io());
    }
}
