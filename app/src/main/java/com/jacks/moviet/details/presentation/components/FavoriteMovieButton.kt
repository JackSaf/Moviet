package com.jacks.moviet.details.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jacks.moviet.R
import com.jacks.moviet.core.designsystem.theme.MovietMotion
import com.jacks.moviet.core.designsystem.theme.Spacing
import com.jacks.moviet.details.presentation.FavoriteState
import com.jacks.moviet.details.presentation.FavoriteUpdate

@Composable
internal fun FavoriteMovieButton(
    modifier: Modifier = Modifier,
    state: FavoriteState,
    onClick: () -> Unit,
) {
    val ready = state as? FavoriteState.Ready
    val isFavorite = ready?.isFavorite == true
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (isFavorite) colors.primaryContainer else colors.primary,
        animationSpec = MovietMotion.fastEffects(),
        label = "Favorite container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFavorite) colors.onPrimaryContainer else colors.onPrimary,
        animationSpec = MovietMotion.fastEffects(),
        label = "Favorite content",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        Button(
            modifier = Modifier.fillMaxWidth().testTag("toggle_favorite"),
            onClick = onClick,
            enabled = ready != null && ready.update != FavoriteUpdate.SAVING,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                // A short Room write should not flash the disabled palette between states.
                disabledContainerColor = containerColor,
                disabledContentColor = contentColor,
            ),
            contentPadding = PaddingValues(Spacing.medium),
        ) {
            AnimatedContent(
                targetState = isFavorite,
                transitionSpec = {
                    (fadeIn(MovietMotion.fastEffects()) togetherWith fadeOut(MovietMotion.fastEffects()))
                        .using(SizeTransform { _, _ -> MovietMotion.fastSpatial() })
                },
                contentAlignment = Alignment.Center,
                label = "Favorite action",
            ) { favorite ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(if (favorite) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(stringResource(if (favorite) R.string.remove_ticket else R.string.save_ticket))
                }
            }
        }
        if (ready?.update == FavoriteUpdate.FAILED) {
            Text(stringResource(R.string.favorite_update_failed), color = MaterialTheme.colorScheme.error)
        }
    }
}
