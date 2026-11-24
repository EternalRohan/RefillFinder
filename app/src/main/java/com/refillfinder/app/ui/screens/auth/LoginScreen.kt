package com.refillfinder.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refillfinder.app.ui.theme.*
import com.refillfinder.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var rollId by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Floating drop animation
    val infiniteTransition = rememberInfiniteTransition(label = "login_anim")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundWhite)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header gradient banner ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF29B6F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Animated water drop logo
                    Box(
                        modifier = Modifier
                            .offset(y = floatY.dp)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 30.sp)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "ReFillFinder",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // ── White card form ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-24).dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(SurfaceWhite, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    // Mode toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundWhite)
                            .padding(4.dp)
                    ) {
                        listOf("Sign In", "Register").forEachIndexed { idx, label ->
                            val selected = (idx == 1) == isRegisterMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected)
                                            Brush.linearGradient(listOf(PrimaryBlue, PrimaryBlueMid))
                                        else
                                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    )
                                    .clickable { isRegisterMode = idx == 1 }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) Color.White else OnSurfaceMuted,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Name field (register only)
                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            LoginField(
                                value = name, onValueChange = { name = it },
                                label = "Full Name", icon = Icons.Default.Person,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))
                            LoginField(
                                value = rollId, onValueChange = { rollId = it },
                                label = "University Roll / Registration No.",
                                icon = Icons.Default.Badge,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    LoginField(
                        value = email, onValueChange = { email = it },
                        label = "Email Address",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )

                    Spacer(Modifier.height(12.dp))

                    LoginField(
                        value = password, onValueChange = { password = it },
                        label = "Campus Portal Password / PIN",
                        icon = Icons.Default.Lock,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        imeAction = ImeAction.Done,
                        onNext = { focusManager.clearFocus() },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = OnSurfaceMuted
                                )
                            }
                        }
                    )

                    // Error
                    AnimatedVisibility(visible = authState.error != null) {
                        authState.error?.let {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedLight)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, null,
                                    tint = RedOffline, modifier = Modifier.size(16.dp))
                                Text(it, color = RedOffline, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Sign In button ──────────────────────────────
                    Button(
                        onClick = {
                            if (isRegisterMode) viewModel.register(name, email, password)
                            else viewModel.login(email, password)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !authState.isLoading
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White, strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Sign in to Campus Network →",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
                        Text("  OR CAMPUS QUICK CONNECT  ",
                            color = OnSurfaceSubtle, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Guest button
                    OutlinedButton(
                        onClick = onLoginSuccess,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🗺️", fontSize = 18.sp)
                            Text("Browse as Guest",
                                color = OnSurface, fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onNext: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = PrimaryBlueMid, modifier = Modifier.size(20.dp)) },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onNext() }, onDone = { onNext() }
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderLight,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = OnSurfaceMuted,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface,
            cursorColor = PrimaryBlue,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite
        )
    )
}
