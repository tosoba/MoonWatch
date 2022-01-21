package com.moonwatch.core.di

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class PancakeswapEndpointsWithExtendedTimeouts(
    val value: String = "PancakeswapEndpointsWithExtendedTimeouts"
)
