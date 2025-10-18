package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatComparators;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugOptionsScreen extends Screen {
    private static final Component TITLE = Component.translatable("debug.options.title");
    private static final Component SUBTITLE = Component.translatable("debug.options.warning");
    static final Component ENABLED_TEXT = Component.translatable("debug.entry.always");
    static final Component IN_F3_TEXT = Component.translatable("debug.entry.f3");
    static final Component DISABLED_TEXT = CommonComponents.OPTION_OFF;
    static final Component NOT_ALLOWED_TOOLTIP = Component.translatable("debug.options.notAllowed.tooltip");
    private static final Component SEARCH = Component.translatable("debug.options.search").withStyle(EditBox.SEARCH_HINT_STYLE);
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);
    @Nullable
    private DebugOptionsScreen.OptionList optionList;
    private EditBox searchBox;
    final List<Button> profileButtons = new ArrayList<>();

    public DebugOptionsScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        this.optionList = new DebugOptionsScreen.OptionList();
        int i = this.optionList.getRowWidth();
        LinearLayout linearlayout1 = LinearLayout.horizontal().spacing(8);
        linearlayout1.addChild(new SpacerElement(i / 3, 1));
        linearlayout1.addChild(new StringWidget(TITLE, this.font), linearlayout1.newCellSettings().alignVerticallyMiddle());
        this.searchBox = new EditBox(this.font, 0, 0, i / 3, 20, this.searchBox, SEARCH);
        this.searchBox.setResponder(p_429834_ -> this.optionList.updateSearch(p_429834_));
        this.searchBox.setHint(SEARCH);
        linearlayout1.addChild(this.searchBox);
        linearlayout.addChild(linearlayout1, LayoutSettings::alignHorizontallyCenter);
        linearlayout.addChild(new MultiLineTextWidget(SUBTITLE, this.font).setMaxWidth(i).setCentered(true).setColor(-2142128), LayoutSettings::alignHorizontallyCenter);
        this.layout.addToContents(this.optionList);
        LinearLayout linearlayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.addProfileButton(DebugScreenProfile.DEFAULT, linearlayout2);
        this.addProfileButton(DebugScreenProfile.PERFORMANCE, linearlayout2);
        linearlayout2.addChild(Button.builder(CommonComponents.GUI_DONE, p_426150_ -> this.onClose()).width(60).build());
        this.layout.visitWidgets(p_426584_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_426584_);
        });
        this.repositionElements();
    }

    @Override
    public void renderBlurredBackground(GuiGraphics p_431670_) {
        this.minecraft.gui.renderDebugOverlay(p_431670_);
        super.renderBlurredBackground(p_431670_);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    private void addProfileButton(DebugScreenProfile p_423993_, LinearLayout p_430879_) {
        Button button = Button.builder(Component.translatable(p_423993_.translationKey()), p_426076_ -> {
            this.minecraft.debugEntries.loadProfile(p_423993_);
            this.minecraft.debugEntries.save();
            this.optionList.refreshEntries();

            for (Button button1 : this.profileButtons) {
                button1.active = true;
            }

            p_426076_.active = false;
        }).width(120).build();
        button.active = !this.minecraft.debugEntries.isUsingProfile(p_423993_);
        this.profileButtons.add(button);
        p_430879_.addChild(button);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.optionList != null) {
            this.optionList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void render(GuiGraphics p_428500_, int p_429213_, int p_429605_, float p_423616_) {
        super.render(p_428500_, p_429213_, p_429605_, p_423616_);
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class AbstractOptionEntry extends ContainerObjectSelectionList.Entry<DebugOptionsScreen.AbstractOptionEntry> {
        public abstract void refreshEntry();
    }

    @OnlyIn(Dist.CLIENT)
    class CategoryEntry extends DebugOptionsScreen.AbstractOptionEntry {
        final Component category;

        public CategoryEntry(final Component p_423713_) {
            this.category = p_423713_;
        }

        @Override
        public void renderContent(GuiGraphics p_428557_, int p_423607_, int p_430999_, boolean p_431305_, float p_431269_) {
            p_428557_.drawCenteredString(DebugOptionsScreen.this.minecraft.font, this.category, this.getContentX() + this.getContentWidth() / 2, this.getContentY() + 5, -1);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput p_424152_) {
                    p_424152_.add(NarratedElementType.TITLE, CategoryEntry.this.category);
                }
            });
        }

        @Override
        public void refreshEntry() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    class OptionEntry extends DebugOptionsScreen.AbstractOptionEntry {
        private final ResourceLocation location;
        protected final List<AbstractWidget> children = Lists.newArrayList();
        private final CycleButton<Boolean> always;
        private final CycleButton<Boolean> f3;
        private final CycleButton<Boolean> never;
        private final String name;
        private final boolean isAllowed;

        public OptionEntry(final ResourceLocation p_425265_) {
            this.location = p_425265_;
            DebugScreenEntry debugscreenentry = DebugScreenEntries.getEntry(p_425265_);
            this.isAllowed = debugscreenentry != null && debugscreenentry.isAllowed(DebugOptionsScreen.this.minecraft.showOnlyReducedInfo());
            String s = p_425265_.getPath();
            if (this.isAllowed) {
                this.name = s;
            } else {
                this.name = ChatFormatting.ITALIC + s;
            }

            this.always = CycleButton.booleanBuilder(
                    DebugOptionsScreen.ENABLED_TEXT.copy().withColor(-2142128), DebugOptionsScreen.ENABLED_TEXT.copy().withColor(-4539718)
                )
                .displayOnlyValue()
                .withCustomNarration(this::narrateButton)
                .create(10, 5, 44, 16, Component.literal(s), (p_431405_, p_430623_) -> this.setValue(p_425265_, DebugScreenEntryStatus.ALWAYS_ON));
            this.f3 = CycleButton.booleanBuilder(
                    DebugOptionsScreen.IN_F3_TEXT.copy().withColor(-171), DebugOptionsScreen.IN_F3_TEXT.copy().withColor(-4539718)
                )
                .displayOnlyValue()
                .withCustomNarration(this::narrateButton)
                .create(10, 5, 44, 16, Component.literal(s), (p_423928_, p_430669_) -> this.setValue(p_425265_, DebugScreenEntryStatus.IN_F3));
            this.never = CycleButton.booleanBuilder(
                    DebugOptionsScreen.DISABLED_TEXT.copy().withColor(-1), DebugOptionsScreen.DISABLED_TEXT.copy().withColor(-4539718)
                )
                .displayOnlyValue()
                .withCustomNarration(this::narrateButton)
                .create(10, 5, 44, 16, Component.literal(s), (p_423857_, p_431336_) -> this.setValue(p_425265_, DebugScreenEntryStatus.NEVER));
            this.children.add(this.never);
            this.children.add(this.f3);
            this.children.add(this.always);
            this.refreshEntry();
        }

        private MutableComponent narrateButton(CycleButton<Boolean> p_425638_) {
            DebugScreenEntryStatus debugscreenentrystatus = DebugOptionsScreen.this.minecraft.debugEntries.getStatus(this.location);
            MutableComponent mutablecomponent = Component.translatable("debug.entry.currently." + debugscreenentrystatus.getSerializedName(), this.name);
            return CommonComponents.optionNameValue(mutablecomponent, p_425638_.getMessage());
        }

        private void setValue(ResourceLocation p_428235_, DebugScreenEntryStatus p_426716_) {
            DebugOptionsScreen.this.minecraft.debugEntries.setStatus(p_428235_, p_426716_);

            for (Button button : DebugOptionsScreen.this.profileButtons) {
                button.active = true;
            }

            this.refreshEntry();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        @Override
        public void renderContent(GuiGraphics p_427669_, int p_422698_, int p_426525_, boolean p_427163_, float p_423082_) {
            int i = this.getContentX();
            int j = this.getContentY();
            p_427669_.drawString(DebugOptionsScreen.this.minecraft.font, this.name, i, j + 5, this.isAllowed ? -1 : -8355712);
            int k = i + this.getContentWidth() - this.never.getWidth() - this.f3.getWidth() - this.always.getWidth();
            if (!this.isAllowed && p_427163_ && p_422698_ < k) {
                p_427669_.setTooltipForNextFrame(DebugOptionsScreen.NOT_ALLOWED_TOOLTIP, p_422698_, p_426525_);
            }

            this.never.setX(k);
            this.f3.setX(this.never.getX() + this.never.getWidth());
            this.always.setX(this.f3.getX() + this.f3.getWidth());
            this.always.setY(j);
            this.f3.setY(j);
            this.never.setY(j);
            this.always.render(p_427669_, p_422698_, p_426525_, p_423082_);
            this.f3.render(p_427669_, p_422698_, p_426525_, p_423082_);
            this.never.render(p_427669_, p_422698_, p_426525_, p_423082_);
        }

        @Override
        public void refreshEntry() {
            DebugScreenEntryStatus debugscreenentrystatus = DebugOptionsScreen.this.minecraft.debugEntries.getStatus(this.location);
            this.always.setValue(debugscreenentrystatus == DebugScreenEntryStatus.ALWAYS_ON);
            this.f3.setValue(debugscreenentrystatus == DebugScreenEntryStatus.IN_F3);
            this.never.setValue(debugscreenentrystatus == DebugScreenEntryStatus.NEVER);
            this.always.active = !this.always.getValue();
            this.f3.active = !this.f3.getValue();
            this.never.active = !this.never.getValue();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class OptionList extends ContainerObjectSelectionList<DebugOptionsScreen.AbstractOptionEntry> {
        private static final Comparator<Map.Entry<ResourceLocation, DebugScreenEntry>> COMPARATOR = (p_428792_, p_429338_) -> {
            int i = FloatComparators.NATURAL_COMPARATOR.compare(p_428792_.getValue().category().sortKey(), p_429338_.getValue().category().sortKey());
            return i != 0 ? i : p_428792_.getKey().compareTo(p_429338_.getKey());
        };
        private static final int ITEM_HEIGHT = 20;

        public OptionList() {
            super(
                Minecraft.getInstance(),
                DebugOptionsScreen.this.width,
                DebugOptionsScreen.this.layout.getContentHeight(),
                DebugOptionsScreen.this.layout.getHeaderHeight(),
                20
            );
            this.updateSearch("");
        }

        @Override
        public void renderWidget(GuiGraphics p_428148_, int p_428689_, int p_426233_, float p_431560_) {
            super.renderWidget(p_428148_, p_428689_, p_426233_, p_431560_);
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        public void refreshEntries() {
            this.children().forEach(DebugOptionsScreen.AbstractOptionEntry::refreshEntry);
        }

        public void updateSearch(String p_424249_) {
            this.clearEntries();
            List<Map.Entry<ResourceLocation, DebugScreenEntry>> list = new ArrayList<>(DebugScreenEntries.allEntries().entrySet());
            list.sort(COMPARATOR);
            DebugEntryCategory debugentrycategory = null;

            for (Map.Entry<ResourceLocation, DebugScreenEntry> entry : list) {
                if (entry.getKey().getPath().contains(p_424249_)) {
                    DebugEntryCategory debugentrycategory1 = entry.getValue().category();
                    if (!debugentrycategory1.equals(debugentrycategory)) {
                        this.addEntry(DebugOptionsScreen.this.new CategoryEntry(debugentrycategory1.label()));
                        debugentrycategory = debugentrycategory1;
                    }

                    this.addEntry(DebugOptionsScreen.this.new OptionEntry(entry.getKey()));
                }
            }

            this.notifyListUpdated();
        }

        private void notifyListUpdated() {
            this.refreshScrollAmount();
            DebugOptionsScreen.this.triggerImmediateNarration(true);
        }
    }
}