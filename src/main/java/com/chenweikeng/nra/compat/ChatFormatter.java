package com.chenweikeng.nra.compat;

import com.chenweikeng.monkeycraft_api.v1.ChatMessageResult;
import com.chenweikeng.monkeycraft_api.v1.IncomingChatContext;
import com.chenweikeng.monkeycraft_api.v1.OutgoingChatContext;
import com.vdurmont.emoji.EmojiManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

final class ChatFormatter {
  private ChatFormatter() {}

  static ChatMessageResult onIncomingChat(IncomingChatContext context) {
    Component message = context.getMessage();
    String senderUuid = context.getSenderUuid();
    String senderName = context.getSenderName();

    return processIncomingMessage(context, message, senderUuid, senderName);
  }

  static ChatMessageResult onOutgoingChat(OutgoingChatContext context) {
    String message = context.getMessage();

    return processOutgoingMessage(message);
  }

  private static ChatMessageResult processIncomingMessage(
      IncomingChatContext context, Component message, String senderUuid, String senderName) {
    boolean[] modified = {false};
    Component filtered = filterComponent(message, modified);

    if (modified[0]) {
      context.setMessage(filtered);
      return ChatMessageResult.MODIFY;
    }

    return ChatMessageResult.PASS;
  }

  private static final int IF_PLUS_COLOR = 0x2E86DE;
  private static final String IF_PLUS_TEXT = "IF+ Subscription Streak";
  private static final int WHITE_COLOR = 0xFFFFFF;
  private static final int EMOJI_TOOLTIP_COLOR = 0xC8D6E5;

  private static boolean containsEmoji(String input) {
    return EmojiManager.isEmoji(input);
  }

  private static int findFirstReplacementChar(String text) {
    for (int i = 0; i < text.length(); i++) {
      String ch = text.substring(i, i + 1);
      if (REPLACEMENT_TABLE.containsKey(ch)) {
        return i;
      }
    }
    return -1;
  }

  private static final Map<String, Component> REPLACEMENT_TABLE = new HashMap<>();

