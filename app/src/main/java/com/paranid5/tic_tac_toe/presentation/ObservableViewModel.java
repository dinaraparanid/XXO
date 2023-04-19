package com.paranid5.tic_tac_toe.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

public abstract class ObservableViewModel<P extends BasePresenter, H extends UIHandler> extends ViewModel {
    @NonNull
    public final P presenter;

    @NonNull
    public final H handler;

    protected ObservableViewModel(final @NonNull P presenter, final @NonNull H handler) {
        this.presenter = presenter;
        this.handler = handler;
    }
}
