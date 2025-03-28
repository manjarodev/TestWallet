package com.example.myapplication.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R


@Composable
fun WCTopAppBar(
    titleText: String,
    vararg actionImages: TopBarActionImage,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = titleText, style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight(700)))
        Spacer(modifier = Modifier.weight(1f))
        actionImages.forEachIndexed() { index, actionImage ->
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = actionImage.onClick),
                painter = painterResource(id = actionImage.resource),
                contentDescription = null,
            )
            if (index != actionImages.lastIndex) Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

data class TopBarActionImage(
    @DrawableRes val resource: Int,
    val onClick: () -> Unit,
)

@Preview(showBackground = true)
@Composable
fun WCTopAppBarPreview() {
    MyApplicationTheme {
        WCTopAppBar(titleText = "Title", TopBarActionImage(R.drawable.ic_ethereum) {})
    }
}