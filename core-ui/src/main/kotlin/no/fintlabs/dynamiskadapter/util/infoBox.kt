package no.fintlabs.dynamiskadapter.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


enum class BoxType {
    ERROR,
    INFO,
}

@Composable
fun infoBox(
    type: BoxType?,
    info: List<String>,
) {
    val outline: Color = if (type == BoxType.INFO) Color.Gray else Color.Red
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(2.dp, outline)
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("$type: ", style = MaterialTheme.typography.h6)
            for (line in info) {
                Text(line)
            }
        }
    }
}