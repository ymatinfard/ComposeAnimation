package com.matin.composeanimation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onSkipClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage by remember {
        derivedStateOf { pagerState.currentPage == pages.size - 1 }
    }
    var nextPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = nextPage) {
        pagerState.animateScrollToPage(nextPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isLastPage,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            TextButton(onClick = onSkipClick) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            modifier = Modifier.weight(.8f),
            state = pagerState
        ) { index ->
            OnboardingPage(page = pages[index], isCurrent = index == pagerState.currentPage)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PagerIndicator(
                modifier = Modifier.padding(bottom = 32.dp),
                pagerState = pagerState
            )
            NavigationButton(
                modifier = modifier,
                onNextClick = {nextPage = pagerState.currentPage + 1},
                isLastPage = isLastPage
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationButton(
    isLastPage: Boolean,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonWidth by animateDpAsState(
        targetValue = if (isLastPage) 200.dp else 160.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ButtonWidth"
    )

    Button(
        onClick = onNextClick,
        modifier = modifier
            .width(buttonWidth),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = isLastPage,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "ButtonTextAnimation"
            ) { isLast ->
                if (isLast) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (isLastPage) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = if (isLastPage) "Complete" else "Next"
            )
        }
    }
}

@Composable
fun PagerIndicator(modifier: Modifier = Modifier, pagerState: PagerState) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(pagerState.pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                animationSpec = tween(300),
            )
            val size by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(size)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun OnboardingPage(modifier: Modifier = Modifier, page: OnboardingPage, isCurrent: Boolean) {
    val enterTransition = remember {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    }

    val exitTransition = remember {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
    AnimatedVisibility(
        enter = enterTransition,
        exit = exitTransition,
        visible = isCurrent
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(.6f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(300.dp),
                    painter = painterResource(page.image),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Int
)

val pages = listOf(
    OnboardingPage(
        title = "Title1",
        description = "Description",
        image = R.drawable.ic_launcher_foreground
    ),
    OnboardingPage(
        title = "Title2",
        description = "Description",
        image = R.drawable.ic_launcher_foreground
    ),
    OnboardingPage(
        title = "Title3",
        description = "Description",
        image = R.drawable.ic_launcher_foreground
    )
)

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen()
}