package com.bekirahmetli.instagramapp.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ProfileImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    imageSize: Int = 96,
    borderColor: Color = Color.Black,
    borderWidth: Int = 2,
    onClick: () -> Unit
) {
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(imageSize.dp)
            .border(width = borderWidth.dp, color = borderColor, shape = CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}