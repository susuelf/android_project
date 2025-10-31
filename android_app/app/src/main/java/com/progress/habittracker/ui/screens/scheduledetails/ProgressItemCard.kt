package com.progress.habittracker.ui.screens.scheduledetails

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.progress.habittracker.data.model.ProgressResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Progress History Item Card komponens
 * 
 * Megjeleníti egy progress rekord részleteit:
 * - Dátum
 * - Logged time (ha van)
 * - Notes (ha van)
 * - Completed státusz ikon
 */
@Composable
fun ProgressItemCard(
    progress: ProgressResponseDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isCompleted == true) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bal oldal: Dátum, logged time, notes
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Dátum
                Text(
                    text = formatDate(progress.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Logged time
                if (progress.loggedTime != null && progress.loggedTime > 0) {
                    Text(
                        text = "Idő: ${progress.loggedTime} perc",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Notes
                if (!progress.notes.isNullOrBlank()) {
                    Text(
                        text = progress.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Jobb oldal: Completed státusz ikon
            Icon(
                imageVector = if (progress.isCompleted == true) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Outlined.Circle
                },
                contentDescription = if (progress.isCompleted == true) {
                    "Befejezve"
                } else {
                    "Nem befejezve"
                },
                tint = if (progress.isCompleted == true) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Dátum formázó segédfüggvény
 * @param dateString Dátum string formátum: YYYY-MM-DD
 * @return Formázott dátum (pl. "2025. jan. 15.")
 */
private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy. MMM. dd.")
        date.format(formatter)
    } catch (_: Exception) {
        dateString
    }
}

// ===== PREVIEW =====

@Preview(showBackground = true)
@Composable
private fun ProgressItemCardPreview_Completed() {
    MaterialTheme {
        ProgressItemCard(
            progress = ProgressResponseDto(
                id = 1,
                scheduleId = 1,
                date = "2025-01-15",
                loggedTime = 45,
                notes = "Jó edzés volt, sok erőt használtam",
                isCompleted = true,
                createdAt = "2025-01-15T10:30:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressItemCardPreview_NotCompleted() {
    MaterialTheme {
        ProgressItemCard(
            progress = ProgressResponseDto(
                id = 2,
                scheduleId = 1,
                date = "2025-01-14",
                loggedTime = null,
                notes = null,
                isCompleted = false,
                createdAt = "2025-01-14T10:30:00Z"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
