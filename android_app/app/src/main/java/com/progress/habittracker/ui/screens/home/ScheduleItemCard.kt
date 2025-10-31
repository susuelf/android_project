package com.progress.habittracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.progress.habittracker.data.model.*
import com.progress.habittracker.ui.theme.Progr3SSTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Schedule Item Card
 * 
 * Egy schedule megjelenítése kártya formátumban a Home Screen-en.
 * 
 * Funkciók:
 * - Habit név és kategória megjelenítése
 * - Időpont kijelzése (start time, duration)
 * - Státusz jelzés (checkbox icon)
 * - Státusz színezés (Completed = zöld, Planned = szürke, Skipped = piros)
 * - Kattintható -> Schedule Details Screen
 * - Checkbox toggle -> státusz váltás
 * 
 * @param schedule A megjelenítendő schedule adatai
 * @param onScheduleClick Kártya kattintás callback (navigáció Details-re)
 * @param onStatusToggle Checkbox kattintás callback (státusz váltás)
 * @param modifier Opcionális Modifier
 */
@Composable
fun ScheduleItemCard(
    schedule: ScheduleResponseDto,
    onScheduleClick: (Int) -> Unit,
    onStatusToggle: (Int, ScheduleStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    // Státusz alapú színezés
    val containerColor = when (schedule.status) {
        ScheduleStatus.Completed -> MaterialTheme.colorScheme.primaryContainer
        ScheduleStatus.Skipped -> MaterialTheme.colorScheme.errorContainer
        ScheduleStatus.Planned -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (schedule.status) {
        ScheduleStatus.Completed -> MaterialTheme.colorScheme.onPrimaryContainer
        ScheduleStatus.Skipped -> MaterialTheme.colorScheme.onErrorContainer
        ScheduleStatus.Planned -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onScheduleClick(schedule.id) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bal oldal: Időpont oszlop
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Start time
                Text(
                    text = formatTime(schedule.startTime),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Duration (ha van)
                schedule.durationMinutes?.let { duration ->
                    Text(
                        text = "${duration}p",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Középső rész: Habit információk
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Habit név
                Text(
                    text = schedule.habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Kategória név
                Text(
                    text = schedule.habit.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
                
                // Goal (ha van)
                schedule.habit.goal?.let { goal ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Jobb oldal: Státusz checkbox
            IconButton(
                onClick = { onStatusToggle(schedule.id, schedule.status) }
            ) {
                Icon(
                    imageVector = when (schedule.status) {
                        ScheduleStatus.Completed -> Icons.Filled.CheckCircle
                        ScheduleStatus.Planned, ScheduleStatus.Skipped -> Icons.Outlined.Circle
                    },
                    contentDescription = when (schedule.status) {
                        ScheduleStatus.Completed -> "Befejezett"
                        ScheduleStatus.Planned -> "Tervezett"
                        ScheduleStatus.Skipped -> "Kihagyott"
                    },
                    tint = when (schedule.status) {
                        ScheduleStatus.Completed -> MaterialTheme.colorScheme.primary
                        ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error
                        ScheduleStatus.Planned -> contentColor
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Időpont formázó helper függvény
 * 
 * Átalakítja a backend ISO 8601 vagy HH:mm formátumot
 * ember-olvasható formátumra (HH:mm)
 * 
 * @param timeString Backend időpont string
 * @return Formázott időpont (pl. "08:30")
 */
private fun formatTime(timeString: String): String {
    return try {
        // Ha ISO 8601 formátum (pl. "2025-10-31T08:30:00")
        if (timeString.contains('T')) {
            val time = LocalTime.parse(timeString.substringAfter('T').substringBefore('Z'))
            time.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            // Ha már HH:mm formátum
            timeString.substringBefore(':').padStart(2, '0') + ":" + 
            timeString.substringAfter(':').take(2).padStart(2, '0')
        }
    } catch (e: Exception) {
        timeString // Fallback: eredeti string
    }
}

/**
 * Preview - Tervezett schedule
 */
@Preview(showBackground = true)
@Composable
fun ScheduleItemCardPreview_Planned() {
    Progr3SSTheme {
        ScheduleItemCard(
            schedule = ScheduleResponseDto(
                id = 1,
                habitId = 1,
                habit = HabitResponseDto(
                    id = 1,
                    name = "Reggeli futás",
                    description = "2km futás a parkban",
                    goal = "10 alkalom 2 hét alatt",
                    category = HabitCategoryResponseDto(
                        id = 1,
                        name = "Sport",
                        iconUrl = null
                    ),
                    userId = 1,
                    createdAt = "2025-10-31T08:00:00Z"
                ),
                userId = 1,
                date = "2025-10-31",
                startTime = "08:30",
                endTime = null,
                durationMinutes = 30,
                status = ScheduleStatus.Planned,
                isCustom = false,
                notes = null,
                participants = null,
                progress = null,
                createdAt = "2025-10-31T08:00:00Z"
            ),
            onScheduleClick = {},
            onStatusToggle = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview - Befejezett schedule
 */
@Preview(showBackground = true)
@Composable
fun ScheduleItemCardPreview_Completed() {
    Progr3SSTheme {
        ScheduleItemCard(
            schedule = ScheduleResponseDto(
                id = 2,
                habitId = 2,
                habit = HabitResponseDto(
                    id = 2,
                    name = "Meditáció",
                    description = "10 perces meditáció",
                    goal = "Minden nap meditálni",
                    category = HabitCategoryResponseDto(
                        id = 2,
                        name = "Wellness",
                        iconUrl = null
                    ),
                    userId = 1,
                    createdAt = "2025-10-31T06:00:00Z"
                ),
                userId = 1,
                date = "2025-10-31",
                startTime = "07:00",
                endTime = "07:10",
                durationMinutes = 10,
                status = ScheduleStatus.Completed,
                isCustom = false,
                notes = null,
                participants = null,
                progress = null,
                createdAt = "2025-10-31T06:00:00Z"
            ),
            onScheduleClick = {},
            onStatusToggle = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}
