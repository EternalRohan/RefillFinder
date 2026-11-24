package com.refillfinder.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.ReportType
import com.refillfinder.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    cooler: Cooler,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ReportType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf<ReportType?>(null) }
    var customMessage by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderDefault)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Report Issue",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = "Block ${cooler.block} • Floor ${cooler.floor}",
                color = OnSurfaceMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "What's the issue?",
                color = OnSurfaceMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Report type chips
            ReportType.entries.forEach { type ->
                val selected = selectedType == type
                Surface(
                    onClick = { selectedType = type },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) PrimaryBlue.copy(alpha = 0.15f) else SurfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (selected) PrimaryBlue else BorderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(type.emoji, fontSize = 18.sp)
                        Text(
                            text = type.displayName,
                            color = if (selected) PrimaryBlue else OnSurface,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom message
            OutlinedTextField(
                value = customMessage,
                onValueChange = { customMessage = it },
                placeholder = { Text("Add a note (optional)...", color = OnSurfaceSubtle) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    cursorColor = PrimaryBlue,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    selectedType?.let { type ->
                        onSubmit(type, customMessage)
                    }
                },
                enabled = selectedType != null && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Submit Report", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
