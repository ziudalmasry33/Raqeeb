package com.raqeeb.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.domain.model.RestrictedDomain
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald
import com.raqeeb.app.ui.theme.SoftWhite

@Composable
fun WebsitesScreen(
    isArabic: Boolean,
    domains: List<RestrictedDomain>,
    onAddDomain: (String) -> Unit,
    onToggleDomain: (Long) -> Unit,
    onRemoveDomain: (Long) -> Unit
) {
    var inputDomain by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepEmerald),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = AccentGold)
                Text(
                    text = if (isArabic)
                        "حماية الخصوصية مُفعّلة: جميع المواقع الإباحية والمضرة محظورة ومُشفّرة محلياً دون إظهار أسمائها."
                    else
                        "Privacy Shield Active: All adult & harmful sites are blocked and masked locally.",
                    color = SoftWhite,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        OutlinedTextField(
            value = inputDomain,
            onValueChange = { inputDomain = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(if (isArabic) "إضافة موقع إضافي للحظر (يُخفى تلقائياً)" else "Add extra domain to block (auto-masked)")
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onAddDomain(inputDomain)
                        inputDomain = ""
                    }
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add", tint = AccentGold)
                }
            }
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(domains, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.animateContentSize(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (item.isBuiltInShield) Icons.Rounded.VerifiedUser else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = item.maskedLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (isArabic) item.category.labelAr else item.category.labelEn,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = item.isEnabled,
                                onCheckedChange = { onToggleDomain(item.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DeepEmerald,
                                    checkedTrackColor = AccentGold
                                )
                            )
                            if (!item.isBuiltInShield) {
                                IconButton(onClick = { onRemoveDomain(item.id) }) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
