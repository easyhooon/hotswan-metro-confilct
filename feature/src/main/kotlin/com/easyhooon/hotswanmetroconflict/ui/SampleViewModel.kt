package com.easyhooon.hotswanmetroconflict.ui

import androidx.lifecycle.ViewModel
import com.easyhooon.hotswanmetroconflict.data.FirstRepository
import com.easyhooon.hotswanmetroconflict.data.SecondRepository
import com.easyhooon.hotswanmetroconflict.di.ActivityRetainedScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
class SampleViewModel(
    @param:Assisted private val screenName: String,
    private val firstRepository: FirstRepository,
    private val secondRepository: SecondRepository,
) : ViewModel() {
    val message: String = "$screenName ${firstRepository.label()} ${secondRepository.label()}"

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(ActivityRetainedScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(screenName: String): SampleViewModel
    }
}
