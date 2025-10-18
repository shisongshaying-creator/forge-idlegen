package net.minecraft.client.gui.components;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiLineLabel {
    MultiLineLabel EMPTY = new MultiLineLabel() {
        @Override
        public int render(GuiGraphics p_283645_, MultiLineLabel.Align p_429765_, int p_94389_, int p_94390_, int p_94391_, boolean p_424601_, int p_94392_) {
            return p_94390_;
        }

        @Override
        public Style getStyle(MultiLineLabel.Align p_426223_, int p_429554_, int p_423794_, int p_424776_, double p_424267_, double p_424042_) {
            return null;
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
            return 0;
        }
    };

    static MultiLineLabel create(Font p_94351_, Component... p_94352_) {
        return create(p_94351_, Integer.MAX_VALUE, Integer.MAX_VALUE, p_94352_);
    }

    static MultiLineLabel create(Font p_94342_, int p_94344_, Component... p_345312_) {
        return create(p_94342_, p_94344_, Integer.MAX_VALUE, p_345312_);
    }

    static MultiLineLabel create(Font p_94346_, Component p_344884_, int p_94348_) {
        return create(p_94346_, p_94348_, Integer.MAX_VALUE, p_344884_);
    }

    static MultiLineLabel create(final Font p_169037_, final int p_342954_, final int p_342610_, final Component... p_345091_) {
        return p_345091_.length == 0
            ? EMPTY
            : new MultiLineLabel() {
                @Nullable
                private List<MultiLineLabel.TextAndWidth> cachedTextAndWidth;
                @Nullable
                private Language splitWithLanguage;

                @Override
                public int render(
                    GuiGraphics p_428611_, MultiLineLabel.Align p_431564_, int p_427631_, int p_430078_, int p_425736_, boolean p_422300_, int p_428300_
                ) {
                    int i = p_430078_;

                    for (MultiLineLabel.TextAndWidth multilinelabel$textandwidth : this.getSplitMessage()) {
                        int j = p_431564_.calculateLeft(p_427631_, multilinelabel$textandwidth.width);
                        p_428611_.drawString(p_169037_, multilinelabel$textandwidth.text, j, i, p_428300_);
                        i += p_425736_;
                    }

                    return i;
                }

                @Nullable
                @Override
                public Style getStyle(MultiLineLabel.Align p_425445_, int p_426631_, int p_424137_, int p_429581_, double p_424300_, double p_425320_) {
                    List<MultiLineLabel.TextAndWidth> list = this.getSplitMessage();
                    int i = Mth.floor((p_425320_ - p_424137_) / p_429581_);
                    if (i >= 0 && i < list.size()) {
                        MultiLineLabel.TextAndWidth multilinelabel$textandwidth = list.get(i);
                        int j = p_425445_.calculateLeft(p_426631_, multilinelabel$textandwidth.width);
                        if (p_424300_ < j) {
                            return null;
                        } else {
                            int k = Mth.floor(p_424300_ - j);
                            return p_169037_.getSplitter().componentStyleAtWidth(multilinelabel$textandwidth.text, k);
                        }
                    } else {
                        return null;
                    }
                }

                private List<MultiLineLabel.TextAndWidth> getSplitMessage() {
                    Language language = Language.getInstance();
                    if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
                        return this.cachedTextAndWidth;
                    } else {
                        this.splitWithLanguage = language;
                        List<FormattedText> list = new ArrayList<>();

                        for (Component component : p_345091_) {
                            list.addAll(p_169037_.splitIgnoringLanguage(component, p_342954_));
                        }

                        this.cachedTextAndWidth = new ArrayList<>();
                        int i = Math.min(list.size(), p_342610_);
                        List<FormattedText> list1 = list.subList(0, i);

                        for (int j = 0; j < list1.size(); j++) {
                            FormattedText formattedtext2 = list1.get(j);
                            FormattedCharSequence formattedcharsequence = Language.getInstance().getVisualOrder(formattedtext2);
                            if (j == list1.size() - 1 && i == p_342610_ && i != list.size()) {
                                FormattedText formattedtext = p_169037_.substrByWidth(
                                    formattedtext2, p_169037_.width(formattedtext2) - p_169037_.width(CommonComponents.ELLIPSIS)
                                );
                                FormattedText formattedtext1 = FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS);
                                this.cachedTextAndWidth
                                    .add(new MultiLineLabel.TextAndWidth(Language.getInstance().getVisualOrder(formattedtext1), p_169037_.width(formattedtext1)));
                            } else {
                                this.cachedTextAndWidth.add(new MultiLineLabel.TextAndWidth(formattedcharsequence, p_169037_.width(formattedcharsequence)));
                            }
                        }

                        return this.cachedTextAndWidth;
                    }
                }

                @Override
                public int getLineCount() {
                    return this.getSplitMessage().size();
                }

                @Override
                public int getWidth() {
                    return Math.min(p_342954_, this.getSplitMessage().stream().mapToInt(MultiLineLabel.TextAndWidth::width).max().orElse(0));
                }
            };
    }

    int render(GuiGraphics p_427771_, MultiLineLabel.Align p_430809_, int p_423256_, int p_430692_, int p_427365_, boolean p_426140_, int p_424081_);

    @Nullable
    Style getStyle(MultiLineLabel.Align p_428076_, int p_430800_, int p_428939_, int p_423660_, double p_424031_, double p_426682_);

    int getLineCount();

    int getWidth();

    @OnlyIn(Dist.CLIENT)
    public static enum Align {
        LEFT {
            @Override
            int calculateLeft(int p_426892_, int p_424796_) {
                return p_426892_;
            }
        },
        CENTER {
            @Override
            int calculateLeft(int p_428378_, int p_426757_) {
                return p_428378_ - p_426757_ / 2;
            }
        },
        RIGHT {
            @Override
            int calculateLeft(int p_422548_, int p_428546_) {
                return p_422548_ - p_428546_;
            }
        };

        abstract int calculateLeft(int p_430735_, int p_426776_);
    }

    @OnlyIn(Dist.CLIENT)
    public record TextAndWidth(FormattedCharSequence text, int width) {
    }
}