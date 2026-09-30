package org.itantra.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Color as AndroidColor

/**
 * Pairing & Link Control Screen matching Stitch 04_link_control:
 * Multi-PHY transports matrix, real-time hardware telemetry bar,
 * tactical secure QR module, and node scanning controls.
 */
@Composable
fun PairingScreen(
    codeText: String,
    secondsRemaining: Long,
    paired: List<PairedUnit>,
    scanning: Boolean,
    onScanToggle: () -> Unit,
    onEnterManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SecureWindow()
    val p = palette

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // TOP SUBHEADER & TELEMETRY BAND
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "[SYS://NET_LAYER/PHY]",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                    Text(
                        text = "CH: 04 // 915 MHZ",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.apricot.core,
                    )
                }
                Text(
                    text = "LINK CONTROL",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = p.ink,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "OFFLINE PHYSICAL LAYER TRANSPORTS",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
            }
        }

        // REALTIME HARDWARE TELEMETRY BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLow)
                .border(Tokens.Hairline, p.hairline)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "PWR:", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
                Text(text = "3.94V [92%]", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.periwinkle.core)
            }
            Text(text = "|", fontSize = 8.sp, color = p.hairline)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "HOPS:", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
                Text(text = "02 MAX", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.ink)
            }
            Text(text = "|", fontSize = 8.sp, color = p.hairline)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "NOISE FL:", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = p.hairlineStrong)
                Text(text = "-104 dBm", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = p.aqua.core)
            }
        }

        // PRIMARY CONNECTED HARDWARE NODE / QR CODE MODULE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(1.dp, p.periwinkle.core)
                .padding(12.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(p.periwinkle.core))
                        Text(
                            text = "SECURE AIRGAP P2P KEY",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = p.periwinkle.core,
                        )
                    }
                    Text(
                        text = "ED25519 // SYMMETRIC",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.hairlineStrong,
                    )
                }

                // QR Code Display
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .border(Tokens.Hairline, p.hairline)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val bitmap = remember(codeText) { qrBitmap(codeText) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Pairing code",
                            modifier = Modifier.size(170.dp),
                        )
                    }
                }

                Text(
                    text = "POINT CAMERA AT PEER UNIT OR SCAN THIS CODE",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = p.ink,
                )
                Text(
                    text = "Key refreshes in ${secondsRemaining / 60}:${"%02d".format(secondsRemaining % 60)}",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.periwinkle.core,
                )
            }
        }

        // PROMINENT SCAN & DISCOVERY TRIGGER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (scanning) p.blush.core else p.periwinkle.core)
                .border(1.dp, if (scanning) p.blush.deep else p.periwinkle.mid)
                .clickable(onClick = onScanToggle)
                .padding(vertical = 12.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Transmit,
                    contentDescription = null,
                    tint = p.surfaceContainerLowest,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (scanning) "STOP CAMERA SCANNING" else "ESTABLISH CONNECTION // SCAN PEERS",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = p.surfaceContainerLowest,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        // MULTI-PHY TRANSPORTS LIST
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "COMMUNICATION TRANSPORTS (MULTI-PHY)",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.muted,
                letterSpacing = 0.5.sp,
            )

            TransportCard(
                title = "1. WI-FI DIRECT (P2P)",
                subtitle = "5.8 GHz // WPA3-SAE // Low Latency",
                status = "ACTIVE PRIMARY",
                statusColor = p.mint.core,
                isActive = true,
            )
            TransportCard(
                title = "2. BLUETOOTH (BLE MESH)",
                subtitle = "BLE 5.2 Extended Range // Paired: ${paired.size} units",
                status = "STANDBY READY",
                statusColor = p.aqua.core,
            )
            TransportCard(
                title = "3. LORA (LONG RANGE RF)",
                subtitle = "868.10 MHz // 22 dBm // Max ~7.2 km",
                status = "FALLBACK READY",
                statusColor = p.apricot.core,
            )
        }

        // DISCOVERED / PAIRED FIELD NODES LIST
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "DISCOVERED FIELD NODES (${paired.size})",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = p.ink,
                    )
                    Text(
                        text = "ACTIVE POLLING",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.aqua.core,
                    )
                }

                if (paired.isEmpty()) {
                    Text(
                        text = "No peers paired yet. Scan QR code or enter manual code.",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = p.muted,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                } else {
                    paired.forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(p.surfaceContainerLow)
                                .border(Tokens.Hairline, p.hairline)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (unit.online) p.mint.core else p.hairlineStrong),
                                )
                                Column {
                                    Text(
                                        text = unit.name,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = p.ink,
                                    )
                                    Text(
                                        text = "NODE ${"%02d".format(unit.nodeId)} // ${unit.lastSeen}",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = p.muted,
                                    )
                                }
                            }
                            Text(
                                text = if (unit.online) "CONNECTED" else "OFFLINE",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (unit.online) p.mint.core else p.muted,
                            )
                        }
                    }
                }
            }
        }

        // MANUAL FALLBACK BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surfaceContainerLow)
                .border(Tokens.Hairline, p.hairline)
                .clickable(onClick = onEnterManually)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "ENTER PAIRING CODE MANUALLY",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = p.ink,
            )
        }
    }
}

@Composable
private fun TransportCard(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    isActive: Boolean = false,
) {
    val p = palette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) p.surfaceContainerHigh else p.surfaceContainerLowest)
            .border(Tokens.Hairline, if (isActive) p.periwinkle.core else p.hairline)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) p.periwinkle.core else p.ink,
                )
                Text(
                    text = subtitle,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = p.muted,
                )
            }
            Text(
                text = status,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
        }
    }
}

data class PairedUnit(
    val name: String,
    val nodeId: Int,
    val lastSeen: String,
    val online: Boolean,
)

val pairingWindowFlags: Int
    get() = android.view.WindowManager.LayoutParams.FLAG_SECURE

private fun qrBitmap(
    text: String,
    size: Int = 512,
): Bitmap? =
    runCatching {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val row = y * size
            for (x in 0 until size) {
                pixels[row + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        Bitmap.createBitmap(pixels, size, size, Bitmap.Config.RGB_565)
    }.getOrNull()
