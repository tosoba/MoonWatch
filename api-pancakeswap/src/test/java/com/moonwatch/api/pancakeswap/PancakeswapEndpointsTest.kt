package com.moonwatch.api.pancakeswap

import com.moonwatch.api.pancakeswap.di.DaggerPancakeswapTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PancakeswapEndpointsTest {
  private val endpoints: PancakeswapEndpoints =
      DaggerPancakeswapTestComponent.builder().build().pancakeswapEndpoints()

  @Test
  fun getTokenTest() {
    runBlocking { println(endpoints.getToken("0x683b383e9d6cc523f4c9764dacebb5752892fc53")) }
  }
}
