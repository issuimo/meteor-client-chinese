/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Config extends System<Config> {
    public final Settings settings = new Settings();

    private final SettingGroup sgVisual = settings.createGroup("视觉");
    private final SettingGroup sgChat = settings.createGroup("聊天");
    private final SettingGroup sgMisc = settings.createGroup("杂项");

    // Visual

    public final Setting<Boolean> customFont = sgVisual.add(new BoolSetting.Builder()
        .name("自定义字体")
        .description("使用一个自定义字体")
        .defaultValue(false)
        .build()
    );

    public final Setting<FontFace> font = sgVisual.add(new FontFaceSetting.Builder()
        .name("字体")
        .description("使用的自定义字体")
        .visible(customFont::get)
        .onChanged(Fonts::load)
        .build()
    );

    public final Setting<Double> rainbowSpeed = sgVisual.add(new DoubleSetting.Builder()
        .name("彩色渐变速度")
        .description("控制全局彩色渐变速度")
        .defaultValue(0.5)
        .range(0, 10)
        .sliderMax(5)
        .build()
    );

    public final Setting<Boolean> titleScreenCredits = sgVisual.add(new BoolSetting.Builder()
        .name("主界面水印")
        .description("显示水印在主界面屏幕上")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> titleScreenSplashes = sgVisual.add(new BoolSetting.Builder()
        .name("主界面次标题")
        .description("在主界面次标题上显示Meteor的文本")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> customWindowTitle = sgVisual.add(new BoolSetting.Builder()
        .name("自定义窗口标题")
        .description("在窗口的标题上显示自定义名称")
        .defaultValue(false)
        .onModuleActivated(setting -> mc.updateWindowTitle())
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    public final Setting<String> customWindowTitleText = sgVisual.add(new StringSetting.Builder()
        .name("自定义标题内容")
        .description("这段文本将会显示在窗口标题上")
        .visible(customWindowTitle::get)
        .defaultValue("Minecraft {mc_version} - {meteor.name} {meteor.version}")
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    public final Setting<SettingColor> friendColor = sgVisual.add(new ColorSetting.Builder()
        .name("好友颜色")
        .description("这个颜色将用于显示自己的好友")
        .defaultValue(new SettingColor(0, 255, 180))
        .build()
    );

    // Chat

    public final Setting<String> prefix = sgChat.add(new StringSetting.Builder()
        .name("聊天前缀")
        .description("Prefix.")
        .defaultValue(".")
        .build()
    );

    public final Setting<Boolean> chatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("日志反馈")
        .description("当meteor执行某些操作时在聊天栏显示。")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> deleteChatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("精简日志")
        .description("删除与之前匹配的日志, 精简日志输出")
        .visible(chatFeedback::get)
        .defaultValue(true)
        .build()
    );

    // Misc

    public final Setting<Integer> rotationHoldTicks = sgMisc.add(new IntSetting.Builder()
        .name("保持连接")
        .description("当不发送任何数据包时，长时间保持服务器连接")
        .defaultValue(4)
        .build()
    );

    public final Setting<Boolean> useTeamColor = sgMisc.add(new BoolSetting.Builder()
        .name("使用队伍颜色")
        .description("使用玩家的团队颜色来渲染透视和射线之类的东西")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> moduleSearchCount = sgMisc.add(new IntSetting.Builder()
        .name("模块搜索计数")
        .description("模块搜索栏中显示的模块数量和设置")
        .defaultValue(12)
        .min(1).sliderMax(12)
        .build()
    );

    public List<String> dontShowAgainPrompts = new ArrayList<>();

    public Config() {
        super("配置");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", settings.toTag());
        tag.put("dontShowAgainPrompts", listToTag(dontShowAgainPrompts));

        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("dontShowAgainPrompts")) dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");

        return this;
    }

    private NbtList listToTag(List<String> list) {
        NbtList nbt = new NbtList();
        for (String item : list) nbt.add(NbtString.of(item));
        return nbt;
    }

    private List<String> listFromTag(NbtCompound tag, String key) {
        List<String> list = new ArrayList<>();
        for (NbtElement item : tag.getList(key, 8)) list.add(item.asString());
        return list;
    }
}
