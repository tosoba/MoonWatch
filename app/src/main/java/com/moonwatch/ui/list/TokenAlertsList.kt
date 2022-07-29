package com.moonwatch.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.moonwatch.MainViewModel
import com.moonwatch.core.android.ext.toEpochMillisDefault
import com.moonwatch.core.model.LoadingInProgress
import com.moonwatch.core.model.WithValue
import com.moonwatch.model.TokenAlertWithValues
import com.moonwatch.ui.TokenIcon
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

@Composable
fun TokenAlertsList(
    onItemClick: (TokenAlertWithValues) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val alertsFlow = remember {
    viewModel.alertsFlow
        .filterIsInstance<WithValue<PagingData<TokenAlertWithValues>>>()
        .map(WithValue<PagingData<TokenAlertWithValues>>::value::get)
  }
  val alerts = alertsFlow.collectAsLazyPagingItems()

  val alertsLoadingFlow = remember {
    viewModel.alertsFlow.map {
      val isLoading = it is LoadingInProgress
      if (!isLoading) {
        var delaysCount = 0
        while (alerts.itemCount == 0 && delaysCount++ < 10) delay(100L)
      }
      isLoading
    }
  }
  val alertsLoadingState = alertsLoadingFlow.collectAsState(initial = alerts.itemCount == 0)

  if (alertsLoadingState.value) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator()
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(alerts) { tokenAlertWithValue ->
        if (tokenAlertWithValue == null) return@items
        TokenAlertWithValueListItem(
            tokenAlertWithValue = tokenAlertWithValue,
            onItemClick = onItemClick,
        )
      }

      if (alerts.itemCount == 0) {
        item {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillParentMaxSize()) {
            Text(
                text = "No created alerts.",
                textAlign = TextAlign.Center,
                style = Typography.h6.copy(fontWeight = FontWeight.Bold),
            )
          }
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun TokenAlertWithValueListItem(
    tokenAlertWithValue: TokenAlertWithValues,
    onItemClick: (TokenAlertWithValues) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val (token, alert, creationValue, currentValue) = tokenAlertWithValue
  val creationValueX = currentValue.usd / creationValue.usd
  ListItem(
      icon = { TokenIcon(token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(text = "$", style = Typography.subtitle2)
          Text(
              text = currentValue.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
        }
      },
      trailing = {
        Row(horizontalArrangement = Arrangement.End) {
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
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
          text = tokenAlertWithValue.token.name,
          style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
      )
      Text(
          text = "${String.format("%.02f", creationValueX)}X",
          color = if (creationValueX >= BigDecimal.ONE) Color.Green else Color.Red,
      )
    }
  }
}
