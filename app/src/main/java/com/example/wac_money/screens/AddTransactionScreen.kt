package com.example.wac_money.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionType
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onTransactionAdded: (Transaction) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember { mutableStateOf("") }

    val categories = listOf(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Health & Fitness",
        "Education",
        "Travel",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { },
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = false,
                onDismissRequest = { }
            ) {
                categories.forEach { categoryOption ->
                    DropdownMenuItem(
                        text = { Text(categoryOption) },
                        onClick = { category = categoryOption }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TransactionType.values().forEach { transactionType ->
                FilterChip(
                    selected = type == transactionType,
                    onClick = { type = transactionType },
                    label = { Text(transactionType.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull()
                if (title.isNotBlank() && amountValue != null && category.isNotBlank()) {
                    val transaction = Transaction(
                        id = 0,
                        title = title,
                        amount = amountValue,
                        category = category,
                        date = Date(),
                        type = type,
                        note = note.takeIf { it.isNotBlank() }
                    )
                    onTransactionAdded(transaction)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Transaction")
        }
    }
}
