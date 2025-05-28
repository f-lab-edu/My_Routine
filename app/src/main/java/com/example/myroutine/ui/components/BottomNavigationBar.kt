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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myroutine.R
import com.example.myroutine.Routes

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
        val modifier = Modifier.weight(1f)

        IconButton(
            onClick = {
                navController.navigate(Routes.TODAY) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.today),
                tint = if (currentRoute == Routes.TODAY) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        IconButton(
            onClick = {
                navController.navigate(Routes.CALENDAR) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.calendar),
                tint = if (currentRoute == Routes.CALENDAR) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        Spacer(modifier = modifier)

        IconButton(
            onClick = {
                navController.navigate(Routes.REPORT) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = stringResource(R.string.report),
                tint = if (currentRoute == Routes.REPORT) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }

        IconButton(
            onClick = {
                navController.navigate(Routes.SETTINGS) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = if (currentRoute == Routes.SETTINGS) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }
    }
}

