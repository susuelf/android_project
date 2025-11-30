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
 * Progress History Item Card - Előrehaladás kártya
 *
 * Ez a komponens egy adott előrehaladási bejegyzés (progress record) részleteit jeleníti meg.
 * Általában egy lista elemeként használatos a Schedule Details képernyőn.
 *
 * Megjelenített adatok:
 * - Dátum (formázva).
 * - Rögzített időtartam (ha van).
 * - Megjegyzések (ha vannak).
 * - Befejezettségi státusz (ikonnal jelezve).
 *
 * @param progress A megjelenítendő előrehaladási adatobjektum.
 * @param modifier Opcionális módosító a kártya elhelyezéséhez.
 */
@Composable
fun ProgressItemCard(
    progress: ProgressResponseDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Ha befejezett, akkor kiemelt színű, egyébként alapértelmezett felületszín
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
            // Bal oldal: Információk (Dátum, Idő, Jegyzet)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Dátum megjelenítése
                Text(
                    text = formatDate(progress.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rögzített időtartam megjelenítése (ha van)
                if (progress.loggedTime != null && progress.loggedTime > 0) {
                    Text(
                        text = "Idő: ${progress.loggedTime} perc",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Megjegyzés megjelenítése (ha van)
                if (!progress.notes.isNullOrBlank()) {
                    Text(
                        text = progress.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2 // Hosszú szöveg esetén levágjuk
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Jobb oldal: Státusz ikon (Pipa vagy Üres kör)
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
 * Dátum formázó segédfüggvény.
 *
 * Átalakítja a "YYYY-MM-DD" formátumú dátumot "YYYY. MMM. dd." formátumra.
 *
 * @param dateString A dátum string formátumban.
 * @return Formázott dátum string (pl. "2025. jan. 15.").
 */
private fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy. MMM. dd.")
        date.format(formatter)
    } catch (_: Exception) {
        dateString // Hiba esetén visszaadjuk az eredeti stringet
    }
}

// ===== ELŐNÉZETEK (PREVIEWS) =====

/**
 * Előnézet - Befejezett állapot.
 */
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

/**
 * Előnézet - Nem befejezett állapot.
 */
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
