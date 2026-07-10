package com.easyhooon.hotswanmetroconflict.data

import dev.zacsweers.metro.Inject

interface FirstRepository {
    fun label(): String
}

interface SecondRepository {
    fun label(): String
}

@Inject
class DefaultFirstRepository : FirstRepository {
    override fun label(): String = "first"
}

@Inject
class DefaultSecondRepository : SecondRepository {
    override fun label(): String = "second"
}
