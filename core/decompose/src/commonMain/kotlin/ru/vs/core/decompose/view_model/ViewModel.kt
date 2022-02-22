package ru.vs.core.decompose.view_model

import co.touchlab.kermit.Logger
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class ViewModel : InstanceKeeper.Instance {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        logger.i { "Create ${this::class.simpleName}" }
    }

    override fun onDestroy() {
        logger.i { "Destroy ${this::class.simpleName}" }
        viewModelScope.cancel()
    }

    companion object {
        private val logger = Logger.withTag(ViewModel::class.simpleName!!)
    }
}
