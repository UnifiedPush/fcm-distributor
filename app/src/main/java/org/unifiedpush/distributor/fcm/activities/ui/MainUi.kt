package org.unifiedpush.distributor.fcm.activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.unifiedpush.android.distributor.ui.compose.PermissionsUi
import org.unifiedpush.android.distributor.ui.compose.RegistrationList
import org.unifiedpush.android.distributor.ui.compose.RegistrationListHeading
import org.unifiedpush.android.distributor.ui.compose.RegistrationsViewModel
import org.unifiedpush.android.distributor.ui.compose.UnregisterBarUi
import org.unifiedpush.android.distributor.ui.compose.state.RegistrationListState
import org.unifiedpush.android.distributor.ui.compose.state.RegistrationState
import org.unifiedpush.distributor.fcm.activities.MainViewModel

@Composable
fun MainUi(viewModel: MainViewModel) {
    val state = viewModel.mainUiState
    val registrationsState = viewModel.registrationsViewModel.state

    if (state.showPermissionDialog) {
        PermissionsUi {
            viewModel.closePermissionDialog()
        }
    }

    Scaffold(
        topBar = {
            if (registrationsState.selectionCount > 0) {
                UnregisterBarUi(
                    viewModel = viewModel.registrationsViewModel,
                    onDelete = { viewModel.deleteSelection() }
                )
            } else {
                AppBarUi()
            }
        }
    ) { innerPadding ->
        MainUiContent(viewModel, innerPadding)
    }
}

@Composable
fun MainUiContent(viewModel: MainViewModel, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                0.dp,
                innerPadding.calculateTopPadding(),
                0.dp,
                innerPadding.calculateBottomPadding()
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    16.dp,
                    0.dp,
                    16.dp,
                    0.dp
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier)
            RegistrationListHeading(
                modifier = Modifier
            )
        }

        RegistrationList(viewModel.registrationsViewModel) {
            // We don't have copyable endpoint
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    val regList =
        listOf(
            RegistrationState(
                icon = null,
                title = "Application 1",
                token = "tok1",
                msgCount = 1337,
                description = "tld.app.1",
                copyable = false
            ),
            RegistrationState(
                icon = null,
                title = "Application 2",
                token = "tok2",
                msgCount = 1,
                description = "tld.app.2",
                copyable = false
            ),
            RegistrationState(
                icon = null,
                title = null,
                token = "tok3",
                msgCount = 2,
                description = "tld.app.3",
                copyable = false
            )
        )
    MainUi(
        MainViewModel(
            MainUiState(),
            RegistrationsViewModel(RegistrationListState(regList))
        )
    )
}
