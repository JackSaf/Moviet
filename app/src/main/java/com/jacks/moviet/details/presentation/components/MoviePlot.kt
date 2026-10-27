package com.jacks.moviet.details.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.MovietMotion
import com.jacks.moviet.core.designsystem.theme.Spacing
import java.text.BreakIterator

@Composable
internal fun MoviePlot(
    modifier: Modifier = Modifier,
    imdbId: String,
    plot: String,
) {
    var expanded by rememberSaveable(imdbId) { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyMedium
    val linkStyle = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    val readMore = stringResource(R.string.read_more)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Text(stringResource(R.string.movie_plot), style = MaterialTheme.typography.titleMedium)
        // Measure against this paragraph's width so the inline link fits on the first frame.
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val textWidth = constraints.maxWidth
            val collapsedPlot = remember(plot, textWidth, textStyle, linkStyle, readMore, textMeasurer) {
                collapsedPlot(
                    plot = plot,
                    readMore = readMore,
                    textWidth = textWidth,
                    textStyle = textStyle,
                    linkStyle = linkStyle,
                    textMeasurer = textMeasurer,
                )
            }
            val collapsedText = remember(imdbId, collapsedPlot, plot, readMore, linkStyle) {
                if (collapsedPlot == null) AnnotatedString(plot) else {
                    plotWithReadMore(collapsedPlot, readMore, linkStyle) { expanded = true }
                }
            }
            val expandedText = remember(plot) { AnnotatedString(plot) }
            val (collapsedHeight, expandedHeight) = remember(
                collapsedText, expandedText, textWidth, textStyle, textMeasurer,
            ) {
                val textConstraints = Constraints(maxWidth = textWidth)
                textMeasurer.measure(collapsedText, textStyle, constraints = textConstraints).size.height to
                    textMeasurer.measure(expandedText, textStyle, constraints = textConstraints).size.height
            }
            val density = LocalDensity.current
            val height by animateDpAsState(
                targetValue = with(density) { (if (expanded) expandedHeight else collapsedHeight).toDp() },
                animationSpec = MovietMotion.defaultSpatial(),
                label = "Plot height",
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clipToBounds()
                    .wrapContentHeight(align = Alignment.Top, unbounded = true),
                text = if (expanded) expandedText else collapsedText,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun plotWithReadMore(
    plot: String,
    label: String,
    linkStyle: SpanStyle,
    onReadMore: () -> Unit = {},
) = buildAnnotatedString {
    append(plot)
    append(' ')
    // The span also lets TextMeasurer include the link's font weight when reserving space.
    withStyle(linkStyle) {
        withLink(
            LinkAnnotation.Clickable(
                tag = "plot_read_more",
                styles = TextLinkStyles(style = linkStyle),
                linkInteractionListener = { onReadMore() },
            ),
        ) { append(label) }
    }
}

private fun collapsedPlot(
    plot: String,
    readMore: String,
    textWidth: Int,
    textStyle: TextStyle,
    linkStyle: SpanStyle,
    textMeasurer: TextMeasurer,
): String? {
    val constraints = Constraints(maxWidth = textWidth)
    val fullLayout = textMeasurer.measure(
        text = plot,
        style = textStyle,
        maxLines = CollapsedPlotLines,
        constraints = constraints,
    )
    if (!fullLayout.hasVisualOverflow) return null

    // Find the longest prefix that leaves the inline action visible on the final line.
    var start = 0
    var end = fullLayout.getLineEnd(CollapsedPlotLines - 1, visibleEnd = true)
    while (start < end) {
        val middle = (start + end + 1) / 2
        val candidate = plotWithReadMore("${plot.take(middle).trimEnd()}…", readMore, linkStyle)
        val layout = textMeasurer.measure(
            text = candidate,
            style = textStyle,
            maxLines = CollapsedPlotLines,
            constraints = constraints,
        )
        if (layout.hasVisualOverflow) end = middle - 1 else start = middle
    }
    val characters = BreakIterator.getCharacterInstance().apply { setText(plot) }
    val boundary = if (characters.isBoundary(start)) start else characters.preceding(start)
    return "${plot.take(boundary).trimEnd()}…"
}

private const val CollapsedPlotLines = 4
