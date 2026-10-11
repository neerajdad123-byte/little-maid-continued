package com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button;

import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.PlainButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ButtonWithId extends PlainButton {
    private final Consumer<Integer> onClick;
    private final int id;

    public ButtonWithId(int id, int x, int y, int width, int height, Component title, Consumer<Integer> onClick) {
        super(Button.builder(title, (b) -> {
        }).pos(x, y).size(width, height));
        this.id = id;
        this.onClick = onClick;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        this.onClick.accept(this.id);
    }
}
