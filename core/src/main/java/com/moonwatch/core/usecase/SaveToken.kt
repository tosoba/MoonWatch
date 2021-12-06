package com.moonwatch.core.usecase

import com.moonwatch.core.model.IToken
import com.moonwatch.core.repo.ITokenRepo
import dagger.Reusable
import javax.inject.Inject

@Reusable class SaveToken @Inject constructor(private val repo: ITokenRepo) {
    suspend operator fun invoke(token: IToken) {

    }
}
