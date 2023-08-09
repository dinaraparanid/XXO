package com.paranid5.tic_tac_toe.data.utils.extensions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public final class LiveDataExt {
    @NonNull
    public static <F, S, R> LiveData<R> combine(
            final @NonNull LiveData<F> first,
            final @NonNull LiveData<S> second,
            final @Nullable R initialValue,
            final @NonNull Combiner<F, S, R> combiner
    ) {
        final MediatorLiveData<R> combined = new MediatorLiveData<>();

        combined.addSource(first, value -> {
            combined.postValue(combiner.combine(value, second.getValue()));
        });

        combined.addSource(second, value -> {
            combined.postValue(combiner.combine(first.getValue(), value));
        });

        return combined;
    }

    public interface Combiner<F, S, R> {
        @Nullable
        R combine(final @Nullable F first, final @Nullable S second);
    }
}