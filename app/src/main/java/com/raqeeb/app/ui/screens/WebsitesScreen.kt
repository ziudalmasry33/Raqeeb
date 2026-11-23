package com.raqeeb.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raqeeb.app.domain.model.DomainCategory
import com.raqeeb.app.domain.model.RestrictedDomain
import com.raqeeb.app.ui.theme.AccentGold
import com.raqeeb.app.ui.theme.DeepEmerald

@Composable
fun WebsitesScreen(
    isArabic: Boolean,
    domains: List<RestrictedDomain>,
    onAddDomain: (String, DomainCategory) -> Unit,
    onToggleDomain: (Long) -> Unit,
    onRemoveDomain: (Long) -> Unit
) {
    var inputDomain by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DomainCategory.CUSTOM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isArabic) "إدارة المواقع المقيدة" else "Website Management",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = inputDomain,
            onValueChange = { inputDomain = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(if (isArabic) "أدخل نطاق الموقع (مثال: example.com)" else "Enter domain (e.g. example.com)")
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onAddDomain(inputDomain, selectedCategory)
                        inputDomain = ""
                    }
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add", tint = AccentGold)
                }
            }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DomainCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = if (isArabic) cat.labelAr else cat.labelEn,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(domains, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.domain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = if (isArabic) item.category.labelAr else item.category.labelEn,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
