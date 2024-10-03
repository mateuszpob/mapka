package pl.smolisoft.mapka


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppDrawerContent(onSettingsClick: () -> Unit, onHelpClick: () -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Menu", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
            Button(onClick = { onSettingsClick() }, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Settings")
            }
            Button(onClick = { onHelpClick() }, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Help")
            }
        }
    }
}

@Composable
fun AppDrawer(drawerState: DrawerState, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    // Handle settings navigation
                },
                onHelpClick = {
                    scope.launch { drawerState.close() }
                    // Handle help navigation
                }
            )
        },
        content = { content() }
    )
}