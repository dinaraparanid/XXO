package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.paranid5.tic_tac_toe.presentation.ObservableViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public final class SelectGameRoomTypeViewModel extends ObservableViewModel<SelectGameRoomTypePresenter, SelectGameRoomTypeUIHandler> {
    @NonNull
    SelectGameRoomTypePresenter presenter;

    @NonNull
    SelectGameRoomTypeUIHandler handler;

    @Inject
    public SelectGameRoomTypeViewModel(
            final @NonNull SelectGameRoomTypePresenter presenter,
            final @NonNull SelectGameRoomTypeUIHandler handler
    ) {
        this.presenter = presenter;
        this.handler = handler;
        initCallbackObservers();
    }

    @Override
    @NonNull
    public SelectGameRoomTypePresenter getPresenter() {
        return presenter;
    }

    @Override
    @NonNull
    public SelectGameRoomTypeUIHandler getHandler() {
        return handler;
    }

    @NonNull
    private final MediatorLiveData<Boolean> isCreateNewRoomButtonClickedStateMerger = new MediatorLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isCreateNewRoomButtonClickedMutableState = new MutableLiveData<>(false);

    @NonNull
    private final MediatorLiveData<Boolean> isConnectRoomButtonClickedStateMerger = new MediatorLiveData<>(false);

    @NonNull
    private final MutableLiveData<Boolean> isConnectRoomButtonClickedMutableState = new MutableLiveData<>(false);

    @Override
    protected void initCallbackObservers() {
        isCreateNewRoomButtonClickedStateMerger.addSource(
                isCreateNewRoomButtonClickedMutableState,
                isCreateNewRoomButtonClickedStateMerger::postValue
        );

        isConnectRoomButtonClickedStateMerger.addSource(
                isConnectRoomButtonClickedMutableState,
                isConnectRoomButtonClickedMutableState::postValue
        );
    }

    @NonNull
    public LiveData<Boolean> getCreateNewRoomButtonClickedState() {
        return isCreateNewRoomButtonClickedMutableState;
    }

    public void onCreateNewRoomButtonClicked() {
        isCreateNewRoomButtonClickedMutableState.postValue(true);
    }

    public void onCreateNewRoomButtonClickedFinished() {
        isCreateNewRoomButtonClickedMutableState.postValue(false);
    }

    @NonNull
    public LiveData<Boolean> getConnectRoomButtonClickedState() {
        return isConnectRoomButtonClickedMutableState;
    }

    public void onConnectRoomButtonClicked() {
        isConnectRoomButtonClickedMutableState.postValue(true);
    }

    public void onConnectRoomButtonClickedFinished() {
        isConnectRoomButtonClickedMutableState.postValue(false);
    }
}
