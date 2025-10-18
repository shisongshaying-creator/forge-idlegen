package net.minecraft.server.players;

import com.google.gson.JsonObject;

public class ServerOpListEntry extends StoredUserEntry<NameAndId> {
    private final int level;
    private final boolean bypassesPlayerLimit;

    public ServerOpListEntry(NameAndId p_422946_, int p_11361_, boolean p_11362_) {
        super(p_422946_);
        this.level = p_11361_;
        this.bypassesPlayerLimit = p_11362_;
    }

    public ServerOpListEntry(JsonObject p_11358_) {
        super(NameAndId.fromJson(p_11358_));
        this.level = p_11358_.has("level") ? p_11358_.get("level").getAsInt() : 0;
        this.bypassesPlayerLimit = p_11358_.has("bypassesPlayerLimit") && p_11358_.get("bypassesPlayerLimit").getAsBoolean();
    }

    public int getLevel() {
        return this.level;
    }

    public boolean getBypassesPlayerLimit() {
        return this.bypassesPlayerLimit;
    }

    @Override
    protected void serialize(JsonObject p_11365_) {
        if (this.getUser() != null) {
            this.getUser().appendTo(p_11365_);
            p_11365_.addProperty("level", this.level);
            p_11365_.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
        }
    }
}