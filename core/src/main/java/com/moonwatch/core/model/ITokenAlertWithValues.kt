package com.moonwatch.core.model

interface ITokenAlertWithValues {
  val token: IToken
  val alert: ITokenAlert
  val currentValue: ITokenValue
  val creationValue: ITokenValue
}
