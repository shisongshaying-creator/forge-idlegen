package net.minecraft.client.gui.screens.packs;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.Entry> {
    static final ResourceLocation SELECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/select_highlighted");
    static final ResourceLocation SELECT_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/select");
    static final ResourceLocation UNSELECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/unselect_highlighted");
    static final ResourceLocation UNSELECT_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/unselect");
    static final ResourceLocation MOVE_UP_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_up_highlighted");
    static final ResourceLocation MOVE_UP_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_up");
    static final ResourceLocation MOVE_DOWN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_down_highlighted");
    static final ResourceLocation MOVE_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_down");
    static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
    static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
    private static final int ENTRY_PADDING = 2;
    private final Component title;
    final PackSelectionScreen screen;

    public TransferableSelectionList(Minecraft p_265029_, PackSelectionScreen p_265777_, int p_265774_, int p_265153_, Component p_265124_) {
        super(p_265029_, p_265774_, p_265153_, 33, 36);
        this.screen = p_265777_;
        this.title = p_265124_;
        this.centerListVertically = false;
    }

    @Override
    public int getRowWidth() {
        return this.width - 4;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    @Override
    public boolean keyPressed(KeyEvent p_425615_) {
        return this.getSelected() != null ? this.getSelected().keyPressed(p_425615_) : super.keyPressed(p_425615_);
    }

    public void updateList(Stream<PackSelectionModel.Entry> p_429245_, @Nullable PackSelectionModel.EntryBase p_430177_) {
        this.clearEntries();
        Component component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.addEntry(new TransferableSelectionList.HeaderEntry(this.minecraft.font, component), (int)(9.0F * 1.5F));
        this.setSelected(null);
        p_429245_.filter(PackSelectionModel.Entry::notHidden).forEach(p_430444_ -> {
            TransferableSelectionList.PackEntry transferableselectionlist$packentry = new TransferableSelectionList.PackEntry(this.minecraft, this, p_430444_);
            this.addEntry(transferableselectionlist$packentry);
            if (p_430177_ != null && p_430177_.getId().equals(p_430444_.getId())) {
                this.screen.setFocused(this);
                this.setFocused(transferableselectionlist$packentry);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public abstract class Entry extends ObjectSelectionList.Entry<TransferableSelectionList.Entry> {
        @Override
        public int getWidth() {
            return super.getWidth() - (TransferableSelectionList.this.scrollbarVisible() ? 6 : 0);
        }

        public abstract String getPackId();
    }

    @OnlyIn(Dist.CLIENT)
    public class HeaderEntry extends TransferableSelectionList.Entry {
        private final Font font;
        private final Component text;

        public HeaderEntry(final Font p_427652_, final Component p_427819_) {
            this.font = p_427652_;
            this.text = p_427819_;
        }

        @Override
        public void renderContent(GuiGraphics p_431062_, int p_422850_, int p_424561_, boolean p_429431_, float p_424656_) {
            p_431062_.drawCenteredString(this.font, this.text, this.getX() + this.getWidth() / 2, this.getContentYMiddle() - 9 / 2, -1);
        }

        @Override
        public Component getNarration() {
            return this.text;
        }

        @Override
        public String getPackId() {
            return "";
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class PackEntry extends TransferableSelectionList.Entry {
        private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
        private static final int MAX_NAME_WIDTH_PIXELS = 157;
        private static final String TOO_LONG_NAME_SUFFIX = "...";
        private final TransferableSelectionList parent;
        protected final Minecraft minecraft;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;

        public PackEntry(final Minecraft p_265717_, final TransferableSelectionList p_426565_, final PackSelectionModel.Entry p_265360_) {
            this.minecraft = p_265717_;
            this.pack = p_265360_;
            this.parent = p_426565_;
            this.nameDisplayCache = cacheName(p_265717_, p_265360_.getTitle());
            this.descriptionDisplayCache = cacheDescription(p_265717_, p_265360_.getExtendedDescription());
            this.incompatibleNameDisplayCache = cacheName(p_265717_, TransferableSelectionList.INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = cacheDescription(p_265717_, p_265360_.getCompatibility().getDescription());
        }

        private static FormattedCharSequence cacheName(Minecraft p_100105_, Component p_100106_) {
            int i = p_100105_.font.width(p_100106_);
            if (i > 157) {
                FormattedText formattedtext = FormattedText.composite(
                    p_100105_.font.substrByWidth(p_100106_, 157 - p_100105_.font.width("...")), FormattedText.of("...")
                );
                return Language.getInstance().getVisualOrder(formattedtext);
            } else {
                return p_100106_.getVisualOrderText();
            }
        }

        private static MultiLineLabel cacheDescription(Minecraft p_100110_, Component p_100111_) {
            return MultiLineLabel.create(p_100110_.font, 157, 2, p_100111_);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.pack.getTitle());
        }

        @Override
        public void renderContent(GuiGraphics p_425257_, int p_428146_, int p_422806_, boolean p_426963_, float p_426560_) {
            PackCompatibility packcompatibility = this.pack.getCompatibility();
            if (!packcompatibility.isCompatible()) {
                int i = this.getContentX() - 1;
                int j = this.getContentY() - 1;
                int k = this.getContentRight() + 1;
                int l = this.getContentBottom() + 1;
                p_425257_.fill(i, j, k, l, -8978432);
            }

            p_425257_.blit(RenderPipelines.GUI_TEXTURED, this.pack.getIconTexture(), this.getContentX(), this.getContentY(), 0.0F, 0.0F, 32, 32, 32, 32);
            FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
            MultiLineLabel multilinelabel = this.descriptionDisplayCache;
            if (this.showHoverOverlay()
                && (this.minecraft.options.touchscreen().get() || p_426963_ || this.parent.getSelected() == this && this.parent.isFocused())) {
                p_425257_.fill(this.getContentX(), this.getContentY(), this.getContentX() + 32, this.getContentY() + 32, -1601138544);
                int i1 = p_428146_ - this.getContentX();
                int j1 = p_422806_ - this.getContentY();
                if (!this.pack.getCompatibility().isCompatible()) {
                    formattedcharsequence = this.incompatibleNameDisplayCache;
                    multilinelabel = this.incompatibleDescriptionDisplayCache;
                }

                if (this.pack.canSelect()) {
                    if (i1 < 32) {
                        p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.SELECT_HIGHLIGHTED_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                    } else {
                        p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.SELECT_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (i1 < 16) {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.UNSELECT_HIGHLIGHTED_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.UNSELECT_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }

                    if (this.pack.canMoveUp()) {
                        if (i1 < 32 && i1 > 16 && j1 < 16) {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.MOVE_UP_HIGHLIGHTED_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.MOVE_UP_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }

                    if (this.pack.canMoveDown()) {
                        if (i1 < 32 && i1 > 16 && j1 > 16) {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.MOVE_DOWN_HIGHLIGHTED_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        } else {
                            p_425257_.blitSprite(RenderPipelines.GUI_TEXTURED, TransferableSelectionList.MOVE_DOWN_SPRITE, this.getContentX(), this.getContentY(), 32, 32);
                        }
                    }
                }
            }

            p_425257_.drawString(this.minecraft.font, formattedcharsequence, this.getContentX() + 32 + 2, this.getContentY() + 1, -1);
            multilinelabel.render(p_425257_, MultiLineLabel.Align.LEFT, this.getContentX() + 32 + 2, this.getContentY() + 12, 10, true, -8355712);
        }

        @Override
        public String getPackId() {
            return this.pack.getId();
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        @Override
        public boolean keyPressed(KeyEvent p_429543_) {
            if (p_429543_.isConfirmation()) {
                this.keyboardSelection();
                return true;
            } else {
                if (p_429543_.hasShiftDown()) {
                    if (p_429543_.isUp()) {
                        this.keyboardMoveUp();
                        return true;
                    }

                    if (p_429543_.isDown()) {
                        this.keyboardMoveDown();
                        return true;
                    }
                }

                return super.keyPressed(p_429543_);
            }
        }

        public void keyboardSelection() {
            if (this.pack.canSelect()) {
                this.handlePackSelection();
            } else if (this.pack.canUnselect()) {
                this.pack.unselect();
            }
        }

        private void keyboardMoveUp() {
            if (this.pack.canMoveUp()) {
                this.pack.moveUp();
            }
        }

        private void keyboardMoveDown() {
            if (this.pack.canMoveDown()) {
                this.pack.moveDown();
            }
        }

        private void handlePackSelection() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.select();
            } else {
                Component component = this.pack.getCompatibility().getConfirmation();
                this.minecraft.setScreen(new ConfirmScreen(p_264693_ -> {
                    this.minecraft.setScreen(this.parent.screen);
                    if (p_264693_) {
                        this.pack.select();
                    }
                }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, component));
            }
        }

        @Override
        public boolean shouldTakeFocusAfterInteraction() {
            return TransferableSelectionList.this.children().stream().anyMatch(p_420774_ -> p_420774_.getPackId().equals(this.getPackId()));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent p_423761_, boolean p_429246_) {
            double d0 = p_423761_.x() - this.getX();
            double d1 = p_423761_.y() - this.getY();
            if (this.showHoverOverlay() && d0 <= 32.0) {
                this.parent.screen.clearSelected();
                if (this.pack.canSelect()) {
                    this.handlePackSelection();
                    return true;
                }

                if (d0 < 16.0 && this.pack.canUnselect()) {
                    this.pack.unselect();
                    return true;
                }

                if (d0 > 16.0 && d1 < 16.0 && this.pack.canMoveUp()) {
                    this.pack.moveUp();
                    return true;
                }

                if (d0 > 16.0 && d1 > 16.0 && this.pack.canMoveDown()) {
                    this.pack.moveDown();
                    return true;
                }
            }

            return super.mouseClicked(p_423761_, p_429246_);
        }
    }
}
