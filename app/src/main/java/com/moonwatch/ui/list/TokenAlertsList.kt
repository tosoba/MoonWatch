package com.moonwatch.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.annotation.ExperimentalCoilApi
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moonwatch.MainViewModel
import com.moonwatch.core.android.ext.toEpochMillisDefault
import com.moonwatch.model.TokenAlertWithValues
import com.moonwatch.ui.TokenIcon
import com.moonwatch.ui.dialog.DeleteItemDialog
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Composable
@OptIn(
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
fun TokenAlertsList(
    onItemClick: (TokenAlertWithValues) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  var tokenAlertBeingDeleted by rememberSaveable { mutableStateOf<TokenAlertWithValues?>(null) }
  tokenAlertBeingDeleted?.let { tokenAlertWithValue ->
    DeleteItemDialog(
        itemName = "${tokenAlertWithValue.token.name} alert",
        dismiss = { tokenAlertBeingDeleted = null },
        delete = { viewModel.deleteAlert(tokenAlertWithValue.alert.id) },
    )
  }

  val alerts = viewModel.alertsFlow.collectAsLazyPagingItems()
  if (alerts.itemCount == 0) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(
          text = "No saved alerts.",
          textAlign = TextAlign.Center,
          style = Typography.h6.copy(fontWeight = FontWeight.Bold),
      )
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(alerts) { tokenAlertWithValue ->
        if (tokenAlertWithValue == null) return@items
        TokenAlertWithValueListItem(
            tokenAlertWithValue = tokenAlertWithValue,
            onItemClick = onItemClick,
            onDeleteClick = { tokenAlertBeingDeleted = it },
        )
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
    tokenAlertWithValue: TokenAlertWithValues,
    onItemClick: (TokenAlertWithValues) -> Unit,
    onDeleteClick: (TokenAlertWithValues) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val (token, alert, value) = tokenAlertWithValue
  ListItem(
      icon = { TokenIcon(token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(text = "$", style = Typography.subtitle2)
          Text(
              text = value.usd.toPlainString(),
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
              checked = alert.active,
              onCheckedChange = { viewModel.toggleAlertActive(alert.id) },
          )
        }
      },
      overlineText = {
        Text(
            text =
                if (alert.lastFiredAt != null) {
                  "Last fired ${TimeAgo.using(alert.lastFiredAt.toEpochMillisDefault)}"
                } else {
                  "Created ${TimeAgo.using(alert.createdAt.toEpochMillisDefault)}"
                },
            modifier = Modifier.fillMaxWidth(),
        )
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