  static {
    REPLACEMENT_TABLE.put(
        "\u4E00", Component.literal("[Imagineer]").withStyle(Style.EMPTY.withColor(0x0176BC)));
    REPLACEMENT_TABLE.put(
        "\u4E01", Component.literal("[Developer]").withStyle(Style.EMPTY.withColor(0xFA8231)));
    REPLACEMENT_TABLE.put(
        "\u4E02", Component.literal("[Manager]").withStyle(Style.EMPTY.withColor(0x8406DE)));
    REPLACEMENT_TABLE.put(
        "\u4E03", Component.literal("[Character]").withStyle(Style.EMPTY.withColor(0x2CC129)));
    REPLACEMENT_TABLE.put(
        "\u4E04", Component.literal("[Builder]").withStyle(Style.EMPTY.withColor(0x1E5DEB)));
    REPLACEMENT_TABLE.put(
        "\u4E05", Component.literal("[Operator]").withStyle(Style.EMPTY.withColor(0x8406DE)));
    REPLACEMENT_TABLE.put(
        "\u4E06", Component.literal("[Coordinator]").withStyle(Style.EMPTY.withColor(0xDB4BC6)));
    REPLACEMENT_TABLE.put(
        "\u4E07", Component.literal("[CastMember]").withStyle(Style.EMPTY.withColor(0xFECA57)));
    REPLACEMENT_TABLE.put(
        "\u4E08", Component.literal("[TourGuide]").withStyle(Style.EMPTY.withColor(0x0C9201)));
    REPLACEMENT_TABLE.put(
        "\u4E09", Component.literal("[Trainee]").withStyle(Style.EMPTY.withColor(0x0C9201)));
    REPLACEMENT_TABLE.put(
        "\u4E0A", Component.literal("[DVC+]").withStyle(Style.EMPTY.withColor(0x0176BC)));
    REPLACEMENT_TABLE.put(
        "\u4E16", Component.literal("[Media]").withStyle(Style.EMPTY.withColor(0x0175B9)));
    REPLACEMENT_TABLE.put(
        "\u4E0B", Component.literal("[DVC]").withStyle(Style.EMPTY.withColor(0x0176BC)));
    REPLACEMENT_TABLE.put(
        "\u4E0C", Component.literal("[Club33+]").withStyle(Style.EMPTY.withColor(0x018178)));
    REPLACEMENT_TABLE.put(
        "\u4E0D", Component.literal("[Club33]").withStyle(Style.EMPTY.withColor(0x018178)));
    REPLACEMENT_TABLE.put(
        "\u4E0E", Component.literal("[D23+]").withStyle(Style.EMPTY.withColor(0x0175B9)));
    REPLACEMENT_TABLE.put(
        "\u4E0F", Component.literal("[D23]").withStyle(Style.EMPTY.withColor(0x0175B9)));
    REPLACEMENT_TABLE.put(
        "\u4E10", Component.literal("[Passholder+]").withStyle(Style.EMPTY.withColor(0xDB2222)));
    REPLACEMENT_TABLE.put(
        "\u4E11", Component.literal("[Passholder]").withStyle(Style.EMPTY.withColor(0xDB2222)));
    REPLACEMENT_TABLE.put(
        "\u4E14", Component.literal("[Guest+]").withStyle(Style.EMPTY.withColor(0xA9A9A9)));
    REPLACEMENT_TABLE.put(
        "\u4E15", Component.literal("[Guest]").withStyle(Style.EMPTY.withColor(0xA9A9A9)));
    REPLACEMENT_TABLE.put(
        "\u4E12", Component.literal("[VIP]").withStyle(Style.EMPTY.withColor(0x0175A9)));
    REPLACEMENT_TABLE.put(
        "\u4E60", Component.literal("[VIP+]").withStyle(Style.EMPTY.withColor(0xF368E0)));
    REPLACEMENT_TABLE.put(
        "\u4E89", Component.literal("[PixelArtist]").withStyle(Style.EMPTY.withColor(0x2D83DA)));
    REPLACEMENT_TABLE.put(
        "\u4E98",
        Component.literal("[JuniorTourGuide]").withStyle(Style.EMPTY.withColor(0x0C9201)));
    REPLACEMENT_TABLE.put(
        "\u4E99", Component.literal("[ShowTech]").withStyle(Style.EMPTY.withColor(0xBB2DD9)));
    REPLACEMENT_TABLE.put(
        "\u4E9A", Component.literal("[Discord]").withStyle(Style.EMPTY.withColor(0x7289DA)));
    REPLACEMENT_TABLE.put(
        "\u4E9E", Component.literal("[Artist]").withStyle(Style.EMPTY.withColor(0x47D7F7)));
    REPLACEMENT_TABLE.put(
        "\u4EAC", Component.literal("[Director]").withStyle(Style.EMPTY.withColor(0xCA3767)));
    REPLACEMENT_TABLE.put(
        "\u4E17", Component.literal("[Guest+++]").withStyle(Style.EMPTY.withColor(0xA9A9A9)));
    REPLACEMENT_TABLE.put(
        "\u6BAC", Component.literal("[Former]").withStyle(Style.EMPTY.withColor(0x2B9270)));
    REPLACEMENT_TABLE.put(
        "\u4EF7",
        Component.literal("[Shout]").withStyle(Style.EMPTY.withColor(0xEB1A3F).withBold(true)));
    REPLACEMENT_TABLE.put("\uF000", null);
    REPLACEMENT_TABLE.put("\uF001", null);
    REPLACEMENT_TABLE.put("\uF002", null);
    REPLACEMENT_TABLE.put("\uF003", null);
    REPLACEMENT_TABLE.put("\uF004", null);
    REPLACEMENT_TABLE.put("\uF005", null);
    REPLACEMENT_TABLE.put("\uF006", null);
    REPLACEMENT_TABLE.put("\uF007", null);
    REPLACEMENT_TABLE.put("\uF008", null);
    REPLACEMENT_TABLE.put("\uF009", null);
    REPLACEMENT_TABLE.put("\uF00A", null);
    REPLACEMENT_TABLE.put("\uF00B", null);
    REPLACEMENT_TABLE.put("\uF00C", null);
    REPLACEMENT_TABLE.put("\uF00D", null);
    REPLACEMENT_TABLE.put("\uF00E", null);
    REPLACEMENT_TABLE.put("\uF00F", null);
    REPLACEMENT_TABLE.put(
        "\u4E6C", Component.literal("[Uncommon]").withStyle(Style.EMPTY.withColor(0x32FF7E)));
    REPLACEMENT_TABLE.put(
        "\u4E67", Component.literal("[Common]").withStyle(Style.EMPTY.withColor(0xA9A9A9)));
    REPLACEMENT_TABLE.put(
        "\u4E6B", Component.literal("[Rare]").withStyle(Style.EMPTY.withColor(0x54A0FF)));
    REPLACEMENT_TABLE.put(
        "\u4EA5", Component.literal("[Exotic]").withStyle(Style.EMPTY.withColor(0x48DBFB)));
    REPLACEMENT_TABLE.put(
        "\u4E68", Component.literal("[Epic]").withStyle(Style.EMPTY.withColor(0xBE2EDD)));
    REPLACEMENT_TABLE.put(
        "\u4E69", Component.literal("[Legendary]").withStyle(Style.EMPTY.withColor(0xFF9F43)));
    REPLACEMENT_TABLE.put(
        "\u4E6A", Component.literal("[Mythic]").withStyle(Style.EMPTY.withColor(0xFD9644)));
    REPLACEMENT_TABLE.put(
        "\u4EA6", Component.literal("[Unobtainable]").withStyle(Style.EMPTY.withColor(0x576574)));
  }

