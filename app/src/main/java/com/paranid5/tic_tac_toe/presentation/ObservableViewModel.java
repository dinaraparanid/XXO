package com.paranid5.tic_tac_toe.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

public abstract class ObservableViewModel<P extends BasePresenter, H extends UIHandler> extends ViewModel {

    @NonNull
    public abstract P getPresenter();

    @NonNull
    public abstract H getHandler();
}
