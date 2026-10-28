package com.jacks.moviet.favorites.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.components.MovieMetadata
import com.jacks.moviet.core.designsystem.components.MoviePoster
import com.jacks.moviet.core.designsystem.components.MovieTitle
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.core.domain.model.Movie

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun FavoriteTicket(
    movie: Movie,
    selected: Boolean,
    compact: Boolean,
    removing: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().ticketCutouts(colors.background)
            .testTag("favorite_${movie.imdbId}").semantics { this.selected = selected },
        shape = MaterialTheme.shapes.large,
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        border = BorderStroke(TicketBorderWidth, if (selected) colors.primary else colors.outlineVariant),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            TicketBody(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                movie = movie,
                compact = compact,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = onClick,
            )
            TicketStub(
                modifier = Modifier.width(TicketStubWidth).fillMaxHeight(),
                movieTitle = movie.title,
                removing = removing,
                onRemove = onRemove,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TicketBody(
    modifier: Modifier = Modifier,
    movie: Movie,
    compact: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick)
            .padding(if (compact) Spacing.compact else Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.compact),
    ) {
        MoviePoster(
            modifier = Modifier.width(if (compact) CompactPosterWidth else PosterWidth).aspectRatio(PosterAspectRatio),
            movie = movie,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            MovieTitle(
                movie = movie,
                maxLines = TicketTitleMaxLines,
                overflow = TextOverflow.Ellipsis,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            MovieMetadata(
                movie = movie,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }
}

@Composable
private fun TicketStub(
    modifier: Modifier = Modifier,
    movieTitle: String,
    removing: Boolean,
    onRemove: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val removeLabel = stringResource(R.string.favorites_remove_movie, movieTitle)
    IconButton(
        modifier = modifier.background(colors.primary)
            .semantics { contentDescription = removeLabel }
            .drawWithContent {
                drawContent()
                val edge = if (layoutDirection == LayoutDirection.Ltr) 0f else size.width
                drawLine(
                    color = colors.onPrimary.copy(alpha = PerforationAlpha),
                    start = Offset(edge, NotchRadius.toPx()),
                    end = Offset(edge, size.height - NotchRadius.toPx()),
                    strokeWidth = TicketBorderWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(PerforationDash.toPx(), PerforationDash.toPx())),
                )
            },
        enabled = !removing,
        onClick = onRemove,
    ) {
        Icon(
            modifier = Modifier.size(BookmarkSize),
            painter = painterResource(R.drawable.ic_bookmark_filled),
            contentDescription = null,
            tint = colors.onPrimary,
        )
    }
}

private fun Modifier.ticketCutouts(background: Color) = drawWithContent {
    // Apply after the whole Surface, including its border, has been drawn.
    drawContent()
    val seam = if (layoutDirection == LayoutDirection.Ltr) size.width - TicketStubWidth.toPx() else TicketStubWidth.toPx()
    drawCircle(background, NotchRadius.toPx(), Offset(seam, 0f))
    drawCircle(background, NotchRadius.toPx(), Offset(seam, size.height))
}

private val PosterWidth = 64.dp
private val CompactPosterWidth = 48.dp
private val TicketStubWidth = 56.dp
private val BookmarkSize = 32.dp
private val NotchRadius = 7.dp
private val PerforationDash = 4.dp
private val TicketBorderWidth = 1.dp
private const val PosterAspectRatio = 2f / 3f
private const val TicketTitleMaxLines = 3
private const val PerforationAlpha = 0.35f
