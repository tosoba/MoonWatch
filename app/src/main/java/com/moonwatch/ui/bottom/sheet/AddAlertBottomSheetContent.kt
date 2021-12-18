package com.moonwatch.ui.bottom.sheet

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moonwatch.MainViewModel
import com.moonwatch.ui.PriceTargetXText
import com.moonwatch.ui.dialog.PriceTargetValidationMessagesDialog
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
fun AddAlertBottomSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: MainViewModel = hiltViewModel(),
) {
  val tokenWithValue = viewModel.tokenWithValueBeingViewed.value ?: throw IllegalArgumentException()
  val (token, tokenValue) = tokenWithValue
  val tokenValueScale = 0.coerceAtLeast(tokenValue.usd.stripTrailingZeros().scale())

  var sellTarget by rememberSaveable { mutableStateOf("") }
  var buyTarget by rememberSaveable { mutableStateOf("") }

  fun isTargetValid(target: String): Boolean = target.toBigDecimalOrNull() != null
  fun isTargetValidOrEmpty(target: String): Boolean = target.isEmpty() || isTargetValid(target)

  var sellTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }
  var buyTargetX by rememberSaveable { mutableStateOf(BigDecimal.ONE) }

  fun BigDecimal.toStringInTokenValueScale(): String =
      stripTrailingZeros().setScale(tokenValueScale, RoundingMode.HALF_UP).toPlainString()

  val scrollState = rememberScrollState()
  val scope = rememberCoroutineScope()

  var priceTargetValidationMessages by rememberSaveable {
    mutableStateOf<List<String>>(emptyList())
  }
  if (priceTargetValidationMessages.isNotEmpty()) {
    PriceTargetValidationMessagesDialog(messages = priceTargetValidationMessages) {
      priceTargetValidationMessages = emptyList()
    }
  }

  Column(
      modifier = Modifier.padding(horizontal = 10.dp).verticalScroll(scrollState),
  ) {
    Box(modifier = Modifier.height(15.dp))

    BottomSheetContentTitleText(text = "Add a new alert")
    TokenValueBottomSheetColumnContent(tokenWithValue)

    OutlinedTextField(
        value = buyTarget,
        onValueChange = {
          buyTarget = it
          buyTargetX =
              if (isTargetValid(buyTarget)) {
                buyTarget.toBigDecimal() / tokenValue.usd
              } else {
                BigDecimal.ONE
              }
        },
        isError =
            !isTargetValidOrEmpty(buyTarget) ||
                (isTargetValid(buyTarget) && buyTargetX >= BigDecimal.ONE),
        label = { Text(text = "Buy target") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
          IconButton(
              onClick = {
                buyTarget = ""
                buyTargetX = BigDecimal.ONE
              },
          ) { Icon(Icons.Default.Clear, "") }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
    ) {
      OutlinedButton(
          enabled = buyTargetX > BigDecimal(0.1) && isTargetValidOrEmpty(buyTarget),
          onClick = {
            buyTargetX -= BigDecimal(0.1)
            buyTarget = (tokenValue.usd * buyTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = buyTargetX < BigDecimal.ONE && isTargetValidOrEmpty(buyTarget),
          onClick = {
            buyTargetX += BigDecimal(0.1)
            buyTarget = (tokenValue.usd * buyTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+0.1X") }
      PriceTargetXText(buyTargetX)
    }

    OutlinedTextField(
        value = sellTarget,
        onValueChange = {
          sellTarget = it
          sellTargetX =
              if (isTargetValid(sellTarget)) {
                sellTarget.toBigDecimal() / tokenValue.usd
              } else {
                BigDecimal.ONE
              }
        },
        isError =
            !isTargetValidOrEmpty(sellTarget) ||
                (isTargetValid(sellTarget) && sellTargetX <= BigDecimal.ONE),
        label = { Text(text = "Sell target") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
          IconButton(
              onClick = {
                sellTarget = ""
                sellTargetX = BigDecimal.ONE
              },
          ) { Icon(Icons.Default.Clear, "") }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
    ) {
      OutlinedButton(
          enabled = sellTargetX > BigDecimal(2.0) && isTargetValid(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal.ONE
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-1X") }
      OutlinedButton(
          enabled = sellTargetX > BigDecimal.ONE && isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX -= BigDecimal(0.1)
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("-0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal(0.1)
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+0.1X") }
      OutlinedButton(
          enabled = isTargetValidOrEmpty(sellTarget),
          onClick = {
            sellTargetX += BigDecimal.ONE
            sellTarget = (tokenValue.usd * sellTargetX).toStringInTokenValueScale()
          },
          modifier = Modifier.weight(1f),
      ) { Text("+1X") }
      PriceTargetXText(sellTargetX)
    }

    val context = LocalContext.current
    OutlinedButton(
        onClick = {
          val validationMessages = mutableListOf<String>()
          if (buyTarget.isBlank() && sellTarget.isBlank()) {
            validationMessages.add("You must specify either a buy or a sell price target.")
          } else {
            if (isTargetValid(buyTarget) && buyTarget.toBigDecimal() >= tokenValue.usd) {
              validationMessages.add(
                  "Chosen buy price target is larger or equal to current token price.")
            }
            if (isTargetValid(sellTarget) && sellTarget.toBigDecimal() <= tokenValue.usd) {
              validationMessages.add(
                  "Chosen sell price target is less or equal to current token price.")
            }
          }

          if (validationMessages.isEmpty()) {
            viewModel.addAlert(
                token.address,
                sellPriceTargetUsd = sellTarget.toBigDecimalOrNull(),
                buyPriceTargetUsd = buyTarget.toBigDecimalOrNull(),
            )
            scope.launch { modalBottomSheetState.hide() }
            Toast.makeText(context, "Alert was created.", Toast.LENGTH_SHORT).show()
          } else {
            priceTargetValidationMessages = validationMessages
          }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(text = "Add alert") }

    Box(modifier = Modifier.height(15.dp))
  }
}
