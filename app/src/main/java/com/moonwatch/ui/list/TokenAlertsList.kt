package com.moonwatch.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moonwatch.MainViewModel
import com.moonwatch.model.TokenAlertWithValue
import com.moonwatch.ui.TokenIcon
import com.moonwatch.ui.dialog.DeleteItemDialog
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

// TODO: alerts list is not filling full width for whatever reason...

@Composable
@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
fun TokenAlertsList(viewModel: MainViewModel = hiltViewModel()) {
  var tokenAlertBeingDeleted by rememberSaveable { mutableStateOf<TokenAlertWithValue?>(null) }
  tokenAlertBeingDeleted?.let { tokenAlertWithValue ->
    DeleteItemDialog(
        itemName = "${tokenAlertWithValue.token.name} alert",
        dismiss = { tokenAlertBeingDeleted = null },
        delete = { viewModel.deleteAlert(tokenAlertWithValue.alert.id) },
    )
  }

  val alerts = viewModel.alertsFlow.collectAsState(initial = emptyList())
  if (alerts.value.isEmpty()) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(text = "No saved alerts.", textAlign = TextAlign.Center)
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(alerts.value) {
        ListItem {
          TokenAlertWithValueListItem(
              tokenAlertWithValue = it,
              onItemClick = {},
              onDeleteClick = { tokenAlertBeingDeleted = it },
          )
        }
      }
    }
  }
}

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    FlowPreview::class,
)
private fun TokenAlertWithValueListItem(
    tokenAlertWithValue: TokenAlertWithValue,
    onItemClick: (TokenAlertWithValue) -> Unit,
    onDeleteClick: (TokenAlertWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  ListItem(
      icon = { TokenIcon(tokenAlertWithValue.token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(text = "$", style = Typography.subtitle2)
          Text(
              text = tokenAlertWithValue.value.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
        }
      },
      trailing = {
        Row(horizontalArrangement = Arrangement.End) {
          IconButton(onClick = { onDeleteClick(tokenAlertWithValue) }) {
            Icon(Icons.Outlined.Delete, "")
          }
          Switch(
              checked = tokenAlertWithValue.alert.active,
              onCheckedChange = { viewModel.toggleAlertActive(tokenAlertWithValue.alert.id) },
          )
        }
      },
      modifier = Modifier.fillMaxWidth().clickable { onItemClick(tokenAlertWithValue) },
  ) {
    Text(
        text = tokenAlertWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth(),
    )
  }
}
