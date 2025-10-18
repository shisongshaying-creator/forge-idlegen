package net.minecraft.client.gui.components.debug;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StrictJsonParser;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DebugScreenEntryList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<ResourceLocation, DebugScreenEntryStatus> allStatuses;
    private final List<ResourceLocation> currentlyEnabled = new ArrayList<>();
    private boolean isF3Visible = false;
    @Nullable
    private DebugScreenProfile profile;
    private final File debugProfileFile;
    private long currentlyEnabledVersion;

    public DebugScreenEntryList(File p_424146_) {
        this.debugProfileFile = new File(p_424146_, "debug-profile.json");
        this.load();
    }

    public void load() {
        try {
            if (!this.debugProfileFile.isFile()) {
                this.loadDefaultProfile();
                this.rebuildCurrentList();
                return;
            }

            String s = FileUtils.readFileToString(this.debugProfileFile);
            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, StrictJsonParser.parse(s));
            DataResult<DebugScreenEntryList.SerializedOptions> dataresult = DebugScreenEntryList.SerializedOptions.CODEC.parse(dynamic);
            DebugScreenEntryList.SerializedOptions debugscreenentrylist$serializedoptions = dataresult.getOrThrow(
                p_424936_ -> new IOException("Could not parse debug profile JSON: " + p_424936_)
            );
            if (debugscreenentrylist$serializedoptions.profile().isPresent()) {
                this.loadProfile(debugscreenentrylist$serializedoptions.profile().get());
            } else {
                this.allStatuses = new HashMap<>();
                if (debugscreenentrylist$serializedoptions.custom().isPresent()) {
                    this.allStatuses.putAll(debugscreenentrylist$serializedoptions.custom().get());
                }

                this.profile = null;
            }
        } catch (JsonSyntaxException | IOException ioexception) {
            LOGGER.error("Couldn't read debug profile file {}, resetting to default", this.debugProfileFile, ioexception);
            this.loadDefaultProfile();
            this.save();
        }

        this.rebuildCurrentList();
    }

    public void loadProfile(DebugScreenProfile p_424913_) {
        this.profile = p_424913_;
        Map<ResourceLocation, DebugScreenEntryStatus> map = DebugScreenEntries.PROFILES.get(p_424913_);
        this.allStatuses = new HashMap<>(map);
        this.rebuildCurrentList();
    }

    private void loadDefaultProfile() {
        this.profile = DebugScreenProfile.DEFAULT;
        this.allStatuses = new HashMap<>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT));
    }

    public DebugScreenEntryStatus getStatus(ResourceLocation p_429319_) {
        DebugScreenEntryStatus debugscreenentrystatus = this.allStatuses.get(p_429319_);
        return debugscreenentrystatus == null ? DebugScreenEntryStatus.NEVER : debugscreenentrystatus;
    }

    public boolean isCurrentlyEnabled(ResourceLocation p_426533_) {
        return this.currentlyEnabled.contains(p_426533_);
    }

    public void setStatus(ResourceLocation p_426767_, DebugScreenEntryStatus p_423179_) {
        this.profile = null;
        this.allStatuses.put(p_426767_, p_423179_);
        this.rebuildCurrentList();
        this.save();
    }

    public boolean toggleStatus(ResourceLocation p_425001_) {
        switch ((DebugScreenEntryStatus)this.allStatuses.get(p_425001_)) {
            case ALWAYS_ON:
                this.setStatus(p_425001_, DebugScreenEntryStatus.NEVER);
                return false;
            case IN_F3:
                if (this.isF3Visible) {
                    this.setStatus(p_425001_, DebugScreenEntryStatus.NEVER);
                    return false;
                }

                this.setStatus(p_425001_, DebugScreenEntryStatus.ALWAYS_ON);
                return true;
            case NEVER:
                if (this.isF3Visible) {
                    this.setStatus(p_425001_, DebugScreenEntryStatus.IN_F3);
                } else {
                    this.setStatus(p_425001_, DebugScreenEntryStatus.ALWAYS_ON);
                }

                return true;
            case null:
            default:
                this.setStatus(p_425001_, DebugScreenEntryStatus.ALWAYS_ON);
                return true;
        }
    }

    public Collection<ResourceLocation> getCurrentlyEnabled() {
        return this.currentlyEnabled;
    }

    public void toggleF3Visible() {
        this.setF3Visible(!this.isF3Visible);
    }

    public void setF3Visible(boolean p_428063_) {
        if (this.isF3Visible != p_428063_) {
            this.isF3Visible = p_428063_;
            this.rebuildCurrentList();
        }
    }

    public boolean isF3Visible() {
        return this.isF3Visible;
    }

    public void rebuildCurrentList() {
        this.currentlyEnabled.clear();
        boolean flag = Minecraft.getInstance().showOnlyReducedInfo();

        for (Entry<ResourceLocation, DebugScreenEntryStatus> entry : this.allStatuses.entrySet()) {
            if (entry.getValue() == DebugScreenEntryStatus.ALWAYS_ON || this.isF3Visible && entry.getValue() == DebugScreenEntryStatus.IN_F3) {
                DebugScreenEntry debugscreenentry = DebugScreenEntries.getEntry(entry.getKey());
                if (debugscreenentry != null && debugscreenentry.isAllowed(flag)) {
                    this.currentlyEnabled.add(entry.getKey());
                }
            }
        }

        this.currentlyEnabled.sort(ResourceLocation::compareTo);
        this.currentlyEnabledVersion++;
    }

    public long getCurrentlyEnabledVersion() {
        return this.currentlyEnabledVersion;
    }

    public boolean isUsingProfile(DebugScreenProfile p_424844_) {
        return this.profile == p_424844_;
    }

    public void save() {
        DebugScreenEntryList.SerializedOptions debugscreenentrylist$serializedoptions = new DebugScreenEntryList.SerializedOptions(
            Optional.ofNullable(this.profile), this.profile == null ? Optional.of(this.allStatuses) : Optional.empty()
        );

        try {
            FileUtils.writeStringToFile(
                this.debugProfileFile,
                DebugScreenEntryList.SerializedOptions.CODEC.encodeStart(JsonOps.INSTANCE, debugscreenentrylist$serializedoptions).getOrThrow().toString()
            );
        } catch (IOException ioexception) {
            LOGGER.error("Failed to save debug profile file {}", this.debugProfileFile, ioexception);
        }
    }

    @OnlyIn(Dist.CLIENT)
    record SerializedOptions(Optional<DebugScreenProfile> profile, Optional<Map<ResourceLocation, DebugScreenEntryStatus>> custom) {
        private static final Codec<Map<ResourceLocation, DebugScreenEntryStatus>> CUSTOM_ENTRIES_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC, DebugScreenEntryStatus.CODEC
        );
        public static final Codec<DebugScreenEntryList.SerializedOptions> CODEC = RecordCodecBuilder.create(
            p_426348_ -> p_426348_.group(
                    DebugScreenProfile.CODEC.optionalFieldOf("profile").forGetter(DebugScreenEntryList.SerializedOptions::profile),
                    CUSTOM_ENTRIES_CODEC.optionalFieldOf("custom").forGetter(DebugScreenEntryList.SerializedOptions::custom)
                )
                .apply(p_426348_, DebugScreenEntryList.SerializedOptions::new)
        );
    }
}