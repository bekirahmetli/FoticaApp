package com.bekirahmetli.instagramapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItems(// Navigasyon öğelerini temsil eden veri sınıfı
    val icon:ImageVector,
    val route:String // Navigasyon öğesine atanan rota
)
val listOfNavItems:List<NavItems> = listOf(// Navigasyon öğelerinin listesi
        NavItems(
            icon = Icons.Default.Home,// Ana sayfa ikonu
            route = Screens.HomeScreen.name// Ana sayfa ekranının rotası
        ),
        NavItems(
            icon = Icons.Default.Search,
            route = Screens.SearchScreen.name
        ),
        NavItems(
            icon = Icons.Default.AddCircle,
            route = Screens.ShareScreen.name
        ),
        NavItems(
            icon = Icons.Default.Favorite,
            route = Screens.NewsScreen.name
        ),
        NavItems(
            icon = Icons.Default.Person,
            route = Screens.ProfileScreen.name
        )
)
