package com.example.tickerboard

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

const val TickerCycleMillis = 300

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TickerBoardScreen() {
    var text by remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        bottomBar = {
            var inputText by remember {
                mutableStateOf("")
            }
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = if (it.length > 20) {
                        it.substring(0..19)
                    } else {
                        it
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    text = inputText
                    keyboardController?.hide()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    ) {
        TickerBoard(
            text = text,
            numColumns = 5,
            numRows = 4,
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Composable
fun TickerBoard(
    text: String,
    numColumns: Int,
    numRows: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    fontSize: TextUnit = 96.sp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
) {
    val padded = text.padEnd(numColumns * numRows, ' ')
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(numRows) { row ->
            TickerRow(
                text = padded.substring(startIndex = row * numColumns), numCells = numColumns,
                horizontalArrangement = horizontalArrangement,
                textColor = textColor,
                backgroundColor = backgroundColor,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun TickerRow(
    text: String,
    numCells: Int,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    fontSize: TextUnit = 96.sp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        repeat(numCells) { index ->
            Ticker(
                letter = text.getOrNull(index) ?: ' ',
                textColor = textColor,
                backgroundColor = backgroundColor,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun Ticker(
    letter: Char,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    fontSize: TextUnit = 96.sp
) {
    val animatable = remember {
        Animatable(initialValue = 0f)
    }
    LaunchedEffect(key1 = letter) {
        val currentIndex = animatable.value.toInt()
        val index = AlphabetMapper.getIndexOf(letter)

        val target = if (index < currentIndex) {
            index + AlphabetMapper.size
        } else {
            index
        }

        val result = animatable.animateTo(
            targetValue = target.toFloat(),
            animationSpec = tween(
                durationMillis = (target - currentIndex) * TickerCycleMillis,
                easing = FastOutSlowInEasing
            )
        )

        if (result.endReason == AnimationEndReason.Finished) {
            animatable.snapTo(index.toFloat())
        }
    }

    val fraction = animatable.value - animatable.value.toInt()
    val rotation = -180f * fraction
    val currentLetter = AlphabetMapper.getLetterAt(animatable.value.toInt())
    val nextLetter = AlphabetMapper.getLetterAt(animatable.value.toInt() + 1)

    Box(modifier = modifier) {
        CenteredText(
            letter = nextLetter,
            textColor = textColor,
            backgroundColor = backgroundColor,
            fontSize = fontSize
        )

        Column {
            Box(modifier = Modifier
                .zIndex(1f)
                .graphicsLayer {
                    rotationX = rotation
                    cameraDistance = 6f * density
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
            ) {
                if (fraction <= 0.5f) {
                    TopHalf {
                        CenteredText(
                            letter = currentLetter,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            fontSize = fontSize
                        )
                    }
                } else {
                    BottomHalf(
                        modifier = Modifier.graphicsLayer {
                            rotationX = 180f
                        }
                    ) {
                        CenteredText(
                            letter = nextLetter,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            fontSize = fontSize
                        )
                    }
                }
            }
            BottomHalf {
                CenteredText(
                    letter = currentLetter,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    fontSize = fontSize
                )
            }
        }
    }
}

@Composable
private fun CenteredText(
    letter: Char,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    fontSize: TextUnit = 96.sp
) {
    var ascent by remember { mutableStateOf(0f) }
    var middle by remember { mutableStateOf(0f) }
    var baseline by remember { mutableStateOf(0f) }
    var top by remember { mutableStateOf(0f) }
    var bottom by remember { mutableStateOf(0f) }

    val delta: Float by remember {
        derivedStateOf {
            ((bottom - baseline) - (ascent - top)) / 2f
        }
    }

    Text(
        text = letter.toString(),
        color = textColor,
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                drawLine(
                    textColor,
                    Offset(x = 0f, y = center.y),
                    Offset(x = size.width, y = center.y),
                    strokeWidth = 2f * density
                )
            }
            .offset {
                IntOffset(x = 0, y = delta.roundToInt())
            },
        onTextLayout = { textLayoutResult ->
            val layoutInput = textLayoutResult.layoutInput
            val fontSizePx = with(layoutInput.density) {
                layoutInput.style.fontSize.toPx()
            }
            baseline = textLayoutResult.firstBaseline
            top = textLayoutResult.getLineTop(0)
            bottom = textLayoutResult.getLineBottom(0)
            middle = bottom - top
            ascent = bottom - fontSizePx
        }
    )
}

@Composable
private fun TopHalf(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier.clipToBounds(),
        content = content
    ) { measurables, constraints ->
        require(measurables.size == 1) { "This composable expects a single child" }

        val placeable = measurables.first().measure(constraints)
        val height = placeable.height / 2

        layout(
            width = placeable.width,
            height = height
        ) {
            placeable.placeRelative(x = 0, y = 0)
        }
    }
}

@Composable
private fun BottomHalf(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.clipToBounds(),
        content = content
    ) { measureable, constraints ->
        require(measureable.size == 1) { "This composable expects a single child" }

        val placeable = measureable.first().measure(constraints)
        val height = placeable.height / 2

        layout(
            width = placeable.width,
            height = height
        ) {
            placeable.placeRelative(x = 0, y = -height)
        }
    }
}