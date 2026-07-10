package com.easyhooon.hotswanmetroconflict.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun SampleScreen() {
    val viewModel = assistedMetroViewModel<SampleViewModel, SampleViewModel.Factory> {
        create("sample")
    }

    Text(viewModel.message)
}
