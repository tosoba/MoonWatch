package com.moonwatch.core.di

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultPancakeswapEndpoints(val value: String = "DefaultPancakeswapEndpoints")
