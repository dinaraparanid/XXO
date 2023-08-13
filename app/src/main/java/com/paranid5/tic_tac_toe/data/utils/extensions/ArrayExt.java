package com.paranid5.tic_tac_toe.data.utils.extensions;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;

public final class ArrayExt {
    public static <T> boolean all(final @NonNull T[] arr, final Function<T, Boolean> predicate) {
        for (final T elem : arr)
            if (!predicate.apply(elem))
                return false;

        return true;
    }

    public static <T> boolean any(final @NonNull T[] arr, final Function<T, Boolean> predicate) {
        for (final T elem : arr)
            if (predicate.apply(elem))
                return true;

        return false;
    }
}
