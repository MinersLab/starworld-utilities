package starworld.core.util

import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.TextContent
import net.minecraft.util.Formatting

fun text(text: String? = null) = MutableText.of(TextContent.EMPTY).also {
    if (text != null) it.append(text)
}

fun text(text: Text? = null) = MutableText.of(TextContent.EMPTY).also {
    if (text != null) it.append(text)
}


fun MutableText.hover(hoverText: Text) = styled {
    it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
}

fun MutableText.color(formatting: Formatting) = styled { it.withColor(formatting) }
fun MutableText.color(formatting: TextColor) = styled { it.withColor(formatting) }
fun MutableText.color(formatting: Int) = styled { it.withColor(formatting) }
