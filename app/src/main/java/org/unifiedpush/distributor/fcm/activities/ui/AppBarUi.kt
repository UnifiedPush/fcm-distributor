package org.unifiedpush.distributor.fcm.activities.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.unifiedpush.distributor.fcm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarUi() {

    TopAppBar(
        colors = TopAppBarDefaults
            .topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ),
        title = {
            Text(
                stringResource(R.string.app_name)
            )
        },
    )

}
