package com.matin.composeanimation

import android.R.attr.height
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(modifier: Modifier = Modifier) {
    var counter by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        (0..50).asFlow().collect {
            counter = it
            delay(1000)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = counter,
            transitionSpec = {
                slideInVertically { height -> -height }.togetherWith(slideOutVertically { height -> height })
            }) { timer ->
            Text(
                text = timer.toString(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 62.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}