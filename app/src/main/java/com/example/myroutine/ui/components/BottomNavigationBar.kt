import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    BottomAppBar(
        cutoutShape = CircleShape,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 8.dp
    ) {
        // 각 슬롯의 비율을 똑같이 맞춤
        val modifier = Modifier.weight(1f)

        // Slot 1 (왼쪽 아이콘 1)
        IconButton(
            onClick = {
                navController.navigate("today") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "today",
                tint = if (currentRoute == "today") MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        // Slot 2 (왼쪽 아이콘 2)
        IconButton(
            onClick = {
                navController.navigate("calendar") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "calendar",
                tint = if (currentRoute == "calendar") MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        // Slot 3 (FAB 자리 - 비워둠)
        Spacer(modifier = modifier)

        // Slot 4 (오른쪽 아이콘 1)
        IconButton(
            onClick = {
                navController.navigate("report") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "report",
                tint = if (currentRoute == "report") MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        // Slot 5 (오른쪽 아이콘 2)
        IconButton(
            onClick = {
                navController.navigate("settings") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "settings",
                tint = if (currentRoute == "settings") MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }
    }
}

