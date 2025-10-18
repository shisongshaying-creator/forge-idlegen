package net.minecraft.client.gui.screens.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LanguageSelectScreen extends OptionsSubScreen {
    private static final Component WARNING_LABEL = Component.translatable("options.languageAccuracyWarning").withColor(-4539718);
    private static final int FOOTER_HEIGHT = 53;
    private LanguageSelectScreen.LanguageSelectionList languageSelectionList;
    final LanguageManager languageManager;

    public LanguageSelectScreen(Screen p_344210_, Options p_342264_, LanguageManager p_343432_) {
        super(p_344210_, p_342264_, Component.translatable("options.language.title"));
        this.languageManager = p_343432_;
        this.layout.setFooterHeight(53);
    }

    @Override
    protected void addContents() {
        this.languageSelectionList = this.layout.addToContents(new LanguageSelectScreen.LanguageSelectionList(this.minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.vertical()).spacing(8);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        linearlayout.addChild(new StringWidget(WARNING_LABEL, this.font));
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.horizontal().spacing(8));
        linearlayout1.addChild(
            Button.builder(Component.translatable("options.font"), p_343010_ -> this.minecraft.setScreen(new FontOptionsScreen(this, this.options))).build()
        );
        linearlayout1.addChild(Button.builder(CommonComponents.GUI_DONE, p_343186_ -> this.onDone()).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.languageSelectionList.updateSize(this.width, this.layout);
    }

    void onDone() {
        LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen$languageselectionlist$entry = this.languageSelectionList.getSelected();
        if (languageselectscreen$languageselectionlist$entry != null
            && !languageselectscreen$languageselectionlist$entry.code.equals(this.languageManager.getSelected())) {
            this.languageManager.setSelected(languageselectscreen$languageselectionlist$entry.code);
            this.options.languageCode = languageselectscreen$languageselectionlist$entry.code;
            this.minecraft.reloadResourcePacks();
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected boolean panoramaShouldSpin() {
        return !(this.lastScreen instanceof AccessibilityOnboardingScreen);
    }

    @OnlyIn(Dist.CLIENT)
    class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
        public LanguageSelectionList(final Minecraft p_343433_) {
            super(p_343433_, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height - 33 - 53, 33, 18);
            String s = LanguageSelectScreen.this.languageManager.getSelected();
            LanguageSelectScreen.this.languageManager
                .getLanguages()
                .forEach(
                    (p_420767_, p_420768_) -> {
                        LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen$languageselectionlist$entry = new LanguageSelectScreen.LanguageSelectionList.Entry(
                            p_420767_, p_420768_
                        );
                        this.addEntry(languageselectscreen$languageselectionlist$entry);
                        if (s.equals(p_420767_)) {
                            this.setSelected(languageselectscreen$languageselectionlist$entry);
                        }
                    }
                );
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
            final String code;
            private final Component language;

            public Entry(final String p_344457_, final LanguageInfo p_342261_) {
                this.code = p_344457_;
                this.language = p_342261_.toComponent();
            }

            @Override
            public void renderContent(GuiGraphics p_425929_, int p_424166_, int p_423552_, boolean p_425863_, float p_431522_) {
                p_425929_.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, this.getContentYMiddle() - 9 / 2, -1);
            }

            @Override
            public boolean keyPressed(KeyEvent p_427001_) {
                if (p_427001_.isSelection()) {
                    this.select();
                    LanguageSelectScreen.this.onDone();
                    return true;
                } else {
                    return super.keyPressed(p_427001_);
                }
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent p_424489_, boolean p_425896_) {
                this.select();
                if (p_425896_) {
                    LanguageSelectScreen.this.onDone();
                }

                return super.mouseClicked(p_424489_, p_425896_);
            }

            private void select() {
                LanguageSelectionList.this.setSelected(this);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.language);
            }
        }
    }
}