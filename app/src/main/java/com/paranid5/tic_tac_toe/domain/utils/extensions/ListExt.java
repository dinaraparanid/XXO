package com.paranid5.tic_tac_toe.domain.utils.extensions;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ListExt {
    @NonNull
    public static <T> List<T> mutableListOf(@NonNull T... elems) {
        final List<T> list = new ArrayList<>(elems.length);
        Collections.addAll(list, elems);
        return list;
    }
}
