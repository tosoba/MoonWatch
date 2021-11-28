package com.moonwatch.api.pancakeswap.di

import com.moonwatch.api.pancakeswap.PancakeswapEndpoints
import com.moonwatch.core.android.di.NetworkModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [PancakeswapNetworkModule::class, NetworkModule::class])
interface PancakeswapTestComponent {
  fun pancakeswapEndpoints(): PancakeswapEndpoints
}
