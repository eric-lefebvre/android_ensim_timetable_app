package com.example.ensim_app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.ui.theme.Navy900

@Composable
internal fun EventBlock(
    cours: Cours,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color)
            .clickable(onClick = onClick),
    ) {
        val blockHeight = maxHeight
        Column(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = cours.matiere,
                color = Navy900,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (blockHeight > 45.dp && cours.salles.isNotEmpty()) {
                Text(
                    text = cours.salles.joinToString { it.nom },
                    color = Navy900,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (blockHeight > 65.dp && cours.professeurs.isNotEmpty()) {
                Text(
                    text = cours.professeurs.joinToString { it.toString() },
                    color = Navy900,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
