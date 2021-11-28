package com.moonwatch.api.pancakeswap

import com.moonwatch.api.pancakeswap.di.DaggerPancakeswapTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PancakeswapEndpointsTest {
  private val endpoints: PancakeswapEndpoints =
      DaggerPancakeswapTestComponent.builder().build().pancakeswapEndpoints()

  @Test
  fun getTokenTest() {
    runBlocking { println(endpoints.getToken("0x969f330c1419130b208f258f517af73edda6a884")) }
  }
}
