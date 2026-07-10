package com.easyhooon.hotswanmetroconflict.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
interface RepositoryModule {
    @Binds
    fun bindFirstRepository(impl: DefaultFirstRepository): FirstRepository

    @Binds
    fun bindSecondRepository(impl: DefaultSecondRepository): SecondRepository
}
