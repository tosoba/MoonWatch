package com.moonwatch.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.annotation.ExperimentalCoilApi
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.accompanist.pager.ExperimentalPagerApi
import com.moonwatch.MainViewModel
import com.moonwatch.R
import com.moonwatch.core.android.ext.toEpochMillisDefault
import com.moonwatch.core.model.LoadingInProgress
import com.moonwatch.core.model.WithValue
import com.moonwatch.model.TokenWithValue
import com.moonwatch.ui.TokenIcon
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

@Composable
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalPagerApi::class,
    FlowPreview::class,
)
fun TokensWithValueList(
    onItemClick: (TokenWithValue) -> Unit,
    onTrailingClick: (TokenWithValue) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
  val tokensFlow = remember {
    viewModel
        .tokensFlow
        .filterIsInstance<WithValue<PagingData<TokenWithValue>>>()
        .map(WithValue<PagingData<TokenWithValue>>::value::get)
  }
  val tokens = tokensFlow.collectAsLazyPagingItems()

  val tokensLoadingFlow = remember {
    viewModel.tokensFlow.map {
      val isLoading = it is LoadingInProgress
      if (!isLoading) {
        var delaysCount = 0
        while (tokens.itemCount == 0 && delaysCount++ < 10) delay(100L)
      }
      isLoading
    }
  }
  val tokensLoadingState = tokensLoadingFlow.collectAsState(initial = tokens.itemCount == 0)

  if (tokensLoadingState.value) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator()
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(tokens) { tokenWithValue ->
        if (tokenWithValue == null) return@items
        TokenWithValueListItem(
            tokenWithValue = tokenWithValue,
            onItemClick = onItemClick,
            onTrailingClick = onTrailingClick,
        )
      }

      if (tokens.itemCount == 0) {
        item {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.fillParentMaxSize()) {
            Text(
                text = "No saved tokens.",
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
@OptIn(
    ExperimentalCoilApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalMaterialApi::class,
    FlowPreview::class,
)
private fun TokenWithValueListItem(
    tokenWithValue: TokenWithValue,
    onItemClick: (TokenWithValue) -> Unit,
    onTrailingClick: (TokenWithValue) -> Unit
) {
  ListItem(
      icon = { TokenIcon(tokenWithValue.token) },
      secondaryText = {
        Row(horizontalArrangement = Arrangement.Start) {
          Text(text = "$", style = Typography.subtitle2)
          Text(
              text = tokenWithValue.value.usd.toPlainString(),
              style = Typography.subtitle2,
              maxLines = 1,
              modifier = Modifier.weight(1f),
          )
        }
      },
      overlineText = {
        Text(
            text =
                "Last updated ${TimeAgo.using(tokenWithValue.value.updatedAt.toEpochMillisDefault)}",
            modifier = Modifier.fillMaxWidth(),
        )
      },
      modifier = Modifier.fillMaxWidth().clickable { onItemClick(tokenWithValue) },
      trailing = {
        IconButton(onClick = { onTrailingClick(tokenWithValue) }) {
          Icon(
              painterResource(R.drawable.ic_baseline_show_chart_24),
              contentDescription = "",
          )
        }
      },
  ) {
    Text(
        text = tokenWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth(),
    )
  }
}
