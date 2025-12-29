package com.skyblockin.voidsurvival.util;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.*;
import io.papermc.paper.registry.data.dialog.type.DialogListType;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.type.MultiActionType;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class DialogUtil {

    public static DialogListType buildDialogList(List<Dialog> dialogs, ActionButton closeButton, int columns, int width) {
        return DialogType.dialogList(RegistrySet.valueSet(RegistryKey.DIALOG, dialogs), closeButton, columns, width);
    }

    public static MultiActionType buildMultiAction(List<ActionButton> buttons, ActionButton exitButton, int columns) {
        return DialogType.multiAction(buttons, exitButton, columns);
    }

    public static Dialog buildConfirmationDialog(String title, ActionButton yesButton, ActionButton noButton, String... content) {

        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(TextUtil.color(title)).body(buildDialogBody(content)).build())
            .type(DialogType.confirmation(yesButton, noButton))
        );

    }

    public static Dialog buildSimpleDialog(String title, ActionButton closeButton, String... content) {
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(TextUtil.color(title)).body(buildDialogBody(content)).build())
            .type(DialogType.notice(closeButton))
        );
    }

    public static List<DialogBody> buildDialogBody(String... content) {

        List<DialogBody> dialogBodyLines = new ArrayList<>();

        for (String line : content) {
            dialogBodyLines.add(DialogBody.plainMessage(TextUtil.color(line)));
        }

        return dialogBodyLines;
    }

    public static ActionButton buildActionButton(String text, int width, Consumer<Audience> action) {
        return ActionButton.builder(TextUtil.color(text))
            .width(width)
            .action(DialogAction.staticAction(ClickEvent.callback(action::accept)))
            .build();
    }

    public static NumberRangeDialogInput buildNumberRangeInput(String key, String label, float min, float max, String labelFormat, float initial, float step) {
        return DialogInput.numberRange(key, TextUtil.color(label), min, max)
            .labelFormat(labelFormat)
            .initial(initial)
            .step(step)
            .build();
    }

    public static TextDialogInput buildTextInput(String key, String label) {
        return DialogInput.text(key, TextUtil.color(label)).build();
    }

    public static BooleanDialogInput buildBooleanInput(String key, String label, String onFalse, String onTrue, boolean initialValue) {
        return DialogInput.bool(key, TextUtil.color(label))
            .onTrue(onTrue)
            .onFalse(onFalse)
            .initial(initialValue)
            .build();
    }

    public static SingleOptionDialogInput buildSingleOptionInput(String key, String label, List<SingleOptionDialogInput.OptionEntry> options) {
        return DialogInput.singleOption(key, TextUtil.color(label), options).build();
    }

    public static SingleOptionDialogInput.OptionEntry buildOptionEntry(String key, String label, boolean initialValue) {
        return SingleOptionDialogInput.OptionEntry.create(key, TextUtil.color(label), initialValue);
    }

}
