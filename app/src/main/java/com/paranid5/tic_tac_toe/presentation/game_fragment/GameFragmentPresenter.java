package com.paranid5.tic_tac_toe.presentation.game_fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.Bindable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.BR;
import com.paranid5.tic_tac_toe.R;
import com.paranid5.tic_tac_toe.data.PlayerRole;
import com.paranid5.tic_tac_toe.data.PlayerType;
import com.paranid5.tic_tac_toe.data.utils.extensions.LiveDataExt;
import com.paranid5.tic_tac_toe.presentation.ObservablePresenter;

import java.util.Arrays;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.qualifiers.ApplicationContext;

public final class GameFragmentPresenter extends ObservablePresenter {
    @NonNull
    private final Context context;

    @NonNull
    public MutableLiveData<PlayerType> typeState;

    @NonNull
    public MutableLiveData<PlayerRole> roleState;

    @NonNull
    public MutableLiveData<PlayerRole> currentMovingPlayerState;

    @NonNull
    public LiveData<Boolean> isMovingState;

    @NonNull
    public MutableLiveData<PlayerRole[]> cellsState;

    @AssistedInject
    public GameFragmentPresenter(
            final @ApplicationContext @NonNull Context context,
            final @Assisted @NonNull MutableLiveData<PlayerType> typeState,
            final @Assisted("role") @NonNull MutableLiveData<PlayerRole> roleState,
            final @Assisted("cur_mov") @NonNull MutableLiveData<PlayerRole> currentMovingPlayerState,
            final @Assisted @NonNull MutableLiveData<PlayerRole[]> cellsState
    ) {
        this.context = context;
        this.typeState = typeState;
        this.roleState = roleState;
        this.currentMovingPlayerState = currentMovingPlayerState;
        this.cellsState = cellsState;

        isMovingState = LiveDataExt.combine(
                roleState,
                currentMovingPlayerState,
                false,
                (role, movingPlayer) -> role == movingPlayer
        );
    }

    @Bindable
    public boolean getMoving() { return Boolean.TRUE.equals(isMovingState.getValue()); }

    @Bindable
    @NonNull
    public String getTurnMessage() {
        return context.getString(getMoving() ? R.string.your_turn : R.string.opponents_turn);
    }

    @Bindable
    @NonNull
    public Drawable[] getCellsPictures() {
        final PlayerRole[] cells = cellsState.getValue();
        final Drawable[] cellPics = new Drawable[cells.length];

        Log.d("CellImages", Arrays.toString(cells));

        for (int i = 0; i < cells.length; ++i)
            cellPics[i] = getCellPicture(cells[i]);

        return cellPics;
    }

    @NonNull
    private Drawable getCellPicture(final @Nullable PlayerRole cellStatus) {
        if (cellStatus == null)
            return AppCompatResources.getDrawable(context, android.R.color.transparent);

        if (cellStatus.equals(PlayerRole.CROSS))
            return AppCompatResources.getDrawable(context, R.drawable.cross);

        return AppCompatResources.getDrawable(context, R.drawable.zero);
    }

    public void startStatesObserving(final @NonNull LifecycleOwner owner) {
        isMovingState.observe(owner, isMoving -> notifyMoveObservers());
        cellsState.observe(owner, playerRoles -> notifyPropertyChanged(BR.cellsPictures));
    }

    private void notifyMoveObservers() {
        notifyPropertyChanged(BR.moving);
        notifyPropertyChanged(BR.turnMessage);
    }
}
