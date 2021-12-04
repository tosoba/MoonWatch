package com.moonwatch.core.model

interface IToken {
  val address: String
  val name: String
  val symbol: String
  val chain: Chain
}
