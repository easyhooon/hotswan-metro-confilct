package com.easyhooon.hotswanmetroconflict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import com.easyhooon.hotswanmetroconflict.di.ActivityRetainedGraphViewModel
import com.easyhooon.hotswanmetroconflict.di.AppGraph
import com.easyhooon.hotswanmetroconflict.ui.SampleScreen
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

class MainActivity : ComponentActivity() {
    private val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create()
    }

    private val graphViewModel: ActivityRetainedGraphViewModel by viewModels {
        ActivityRetainedGraphViewModel.Factory(appGraph)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides graphViewModel.graph.metroViewModelFactory,
                ) {
                    SampleScreen()
                }
            }
        }
    }
}
