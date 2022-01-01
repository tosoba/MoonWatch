package com.moonwatch.core.model

interface ITokenAlertWithCurrentValue {
  val token: IToken
  val alert: ITokenAlert
  val currentValue: ITokenValue
}
