package com.matin.composeanimation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import com.matin.composeanimation.ui.theme.ComposeAnimationTheme
import kotlinx.coroutines.launch


@Composable
fun ItemSelectionScreen(modifier: Modifier = Modifier) {
    var isSelected by remember { mutableStateOf(false) }
    SelectableTextItem(
        modifier = modifier,
        title = "Title",
        subtitle = "Some subtitle text goes here to test",
        isSelected = isSelected,
        onClick = {
            isSelected = it
        }
    )
}

@Composable
fun SelectableTextItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    color: Color = MaterialTheme.colorScheme.onBackground,
    isSelected: Boolean,
    onClick: (isSelected: Boolean) -> Unit = {},
) {
    val currentColor = if (isSelected) color else Color.Gray
    val cornerRadius = RoundedCornerShape(10.dp)

    val iconScale = remember { Animatable(initialValue = 1f) }
    val bodyScale = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(key1 = isSelected) {
        if (isSelected) {
            launch {
                animateIconScale(iconScale)
            }
            launch {
                animateBodyScale(bodyScale)
            }
        } else {
            iconScale.snapTo(1f)
            bodyScale.snapTo(1f)
        }
    }
    Column(
        modifier = modifier
            .scale(bodyScale.value)
            .border(
                width = 1.dp,
                color = currentColor,
                shape = cornerRadius
            )
            .clickable {
                onClick(!isSelected)
            }
            .padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier, horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = currentColor,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .scale(iconScale.value),
                contentDescription = "Selected",
                tint = currentColor,
            )
        }
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                color = currentColor,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private suspend fun animateIconScale(iconScale: Animatable<Float, AnimationVector1D>) {
    iconScale.animateTo(
        targetValue = .3f,
        animationSpec = tween(
            durationMillis = 200
        )
    )
    iconScale.animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 200
        )
    )
}

private suspend fun animateBodyScale(bodyScale: Animatable<Float, AnimationVector1D>) {
    bodyScale.animateTo(
        targetValue = .9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    bodyScale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SelectableItemPreview(modifier: Modifier = Modifier) {
    ComposeAnimationTheme {
        SelectableTextItem(
            title = "Title",
            subtitle = "Subtitle",
            isSelected = true
        )
    }
}