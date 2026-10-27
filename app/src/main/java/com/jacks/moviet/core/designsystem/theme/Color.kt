package com.jacks.moviet.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// UI chrome from the reference: amber selection, charcoal surfaces and off-white text.
// Movie artwork and the presentation's surrounding scenery are not theme colors.
private val Amber = Color(0xFFFFB74D)
private val DeepBrown = Color(0xFF241705)
private val AmberBrown = Color(0xFF503512)
private val PaleAmber = Color(0xFFFFDDB0)
private val Ochre = Color(0xFF805300)
private val WarmGrey = Color(0xFFCEC5B8)
private val DarkWarmGrey = Color(0xFF25221C)
private val DarkTaupe = Color(0xFF36312A)
private val PaleBeige = Color(0xFFEAE1D4)
private val Sand = Color(0xFFE4C18F)
private val Espresso = Color(0xFF2D2110)
private val Walnut = Color(0xFF44331C)
private val Peach = Color(0xFFFFDFB2)

private val Charcoal = Color(0xFF080B0D)
private val OffWhite = Color(0xFFF2F2F2)
private val BlueCharcoal = Color(0xFF1B2227)
private val Silver = Color(0xFFB7BFC7)
private val LightGrey = Color(0xFFE4E7EA)
private val DarkSlate = Color(0xFF1B2024)
private val SlateGrey = Color(0xFF78828B)
private val DeepSlateGrey = Color(0xFF384148)
private val Black = Color(0xFF000000)
private val LightCharcoal = Color(0xFF30383E)
private val NearBlack = Color(0xFF050708)
private val DeepCharcoal = Color(0xFF0E1215)
private val DarkCharcoal = Color(0xFF141A1E)
private val MediumCharcoal = Color(0xFF1E252A)
private val SoftCharcoal = Color(0xFF293138)

private val Rose = Color(0xFFFFB4AB)
private val Maroon = Color(0xFF690005)
private val Crimson = Color(0xFF93000A)
private val PaleRose = Color(0xFFFFDAD6)

private val DarkUmber = Color(0xFF291800)
private val Caramel = Color(0xFF614000)
private val BlackBrown = Color(0xFF201B13)
private val Taupe = Color(0xFF4D463B)
private val Chestnut = Color(0xFF594326)

internal val MovietDarkColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = DeepBrown,
    primaryContainer = AmberBrown,
    onPrimaryContainer = PaleAmber,
    inversePrimary = Ochre,
    secondary = WarmGrey,
    onSecondary = DarkWarmGrey,
    secondaryContainer = DarkTaupe,
    onSecondaryContainer = PaleBeige,
    tertiary = Sand,
    onTertiary = Espresso,
    tertiaryContainer = Walnut,
    onTertiaryContainer = Peach,
    background = Charcoal,
    onBackground = OffWhite,
    surface = Charcoal,
    onSurface = OffWhite,
    surfaceVariant = BlueCharcoal,
    onSurfaceVariant = Silver,
    // Keep elevated surfaces neutral; the poster art supplies the cinematic color.
    surfaceTint = Color.Transparent,
    inverseSurface = LightGrey,
    inverseOnSurface = DarkSlate,
    error = Rose,
    onError = Maroon,
    errorContainer = Crimson,
    onErrorContainer = PaleRose,
    outline = SlateGrey,
    outlineVariant = DeepSlateGrey,
    scrim = Black,
    surfaceBright = LightCharcoal,
    surfaceDim = Charcoal,
    surfaceContainerLowest = NearBlack,
    surfaceContainerLow = DeepCharcoal,
    surfaceContainer = DarkCharcoal,
    surfaceContainerHigh = MediumCharcoal,
    surfaceContainerHighest = SoftCharcoal,
    primaryFixed = PaleAmber,
    primaryFixedDim = Amber,
    onPrimaryFixed = DarkUmber,
    onPrimaryFixedVariant = Caramel,
    secondaryFixed = PaleBeige,
    secondaryFixedDim = WarmGrey,
    onSecondaryFixed = BlackBrown,
    onSecondaryFixedVariant = Taupe,
    tertiaryFixed = Peach,
    tertiaryFixedDim = Sand,
    onTertiaryFixed = DarkUmber,
    onTertiaryFixedVariant = Chestnut,
)
