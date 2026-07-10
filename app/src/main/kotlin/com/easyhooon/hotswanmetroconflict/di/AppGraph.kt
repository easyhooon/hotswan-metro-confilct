package com.easyhooon.hotswanmetroconflict.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(): AppGraph
    }
}

@GraphExtension(ActivityRetainedScope::class)
interface ActivityRetainedGraph : ViewModelGraph {
    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createActivityRetainedGraph(): ActivityRetainedGraph
    }
}
