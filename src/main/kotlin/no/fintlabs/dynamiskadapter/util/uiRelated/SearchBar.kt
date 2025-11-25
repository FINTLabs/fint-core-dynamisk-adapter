@file:Suppress("ktlint:standard:no-wildcard-imports")

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun searchBar(
    label: String,
    allItems: List<String>,
    onSelectItem: (String) -> Unit,
) {
    val selectedItem = remember { mutableStateOf("") }
    val searchQuery = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateOf<List<String>>(emptyList()) }
    val recommendations = remember { mutableStateOf<List<String>>(emptyList()) }

    fun onInputChange(
        input: String,
        allItems: List<String>,
    ) {
        searchQuery.value = input
        searchResults.value = allItems.filter { it.contains(input) }
        recommendations.value = searchResults.value.take(3)
    }

    fun selectItem(item: String) {
        searchQuery.value = item
        selectedItem.value = item
        onSelectItem(item)
        recommendations.value = emptyList()
    }

    return (
        Column {
            Text(label)
            TextField(
                value = searchQuery.value,
                onValueChange = { onInputChange(it, allItems) },
            )
            Column {
                for (item in recommendations.value) {
                    Button(
                        onClick = { selectItem(item) },
                    ) {
                        Text(item)
                    }
                }
            }
        }
    )
}
