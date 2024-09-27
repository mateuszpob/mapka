package pl.smolisoft.mapka

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
//import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class DrawerMenu {
    @SuppressLint("NotConstructor")
    @Composable
    fun DrawerMenu(onMenuItemClick: (String) -> Unit) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Home",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onMenuItemClick("Home") }
            )
            Text(
                text = "Settings",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onMenuItemClick("Settings") }
            )
            Text(
                text = "Profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onMenuItemClick("Profile") }
            )
        }
    }

}