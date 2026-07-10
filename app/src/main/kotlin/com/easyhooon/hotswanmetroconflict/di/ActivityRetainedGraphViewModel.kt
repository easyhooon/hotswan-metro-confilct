package com.easyhooon.hotswanmetroconflict.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.asContribution

class ActivityRetainedGraphViewModel(
    appGraph: AppGraph,
) : ViewModel() {
    val graph: ActivityRetainedGraph =
        appGraph.asContribution<ActivityRetainedGraph.Factory>().createActivityRetainedGraph()

    class Factory(
        private val appGraph: AppGraph,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ActivityRetainedGraphViewModel(appGraph) as T
        }
    }
}
