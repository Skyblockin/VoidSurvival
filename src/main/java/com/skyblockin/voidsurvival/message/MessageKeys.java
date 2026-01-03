package com.skyblockin.voidsurvival.message;

public final class MessageKeys {

    // Friend command messages
    public static final String FRIEND_MUTUALS_FRIEND_LIST_HEADER = "command.friend.mutuals.list.header";
    public static final String FRIEND_MUTUALS_FRIEND_LIST_ENTRY = "command.friend.mutuals.list.entry";
    public static final String FRIEND_MUTUALS_SELF = "command.friend.mutuals.self-feedback";
    public static final String FRIEND_MUTUALS_NO_MUTUALS = "command.friend.mutuals.no-mutuals";
    public static final String FRIEND_LIST_HEADER = "command.friend.list.header";
    public static final String FRIEND_LIST_ENTRY = "command.friend.list.entry";
    public static final String FRIEND_LIST_NO_FRIENDS = "command.friend.list.no-friends-feedback";
    public static final String FRIEND_ADD_REQUEST_ALREADY_SENT = "command.friend.add.request-already-sent";
    public static final String FRIEND_ADD_RECIPIENT = "command.friend.add.recipient-feedback";
    public static final String FRIEND_ADD_SENDER = "command.friend.add.sender-feedback";
    public static final String FRIEND_ADD_SELF = "command.friend.add.self-feedback";
    public static final String FRIEND_ADD_ALREADY_FRIENDS = "command.friend.add.already-friends";
    public static final String FRIEND_ADD_ACCEPT_SENDER = "command.friend.add.accept-sender";
    public static final String FRIEND_ADD_ACCEPT_RECIPIENT = "command.friend.add.accept-recipient";
    public static final String FRIEND_REMOVE_SELF = "command.friend.remove.self-feedback";
    public static final String FRIEND_REMOVE_SENDER = "command.friend.remove.sender-feedback";
    public static final String FRIEND_REMOVE_RECIPIENT = "command.friend.remove.recipient-feedback";
    public static final String FRIEND_REMOVE_NOT_FRIENDS = "command.friend.remove.not-friends";
    public static final String FRIEND_FEEDBACK = "command.friend.feedback";

    // Leaderboard stuff
    public static final String LEADERBOARD_HEADER = "command.leaderboard.list.header";
    public static final String LEADERBOARD_ENTRY = "command.leaderboard.list.entry";

    // Island stuff
    public static final String ISLAND_ALREADY_CREATED = "command.island.already-created";
    public static final String ISLAND_GENERATING = "command.island.generating";
    public static final String ISLAND_ALREADY_GENERATING = "command.island.already-generating";
    public static final String ISLAND_TELEPORT_LONG_WAIT = "command.island.teleport-long-wait";
    public static final String ISLAND_TELEPORT_SMALL_WAIT = "command.island.teleport-small-wait";
    public static final String ISLAND_TELEPORT = "command.island.teleport";

    // Home command messages
    public static final String HOME_SET = "command.home.set";
    public static final String HOME_NOT_SET = "command.home.not-set";
    public static final String HOME_DEFAULT_NOT_SET = "command.home.default-not-set";
    public static final String HOME_NOT_FOUND = "command.home.not-found";
    public static final String HOME_OBSTRUCTED = "command.home.obstructed";
    public static final String HOME_TELEPORTED = "command.home.teleported";

    // Chest stuff
    public static final String CHEST_ON_COOLDOWN = "chest.on-cooldown";

    // Campfire stuff
    public static final String CAMPFIRE_UNLOCKED = "campfire.unlocked";
    public static final String CAMPFIRE_TOO_MANY_CAMPFIRES = "campfire.too-many-campfires";
    public static final String CAMPFIRE_DELETED = "campfire.deleted";
    public static final String CAMPFIRE_NOT_FOUND = "campfire.not-found";
    public static final String CAMPFIRE_NONE_UNLOCKED = "campfire.none-unlocked";
    public static final String CAMPFIRE_NO_COMBAT_WARP = "campfire.combat-warp-blocked";
    public static final String CAMPFIRE_INVALID = "campfire.invalid-campfire";
    public static final String CAMPFIRE_WARPED = "campfire.warped";
    public static final String CAMPFIRE_ALREADY_AT_CAMPFIRE = "campfire.already-at-campfire";

    // Combat stuff
    public static final String COMBAT_COMMAND_BLOCKED_IN_COMBAT = "combat.command-blocked-in-combat";
    public static final String COMBAT_KILLSTREAK_REACHED = "combat.killstreak-reached";
    public static final String COMBAT_KILLSTREAK_ENDED = "combat.killstreak-ended";

    // Miscellaneous stuff
    public static final String PLAYER_NOT_FOUND = "command.player-not-found";
    public static final String CLOSE_ONE = "saved-from-void";
}
