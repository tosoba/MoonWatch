package com.moonwatch.core.android.ext

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

val Long.millisToLocalDateTime: LocalDateTime
  get() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
