package com.moonwatch.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.moonwatch.R

@Composable
fun CopyIconButton(text: String, toastText: String) {
  val context = LocalContext.current
  IconButton(
      onClick = {
        val clip = ClipData.newPlainText("view_token_copied_value", text)
        ContextCompat.getSystemService(context, ClipboardManager::class.java)?.let { manager ->
          manager.setPrimaryClip(clip)
          Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
      },
  ) { Icon(painterResource(R.drawable.ic_baseline_content_copy_24), "") }
}
