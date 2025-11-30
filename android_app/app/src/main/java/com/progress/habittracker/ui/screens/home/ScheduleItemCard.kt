package com.progress.habittracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.progress.habittracker.data.model.*
import com.progress.habittracker.ui.theme.DarkSurface
import com.progress.habittracker.ui.theme.HabitBlue
import com.progress.habittracker.ui.theme.Progr3SSTheme
import com.progress.habittracker.ui.theme.SuccessCyan
import com.progress.habittracker.ui.theme.TextPrimary
import com.progress.habittracker.ui.theme.TextSecondary
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Schedule Item Card (Design frissítve - Dark Theme)
 * 
 * Egy schedule megjelenítése kártya formátumban a Home Screen-en.
 * 
 * Funkciók:
 * - Habit név és időpont megjelenítése
 * - Dark surface háttér (#2A2A3E)
 * - Habit ikon színes körben (bal oldal)
 * - Időpont és habit név (középen)
 * - Státusz checkbox (jobb oldal) - zöld pipa ha kész, üres kör ha nem
 * - Kattintható -> Schedule Details Screen
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
    // UI állapot számítása a központosított kalkulátorral
    val uiState = com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule)
    
    // Automatikus Completed státusz ha a kalkulátor szerint kész
    val effectiveStatus = if (uiState.isChecked) ScheduleStatus.Completed else ScheduleStatus.Planned
    
    // Interakció tiltása ha a kalkulátor szerint disabled
    val isDisabled = !uiState.isEnabled
    
    // Habit ikon szín (alapból HabitBlue, kategória szerint variálható)
    val habitIconColor = HabitBlue // Később kategória alapján lehet testreszabni
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onScheduleClick(schedule.id) },
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bal oldal: Habit ikon színes körben
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(habitIconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // Habit első betűje vagy emoji (később lehet kategória ikon)
                Text(
                    text = schedule.habit.name.firstOrNull()?.uppercase() ?: "H",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = habitIconColor,
                    fontSize = 20.sp
                )
            }
            
            // Középső rész: Habit név és időpont
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Habit név
                Text(
                    text = schedule.habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Időpont
                Text(
                    text = formatTime(schedule.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            
            // Jobb oldal: Státusz checkbox
            IconButton(
                onClick = { onStatusToggle(schedule.id, schedule.status) },
                enabled = !isDisabled, // Csak ha progress NEM 100%, akkor enabled
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = when (effectiveStatus) {
                        ScheduleStatus.Completed -> Icons.Filled.CheckCircle
                        ScheduleStatus.Planned, ScheduleStatus.Skipped -> Icons.Outlined.Circle
                    },
                    contentDescription = when (effectiveStatus) {
                        ScheduleStatus.Completed -> androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_completed)
                        ScheduleStatus.Planned -> androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_not_completed)
                        ScheduleStatus.Skipped -> androidx.compose.ui.res.stringResource(com.progress.habittracker.R.string.status_skipped)
                    },
                    tint = when {
                        effectiveStatus == ScheduleStatus.Completed -> SuccessCyan
                        isDisabled -> SuccessCyan.copy(alpha = 0.5f)
                        effectiveStatus == ScheduleStatus.Skipped -> MaterialTheme.colorScheme.error
                        else -> TextSecondary
                    },
                    modifier = Modifier.size(28.dp)
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
