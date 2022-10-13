package net.jmb19905.rightchat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jmb19905.rightchat.RightChat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Final
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;

    @Shadow
    private int scrolledLines;

    @Shadow
    private boolean hasUnreadNewMessages;

    @Shadow
    protected abstract boolean isChatHidden();
    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    public abstract int getVisibleLineCount();

    @Shadow
    protected abstract int getIndicatorX(ChatHudLine.Visible visible);

    @Shadow
    protected abstract int getLineHeight();

    @Shadow
    public abstract double getChatScale();

    @Shadow
    protected abstract void drawIndicatorIcon(MatrixStack matrices, int x, int y, MessageIndicator.Icon icon);

    @Shadow
    public abstract int getWidth();

    @SuppressWarnings("SuspiciousNameCombination")
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack matrices, int currentTick, CallbackInfo info) {
        int u;
        int t;
        int s;
        int r;
        int q;
        int o;
        if (this.isChatHidden()) {
            return;
        }
        int i = this.getVisibleLineCount();
        int j = this.visibleMessages.size();
        if (j == 0) {
            return;
        }
        boolean bl = this.isChatFocused();
        float f = (float)this.getChatScale();
        int k = MathHelper.ceil((float)this.getWidth() / f);
        matrices.push();
        matrices.translate(4.0, 8.0, 0.0);
        matrices.scale(f, f, 1.0f);
        MinecraftClient client = MinecraftClient.getInstance();
        assert client != null;
        double d = client.options.getChatOpacity().getValue() * (double)0.9f + (double)0.1f;
        double e = client.options.getTextBackgroundOpacity().getValue();
        double g = client.options.getChatLineSpacing().getValue();
        int l = this.getLineHeight();
        double h = -8.0 * (g + 1.0) + 4.0 * g;
        int m = 0;
        for (int n = 0; n + this.scrolledLines < this.visibleMessages.size() && n < i; ++n) {
            ChatHudLine.Visible visible = this.visibleMessages.get(n + this.scrolledLines);
            if (visible == null || (o = currentTick - visible.addedTime()) >= 200 && !bl) continue;
            double p = bl ? 1.0 : getMessageOpacityMultiplier(o);
            q = (int)(255.0 * p * d);
            r = (int)(255.0 * p * e);
            ++m;
            if (q <= 3) continue;
            s = 0;
            t = -n * l;
            u = (int)((double)t + h);
            matrices.push();
            matrices.translate(0.0, 0.0, 50.0);
            ChatHud.fill(matrices, getRight(k) + 8, t - l, getLeft(k) - 4, t, r << 24);
            MessageIndicator messageIndicator = visible.indicator();
            if (messageIndicator != null) {
                int v = messageIndicator.indicatorColor() | q << 24;
                ChatHud.fill(matrices, getRight(k) - 4, t - l, getLeft(k) - 2, t, v);
                if (bl && visible.endOfEntry() && messageIndicator.icon() != null) {
                    int w = this.getIndicatorX(visible);
                    int x = u + client.textRenderer.fontHeight;
                    this.drawIndicatorIcon(matrices, w, x, messageIndicator.icon());
                }
            }
            RenderSystem.enableBlend();
            matrices.translate(0.0, 0.0, 50.0);
            client.textRenderer.drawWithShadow(matrices, visible.content(), getRight(k), (float)u, 0xFFFFFF + (q << 24));
            RenderSystem.disableBlend();
            matrices.pop();
        }
        long y = client.getMessageHandler().getUnprocessedMessageCount();
        if (y > 0L) {
            o = (int)(128.0 * d);
            int z = (int)(255.0 * e);
            matrices.push();
            matrices.translate(0.0, 0.0, 50.0);
            ChatHud.fill(matrices, -2, 0, k + 4, 9, z << 24);
            RenderSystem.enableBlend();
            matrices.translate(0.0, 0.0, 50.0);
            client.textRenderer.drawWithShadow(matrices, Text.translatable("chat.queue", y), 0.0f, 1.0f, 0xFFFFFF + (o << 24));
            matrices.pop();
            RenderSystem.disableBlend();
        }
        if (bl) {
            o = this.getLineHeight();
            int z = j * o;
            int aa = m * o;
            q = this.scrolledLines * aa / j;
            r = aa * aa / z;
            if (z != aa) {
                s = q > 0 ? 170 : 96;
                t = this.hasUnreadNewMessages ? 0xCC3333 : 0x3333AA;
                u = k + 4;
                ChatHud.fill(matrices, u, -q, u + 2, -q - r, t + (s << 24));
                ChatHud.fill(matrices, u + 2, -q, u + 1, -q - r, 0xCCCCCC + (s << 24));
            }
        }
        matrices.pop();
        info.cancel();
    }

    private int getLeft(int k) {
        if (RightChat.config.chatOnRight) {
            return MinecraftClient.getInstance().getWindow().getScaledWidth() - k;
        } else {
            return 0;
        }
    }

    private int getRight(int k) {
        if (RightChat.config.chatOnRight) {
            return MinecraftClient.getInstance().getWindow().getScaledWidth();
        } else {
            return k;
        }
    }

    private static double getMessageOpacityMultiplier(int age) {
        double d = (double)age / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = MathHelper.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }

}