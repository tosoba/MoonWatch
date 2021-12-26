package com.moonwatch.ui.bottom.sheet

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moonwatch.MainViewModel
import com.moonwatch.core.exception.InvalidAddressException
import com.moonwatch.core.model.Failed
import com.moonwatch.core.model.LoadingInProgress
import com.moonwatch.core.model.Ready
import com.moonwatch.ui.RetryLoadingTokenButton
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class, FlowPreview::class)
fun SaveTokenBottomSheetContent(
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: MainViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  Column(modifier = Modifier.padding(vertical = 15.dp, horizontal = 10.dp)) {
    BottomSheetContentTitleText("Add a new token")
    val tokenAddress = viewModel.tokenAddress.collectAsState(initial = "")
    OutlinedTextField(
        value = tokenAddress.value,
        onValueChange = viewModel::setTokenAddress,
        label = { Text("Address") },
        singleLine = true,
        isError = viewModel.tokenWithValueBeingAdded.loadable is Failed,
        modifier = Modifier.fillMaxWidth(),
    )
    when (val tokenWithValue = viewModel.tokenWithValueBeingAdded.loadable) {
      is Failed -> {
        when (val error = tokenWithValue.error) {
          is HttpException -> {
            if (error.code() == 404) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Token not found.", textAlign = TextAlign.Center)
              }
            } else {
              Toast.makeText(
                      LocalContext.current,
                      "Unknown network error.",
                      Toast.LENGTH_SHORT,
                  )
                  .show()
              RetryLoadingTokenButton(scope)
            }
          }
          is TimeoutCancellationException -> {
            Toast.makeText(LocalContext.current, "Request has timed out.", Toast.LENGTH_SHORT)
                .show()
            RetryLoadingTokenButton(scope)
          }
          is IOException -> {
            Toast.makeText(
                    LocalContext.current,
                    "No internet connection.",
                    Toast.LENGTH_SHORT,
                )
                .show()
            RetryLoadingTokenButton(scope)
          }
          is InvalidAddressException -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
              Text(text = "Invalid token address.", textAlign = TextAlign.Center)
            }
          }
          else -> {
            Toast.makeText(LocalContext.current, "Unknown error.", Toast.LENGTH_SHORT).show()
            RetryLoadingTokenButton(scope)
          }
        }
      }
      is Ready -> {
        OutlinedTextField(
            value = tokenWithValue.value.token.name,
            onValueChange = {},
            label = { Text("Name") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = tokenWithValue.value.value.usd.toPlainString(),
            onValueChange = {},
            label = { Text("Value in USD") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.SpaceAround) {
          OutlinedButton(
              onClick = {
                scope.launch {
                  viewModel.clearTokenBeingAddedAddress()
                  modalBottomSheetState.hide()
                }
              },
              modifier = Modifier.weight(1f),
          ) { Text(text = "Cancel") }
          Box(modifier = Modifier.size(5.dp))
          OutlinedButton(
              onClick = {
                scope.launch {
                  viewModel.saveTokenCurrentlyBeingAdded()
                  modalBottomSheetState.hide()
                }
              },
              modifier = Modifier.weight(1f),
          ) { Text(text = "Save") }
        }
      }
      is LoadingInProgress -> {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(10.dp),
        ) { CircularProgressIndicator() }
      }
      else -> return@Column
    }
  }
}
