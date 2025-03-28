package com.example.myapplication.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun BlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = Color(0xFFE5E7E7)
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3496ff),
            contentColor = contentColor
        ),
    ) {
        Text(text = text, color = contentColor)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ButtonWithLoader(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
) {
    val contentColor = Color(0xFFE5E7E7)
    AnimatedContent(targetState = isLoading, label = "Loading") { state ->
        if (state) {
            CircularProgressIndicator(
                modifier = modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .wrapContentHeight(Alignment.CenterVertically),
                color = contentColor,
                strokeWidth = 4.dp
            )
        } else {
            Button(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3496ff),
                    contentColor = contentColor
                )
            ) {
                Text(
                    text = text,
                    color = contentColor,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }
    }
}