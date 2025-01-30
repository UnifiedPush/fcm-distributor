package org.unifiedpush.distributor.fcm.activities

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.unifiedpush.android.distributor.ui.compose.RegistrationsViewModel
import org.unifiedpush.android.distributor.ui.compose.state.RegistrationListState
import org.unifiedpush.distributor.fcm.activities.ui.MainUiState

class MainViewModel(
    mainUiState: MainUiState,
    val registrationsViewModel: RegistrationsViewModel
) : ViewModel() {
    constructor(context: Context) : this(
        mainUiState = MainUiState(),
        registrationsViewModel = RegistrationsViewModel(
            getRegistrationListState(context)
        )
    )

    var mainUiState by mutableStateOf(mainUiState)
        private set

    fun closePermissionDialog() {
        viewModelScope.launch {
            mainUiState = mainUiState.copy(showPermissionDialog = false)
        }
    }
    fun refreshRegistrations(context: Context) {
        viewModelScope.launch {
            registrationsViewModel.state = getRegistrationListState(context)
        }
    }

    fun deleteSelection() {
        viewModelScope.launch {
            val state = registrationsViewModel.state
            val tokenList = state.list.filter { it.selected }.map { it.token }
            publishAction(
                AppAction(AppAction.Action.DeleteRegistration(tokenList))
            )
            registrationsViewModel.state = RegistrationListState(
                list = state.list.filter {
                    !it.selected
                },
                selectionCount = 0
            )
        }
    }
}
