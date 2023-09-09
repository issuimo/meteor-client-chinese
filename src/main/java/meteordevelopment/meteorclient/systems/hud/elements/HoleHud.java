/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HoleHud extends HudElement {
    public static final HudElementInfo<HoleHud> INFO = new HudElementInfo<>(Hud.GROUP, "洞穴", "显示你所站的洞穴信息", HoleHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("背景");

    // General

    public final Setting<List<Block>> safe = sgGeneral.add(new BlockListSetting.Builder()
        .name("安全方块")
        .description("那些方块是安全的")
        .defaultValue(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("缩放")
        .description("缩放比例")
        .defaultValue(2)
        .onChanged(aDouble -> calculateSize())
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("边框")
        .description("在控件周围添加空白边框")
        .defaultValue(0)
        .onChanged(integer -> calculateSize())
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("背景")
        .description("显示背景")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("背景颜色")
        .description("背景使用颜色")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private final Color BG_COLOR = new Color(255, 25, 25, 100);
    private final Color OL_COLOR = new Color(255, 25, 25, 255);

    public HoleHud() {
        super(INFO);

        calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    private void calculateSize() {
        setSize(16 * 3 * scale.get(), 16 * 3 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x + border.get();
            double y = this.y + border.get();

            drawBlock(renderer, get(Facing.Left), x, y + 16 * scale.get()); // Left
            drawBlock(renderer, get(Facing.Front), x + 16 * scale.get(), y); // Front
            drawBlock(renderer, get(Facing.Right), x + 32 * scale.get(), y + 16 * scale.get()); // Right
            drawBlock(renderer, get(Facing.Back), x + 16 * scale.get(), y + 32 * scale.get()); // Back
        });

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }
    }

    private Direction get(Facing dir) {
        if (isInEditor()) return Direction.DOWN;
        return Direction.fromRotation(MathHelper.wrapDegrees(mc.player.getYaw() + dir.offset));
    }

    private void drawBlock(HudRenderer renderer, Direction dir, double x, double y) {
        Block block = dir == Direction.DOWN ? Blocks.OBSIDIAN : mc.world.getBlockState(mc.player.getBlockPos().offset(dir)).getBlock();
        if (!safe.get().contains(block)) return;

        renderer.item(block.asItem().getDefaultStack(), (int) x, (int) y, scale.get().floatValue(), false);

        if (dir == Direction.DOWN) return;

        ((WorldRendererAccessor) mc.worldRenderer).getBlockBreakingInfos().values().forEach(info -> {
            if (info.getPos().equals(mc.player.getBlockPos().offset(dir))) {
                renderBreaking(renderer, x, y, info.getStage() / 9f);
            }
        });
    }

    private void renderBreaking(HudRenderer renderer, double x, double y, double percent) {
        renderer.quad(x, y, (16 * percent) * scale.get(), 16 * scale.get(), BG_COLOR);
        renderer.quad(x, y, 16 * scale.get(), 1 * scale.get(), OL_COLOR);
        renderer.quad(x, y + 15 * scale.get(), 16 * scale.get(), 1 * scale.get(), OL_COLOR);
        renderer.quad(x, y, 1 * scale.get(), 16 * scale.get(), OL_COLOR);
        renderer.quad(x + 15 * scale.get(), y, 1 * scale.get(), 16 * scale.get(), OL_COLOR);
    }

    private enum Facing {
        Left(-90),
        Right(90),
        Front(0),
        Back(180);

        public final int offset;

        Facing(int offset) {
            this.offset = offset;
        }
    }
}