  private static Component filterComponent(Component component, boolean[] modified) {
    MutableComponent result = Component.empty();

    component.visit(
        (style, text) -> {
          if (text.isEmpty()) {
            return Optional.empty();
          }

          boolean shouldModify = false;

          HoverEvent hoverEvent = style.getHoverEvent();
          if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            HoverEvent.ShowText showText = (HoverEvent.ShowText) hoverEvent;
            Component hoverText = showText.value();
            if (hoverText != null) {
              if (hoverText.getString().equals(IF_PLUS_TEXT)) {
                Style hoverStyle = hoverText.getStyle();
                if (hoverStyle != null
                    && hoverStyle.getColor() != null
                    && hoverStyle.getColor().getValue() == IF_PLUS_COLOR) {
                  shouldModify = true;
                }
              }
            }
          }

          if (shouldModify) {
            modified[0] = true;
            return Optional.empty();
          }

          TextColor color = style.getColor();
          boolean isWhite = color != null && color.getValue() == WHITE_COLOR;

          int matchIndex = findFirstReplacementChar(text);
          if (matchIndex >= 0) {
            modified[0] = true;
            String before = text.substring(0, matchIndex);
            String matchChar = text.substring(matchIndex, matchIndex + 1);
            String after = text.substring(matchIndex + 1);

            if (!before.isEmpty()) {
              result.append(Component.literal(before).withStyle(style));
            }

            Component replacement = REPLACEMENT_TABLE.get(matchChar);
            if (replacement != null) {
              result.append(replacement);
            } else if (!REPLACEMENT_TABLE.containsKey(matchChar)) {
              result.append(Component.literal(matchChar).withStyle(style));
            }

            while (!after.isEmpty()) {
              int nextMatch = findFirstReplacementChar(after);
              if (nextMatch < 0) {
                result.append(Component.literal(after).withStyle(style));
                break;
              }
              String nextBefore = after.substring(0, nextMatch);
              String nextChar = after.substring(nextMatch, nextMatch + 1);
              after = after.substring(nextMatch + 1);

              if (!nextBefore.isEmpty()) {
                result.append(Component.literal(nextBefore).withStyle(style));
              }

              Component nextReplacement = REPLACEMENT_TABLE.get(nextChar);
              if (nextReplacement != null) {
                result.append(nextReplacement);
              } else if (!REPLACEMENT_TABLE.containsKey(nextChar)) {
                result.append(Component.literal(nextChar).withStyle(style));
              }
            }
            return Optional.empty();
          }

          if (text.codePointCount(0, text.length()) == 1
              && !containsEmoji(text)
              && hoverEvent != null
              && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT
              && isWhite) {
            HoverEvent.ShowText showText = (HoverEvent.ShowText) hoverEvent;
            Component hoverTextComponent = showText.value();
            if (hoverTextComponent != null) {
              String hoverString = hoverTextComponent.getString();
              if (hoverString.startsWith(":")) {
                Style hoverStyle = hoverTextComponent.getStyle();
                if (hoverStyle != null
                    && hoverStyle.getColor() != null
                    && hoverStyle.getColor().getValue() == EMOJI_TOOLTIP_COLOR) {
                  modified[0] = true;
                  result.append(Component.literal(hoverString));
                  return Optional.empty();
                }
              }
            }
          }

          result.append(Component.literal(text).withStyle(style));

          return Optional.empty();
        },
        component.getStyle());

    return result;
  }

  private static ChatMessageResult processOutgoingMessage(String message) {
    return ChatMessageResult.PASS;
  }
}
