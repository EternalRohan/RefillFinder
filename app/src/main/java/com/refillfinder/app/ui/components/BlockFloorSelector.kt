package com.refillfinder.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.utils.Constants

@Composable
fun BlockFloorSelector(
    currentBlock: Int,
    currentFloor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedBlock by remember { mutableStateOf(currentBlock) }
    var selectedFloor by remember { mutableStateOf(currentFloor) }
    var step by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (step == 0) "Select Block" else "Select Floor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurface
                )
                Text(
                    text = if (step == 0) "Block 1–60" else "Floor 1–9",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (step == 0) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items((1..60).toList()) { block ->
                            val selected = block == selectedBlock
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) PrimaryBlue else SurfaceVariant)
                                    .clickable { selectedBlock = block },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$block",
                                    color = if (selected) Color.White else OnSurfaceMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items((1..9).toList()) { floor ->
                            val selected = floor == selectedFloor
                            val hasCooler = floor in Constants.COOLER_FLOORS
                            Surface(
                                onClick = { selectedFloor = floor },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) PrimaryBlue else SurfaceVariant,
                                border = BorderStroke(1.dp, if (selected) PrimaryBlueLight else BorderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Floor $floor",
                                        color = if (selected) Color.White else OnSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (hasCooler) {
                                        Text("💧", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (step == 0) onDismiss() else step = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderDefault)
                    ) {
                        Text(if (step == 0) "Cancel" else "Back", color = OnSurfaceMuted)
                    }
                    Button(
                        onClick = {
                            if (step == 0) step = 1
                            else onConfirm(selectedBlock, selectedFloor)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(if (step == 0) "Next" else "Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}
