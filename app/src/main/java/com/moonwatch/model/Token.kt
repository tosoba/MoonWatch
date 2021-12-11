package com.moonwatch.model

import android.os.Parcelable
import com.moonwatch.core.model.Chain
import com.moonwatch.core.model.IToken
import kotlinx.parcelize.Parcelize

@Parcelize
data class Token(
    override val address: String,
    override val name: String,
    override val symbol: String,
    override val chain: Chain
) : IToken, Parcelable {
  constructor(
      other: IToken
  ) : this(
      other.address,
      other.name,
      other.symbol,
      other.chain,
  )
}
