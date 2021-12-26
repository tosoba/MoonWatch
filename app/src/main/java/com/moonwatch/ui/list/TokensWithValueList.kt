package com.moonwatch.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.moonwatch.model.Token
import com.moonwatch.model.TokenWithValue
import com.moonwatch.ui.TokenIcon
import com.moonwatch.ui.dialog.DeleteItemDialog
import com.moonwatch.ui.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.threeten.bp.ZoneId

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
    viewModel: MainViewModel = hiltViewModel()
) {
  val tokenBeingDeleted = rememberSaveable { mutableStateOf<Token?>(null) }
  tokenBeingDeleted.value?.let { token ->
    DeleteItemDialog(
        itemName = "${token.name} with all associated alerts",
        dismiss = { tokenBeingDeleted.value = null },
        delete = { viewModel.deleteToken(token.address) },
    )
  }

  val tokens = viewModel.tokensFlow.collectAsLazyPagingItems()
  if (tokens.itemCount == 0) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      Text(
          text = "No saved tokens.",
          textAlign = TextAlign.Center,
          style = Typography.h6.copy(fontWeight = FontWeight.Bold),
      )
    }
  } else {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(tokens) { tokenWithValue ->
        if (tokenWithValue == null) return@items
        TokenWithValueListItem(
            tokenWithValue = tokenWithValue,
            onItemClick = onItemClick,
            onDeleteClick = tokenBeingDeleted::value::set,
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
private fun TokenWithValueListItem(
    tokenWithValue: TokenWithValue,
    onItemClick: (TokenWithValue) -> Unit,
    onDeleteClick: (Token) -> Unit
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
      trailing = {
        IconButton(onClick = { onDeleteClick(tokenWithValue.token) }) {
          Icon(Icons.Outlined.Delete, "")
        }
      },
      overlineText = {
        Text(
            text =
                "Last updated ${TimeAgo.using(tokenWithValue.value.updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())}",
            modifier = Modifier.fillMaxWidth(),
        )
      },
      modifier = Modifier.fillMaxWidth().clickable { onItemClick(tokenWithValue) },
  ) {
    Text(
        text = tokenWithValue.token.name,
        style = Typography.subtitle1.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.fillMaxWidth(),
    )
  }
}
