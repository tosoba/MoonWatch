package com.moonwatch.repo.di

import com.moonwatch.core.repo.IAlertRepo
import com.moonwatch.core.repo.ITokenRepo
import com.moonwatch.repo.AlertRepo
import com.moonwatch.repo.TokenRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
  @Binds abstract fun alertRepo(repo: AlertRepo): IAlertRepo
  @Binds abstract fun tokenRepo(repo: TokenRepo): ITokenRepo
}
