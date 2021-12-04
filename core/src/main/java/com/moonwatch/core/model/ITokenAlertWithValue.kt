package com.moonwatch.core.model

interface ITokenAlertWithValue {
  val token: IToken
  val alert: ITokenAlert
  val value: ITokenValue
}
