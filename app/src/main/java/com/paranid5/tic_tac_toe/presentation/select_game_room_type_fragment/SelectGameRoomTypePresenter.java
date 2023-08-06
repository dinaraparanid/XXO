package com.paranid5.tic_tac_toe.presentation.select_game_room_type_fragment;

import androidx.annotation.NonNull;

import com.paranid5.tic_tac_toe.domain.utils.extensions.ListExt;
import com.paranid5.tic_tac_toe.presentation.BasePresenter;
import com.paranid5.tic_tac_toe.presentation.game_fragment.PlayerRole;

import java.util.Collections;
import java.util.List;

public final class SelectGameRoomTypePresenter implements BasePresenter {
    @NonNull
    public final PlayerRole[] roles;

    public SelectGameRoomTypePresenter() { this.roles = generateRoles(); }

    public SelectGameRoomTypePresenter(final @NonNull PlayerRole[] roles) {
        this.roles = roles;
    }

    @NonNull
    private static PlayerRole[] generateRoles() {
        final List<PlayerRole> roles = ListExt.mutableListOf(PlayerRole.CROSS, PlayerRole.ZERO);
        Collections.shuffle(roles);
        return roles.toArray(new PlayerRole[0]);
    }
}
