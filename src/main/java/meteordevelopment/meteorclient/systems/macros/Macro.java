/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.macros;

import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.starscript.Script;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Macro implements ISerializable<Macro> {
    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("名称")
        .description("宏名称")
        .defaultValue("")
        .build()
    );

    public Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("消息 & 指令")
        .description("发送到聊天栏(如果是指令则执行)")
        .onChanged(v -> dirty = true)
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    public Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("快捷键")
        .description("绑定的快捷键")
        .build()
    );

    private final List<Script> scripts = new ArrayList<>(1);
    private boolean dirty;

    public Macro() {}
    public Macro(NbtElement tag) {
        fromTag((NbtCompound) tag);
    }

    public boolean onAction(boolean isKey, int value) {
        if (!keybind.get().matches(isKey, value) || mc.currentScreen != null) return false;
        return onAction();
    }

    public boolean onAction() {
            if (dirty) {
                scripts.clear();

                for (String message : messages.get()) {
                    Script script = MeteorStarscript.compile(message);
                    if (script != null) scripts.add(script);
                }

                dirty = false;
            }

            for (Script script : scripts) {
                String message = MeteorStarscript.run(script);

                if (message != null) {
                    ChatUtils.sendPlayerMsg(message);
                }
            }

            return true;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public Macro fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            settings.fromTag(tag.getCompound("settings"));
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Macro macro = (Macro) o;
        return Objects.equals(macro.name.get(), this.name.get());
    }
}
