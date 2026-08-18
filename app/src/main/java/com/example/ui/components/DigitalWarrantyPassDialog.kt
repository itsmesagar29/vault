package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.exporter.ExportHelper
import com.example.domain.model.BrandSupportDirectory
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon

@Composable
fun DigitalWarrantyPassDialog(
    receipt: ReceiptItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val brandSupport = BrandSupportDirectory.findSupportFor(receipt.merchantName, receipt.itemName)
    val scrollState = rememberScrollState()

    val certificateNumber = "CERT-VAULT-${receipt.id.toString().padStart(5, '0')}"
    val claimRef = "CLM-2026-${(receipt.id * 891 + 1042) % 9000 + 1000}"

    val statusColor = when (receipt.warrantyStatus) {
        WarrantyStatus.ACTIVE -> StatusActive
        WarrantyStatus.EXPIRING_SOON -> StatusExpiringSoon
        WarrantyStatus.EXPIRED -> StatusExpired
        WarrantyStatus.NO_WARRANTY -> Color.Gray
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(BrandPrimary, BrandAccent, Color(0xFF6366F1))
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            color = DarkBackground,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = BrandAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DIGITAL WARRANTY PASS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = certificateNumber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = BrandAccent
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Certificate Pass Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = receipt.merchantName.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = receipt.itemName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            // Active / Expired Stamp Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = if (receipt.warrantyStatus == WarrantyStatus.ACTIVE || receipt.warrantyStatus == WarrantyStatus.EXPIRING_SOON) "PROTECTED" else "EXPIRED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Value Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "INVOICE AMOUNT", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(text = receipt.formattedAmount, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "PURCHASED", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(text = receipt.formattedPurchaseDate, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "COVERAGE DURATION", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(text = "${receipt.warrantyMonths} Months", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "EXPIRES ON", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = receipt.formattedExpiryDate,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Barcode Canvas Simulation
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                val barWidth = size.width / 60
                                for (i in 0..58) {
                                    val isBar = ((i * 13 + receipt.id.toInt() * 7) % 3) != 0
                                    if (isBar) {
                                        drawLine(
                                            color = Color.Black,
                                            start = Offset(i * barWidth + barWidth / 2, 0f),
                                            end = Offset(i * barWidth + barWidth / 2, size.height),
                                            strokeWidth = if (i % 4 == 0) barWidth * 0.8f else barWidth * 0.45f
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$certificateNumber • $claimRef",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Official Brand Support Hub
                if (brandSupport != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = BrandAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Official ${brandSupport.brandName} Support & Care",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = brandSupport.standardWarrantyCoverageNote,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!brandSupport.tollFreeNumber.isNullOrEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${brandSupport.tollFreeNumber.replace(" ", "")}"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call Helpline", fontSize = 11.sp)
                                    }
                                }

                                if (!brandSupport.claimWebsiteUrl.isNullOrEmpty()) {
                                    OutlinedButton(
                                        onClick = {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(brandSupport.claimWebsiteUrl))
                                            context.startActivity(webIntent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Claim Portal", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Send Claim Email & Share Pass
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val (subject, body) = ExportHelper.generateWarrantyClaimEmail(receipt, brandSupport?.supportEmail)
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                if (!brandSupport?.supportEmail.isNullOrEmpty()) {
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(brandSupport?.supportEmail))
                                }
                                putExtra(Intent.EXTRA_SUBJECT, subject)
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                ExportHelper.shareText(context, "$subject\n\n$body", "Warranty Claim Email")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("claim_email_action_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft Claim", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val passText = """
                                🛡️ DIGITAL WARRANTY CERTIFICATE:
                                Ref ID: $certificateNumber
                                Claim Ref: $claimRef
                                
                                Merchant: ${receipt.merchantName}
                                Product: ${receipt.itemName}
                                Price: ${receipt.formattedAmount}
                                Purchase Date: ${receipt.formattedPurchaseDate}
                                Coverage: ${receipt.warrantyMonths} Months
                                Status: ${receipt.warrantyStatus.name}
                                Expiry Date: ${receipt.formattedExpiryDate}
                                
                                Notes: ${receipt.notes.ifBlank { "N/A" }}
                                Verified with BillVault On-Device Security.
                            """.trimIndent()
                            ExportHelper.shareText(context, passText, "Share Digital Warranty Pass")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_pass_action_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Pass", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Copy Certificate ID
                OutlinedButton(
                    onClick = {
                        val passDetails = "Ref: $certificateNumber | Merchant: ${receipt.merchantName} | Item: ${receipt.itemName} | Expires: ${receipt.formattedExpiryDate}"
                        clipboardManager.setText(AnnotatedString(passDetails))
                        Toast.makeText(context, "Copied certificate reference to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_certificate_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Certificate Reference", fontSize = 12.sp)
                }
            }
        }
    }
}
