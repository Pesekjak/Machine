package me.pesekjak.machine.chat;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.pesekjak.machine.server.NBTSerializable;
import me.pesekjak.machine.utils.NamespacedKey;
import mx.kenzie.nbt.NBTCompound;
import mx.kenzie.nbt.NBTList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;

import static me.pesekjak.machine.chat.ChatType.Element.DEFAULT_NARRATION_ELEMENT;

/**
 * Different chat message types, used by Minecraft's chat system.
 */
@AllArgsConstructor
public enum ChatType implements NBTSerializable {

    CHAT(
            NamespacedKey.minecraft("chat"),
            Element.chat(
                    Set.of(Parameter.SENDER, Parameter.CONTENT),
                    "chat.type.text",
                    null
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    SAY_COMMAND(
            NamespacedKey.minecraft("say_command"),
            Element.chat(
                    Set.of(Parameter.SENDER, Parameter.CONTENT),
                    "chat.type.announcement",
                    null
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    MSG_COMMAND_INCOMING(
            NamespacedKey.minecraft("msg_command_incoming"),
            Element.chat(
                    Set.of(Parameter.SENDER, Parameter.CONTENT),
                    "commands.message.display.incoming",
                    Style.style()
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
                    .build()
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    MSG_COMMAND_OUTGOING(
            NamespacedKey.minecraft("msg_command_outgoing"),
            Element.chat(
                    Set.of(Parameter.TARGET, Parameter.CONTENT),
                    "commands.message.display.outgoing",
                    Style.style()
                    .color(NamedTextColor.GRAY)
                    .decorate(TextDecoration.ITALIC)
                    .build()
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    TEAM_MSG_COMMAND_INCOMING(
            NamespacedKey.minecraft("team_msg_command_incoming"),
            Element.chat(
                    Set.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT),
                    "chat.type.team.text",
                    null
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    TEAM_MSG_COMMAND_OUTGOING(
            NamespacedKey.minecraft("team_msg_command_outgoing"),
            Element.chat(
                    Set.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT),
                    "chat.type.team.sent",
                    null
            ),
            DEFAULT_NARRATION_ELEMENT
    ),
    EMOTE_COMMAND(
            NamespacedKey.minecraft("emote_command"),
            Element.chat(
                    Set.of(Parameter.SENDER, Parameter.TARGET),
                    "chat.type.emote",
                    null
            ),
            Element.narration(
                    Set.of(Parameter.SENDER, Parameter.CONTENT),
                    "chat.type.emote",
                    null
            )
    ),
    @Deprecated // Is not used by vanilla server?
    TELLRAW(
            NamespacedKey.minecraft("raw"),
            Element.chat(
                    Set.of(Parameter.CONTENT),
                    "%s",
                    null
            ),
            Element.narration(
                    Set.of(Parameter.CONTENT),
                    "%s",
                    null
            )
    );

    @Getter
    private final @NotNull NamespacedKey name;
    @Getter(AccessLevel.PROTECTED)
    protected final @NotNull Element chatElement;
    @Getter(AccessLevel.PROTECTED)
    protected final @NotNull Element narrationElement;

    public int getId() {
        return ordinal();
    }

    public static @NotNull ChatType fromID(@Range(from = 0, to = 7) int id) {
        Preconditions.checkArgument(id < values().length, "Unsupported Chat type");
        return values()[id];
    }

    @Override
    public @NotNull NBTCompound toNBT() {
        NBTCompound element = new NBTCompound(Map.of(
                "chat", chatElement.toNBT(),
                "narration", narrationElement.toNBT()
        ));
        return new NBTCompound(Map.of(
                "name", name.toString(),
                "id", ordinal(),
                "element", element
        ));
    }

    /**
     * Chat and Narration types of chat types, contain information
     * about their parameters, translation key and chat style.
     */
    protected record Element(@NotNull ElementType type,
                             @NotNull Set<Parameter> parameters,
                             @NotNull String translationKey,
                             @Nullable Style style) implements NBTSerializable {

        static final @NotNull Element DEFAULT_NARRATION_ELEMENT = Element.narration(
                Set.of(Parameter.SENDER, Parameter.CONTENT),
                "chat.type.text.narrate",
                null);

        /**
         * Creates new element of type chat.
         * @param parameters parameters of the element
         * @param translationKey translation key of the element
         * @param style chat style of the element
         * @return created chat type element
         */
        public static @NotNull Element chat(Set<Parameter> parameters, String translationKey, @Nullable Style style) {
            return new Element(ElementType.CHAT, parameters, translationKey, style);
        }
        /**
         * Creates new element of type narration.
         * @param parameters parameters of the element
         * @param translationKey translation key of the element
         * @param style chat style of the element
         * @return created chat type element
         */
        public static @NotNull Element narration(Set<Parameter> parameters, String translationKey, @Nullable Style style) {
            return new Element(ElementType.NARRATION, parameters, translationKey, style);
        }

        @Override
        public @NotNull NBTCompound toNBT() {
            final NBTList parameters = new NBTList(this.parameters.stream().map(Parameter::getName).toList());
            final Map<String, String> styleMap = new HashMap<>();
            if(style != null) {
                Map<TextDecoration, TextDecoration.State> decorations = style.decorations();
                for (TextDecoration decoration : decorations.keySet()) {
                    if (decorations.get(decoration) != TextDecoration.State.NOT_SET)
                        styleMap.put(decoration.toString(), decorations.get(decoration).toString());
                }
                TextColor color = style.color();
                if (color != null) {
                    NamedTextColor named = NamedTextColor.namedColor(color.value());
                    if (named != null)
                        styleMap.put("color", named.toString());
                    else
                        styleMap.put("color", color.asHexString());
                }
                Key font = style.font();
                if (font != null)
                    styleMap.put("font", font.asString());
            }
            NBTCompound style = new NBTCompound();
            for(String key : styleMap.keySet())
                style.set(key, styleMap.get(key));
            return new NBTCompound(Map.of(
                    "translation_key", translationKey,
                    "parameters", parameters,
                    "style", style
            ));
        }

    }

    /**
     * Type of chat type element.
     */
    @AllArgsConstructor
    protected enum ElementType {
        CHAT("chat"),
        NARRATION("narration");
        @Getter
        private final @NotNull String name;
    }

    /**
     * Parameters used by chat type elements.
     */
    @AllArgsConstructor
    protected enum Parameter {
        SENDER("sender"),
        TARGET("target"),
        CONTENT("content");
        @Getter
        private final @NotNull String name;
    }

}
