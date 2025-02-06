package com.bekirahmetli.instagramapp.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bekirahmetli.instagramapp.home.Chat
import com.bekirahmetli.instagramapp.home.HomeCamera
import com.bekirahmetli.instagramapp.home.HomeMessages
import com.bekirahmetli.instagramapp.home.HomeScreen
import com.bekirahmetli.instagramapp.model.Users
import com.bekirahmetli.instagramapp.login.Login
import com.bekirahmetli.instagramapp.login.RegisterUp
import com.bekirahmetli.instagramapp.model.Message
import com.bekirahmetli.instagramapp.model.UserPosts
import com.bekirahmetli.instagramapp.model.UsersDetails
import com.bekirahmetli.instagramapp.news.NewsScreen
import com.bekirahmetli.instagramapp.profile.ChangePassword
import com.bekirahmetli.instagramapp.profile.LikePosts
import com.bekirahmetli.instagramapp.profile.ProfileDetay
import com.bekirahmetli.instagramapp.profile.ProfileEdit
import com.bekirahmetli.instagramapp.profile.ProfileScreen
import com.bekirahmetli.instagramapp.search.SearchScreen
import com.bekirahmetli.instagramapp.search.SearchUsers
import com.bekirahmetli.instagramapp.share.ShareNext
import com.bekirahmetli.instagramapp.share.ShareScreen
import com.bekirahmetli.instagramapp.utils.CommentSheet
import com.bekirahmetli.instagramapp.utils.OtherUserProfile
import com.bekirahmetli.instagramapp.utils.PostTime

@Composable
fun AppNavigation(){// Uygulama navigasyonunu oluşturan bir fonksiyon
    val navController: NavHostController = rememberNavController()// NavController oluşturuluyor
    var isNavBarVisible by remember { mutableStateOf(true) }

    Scaffold (
        bottomBar = {

            if (isNavBarVisible) {
                NavigationBar {

                    listOfNavItems.forEach { navItem ->
                        NavigationBarItem(// Seçili öğe, geçerli rotanın rotaya eşit olup olmadığına bağlı olarak belirlenir
                            selected = currentDestination(navController)?.route == navItem.route,
                            onClick = {
                                navController.navigate(navItem.route) {
                                    // Geçerli rota ile başlangıç rotası arasında bir pop-up yapısı oluşturur
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true// Tek başına en üstte başlatır
                                    restoreState = true// Durum bilgisini geri yükler
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = null
                                )
                            },
                        )
                    }
                }
            }
        }
    ){paddingValues : PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "Login",
            modifier = Modifier.padding(paddingValues)
        ){// Her ekran için composable oluştur
            composable(route = Screens.HomeScreen.name){
                isNavBarVisible = true// BottomNavigationBar görünür olacak
                HomeScreen(navController = navController)// Ana ekran içeriği
            }
            composable(route = Screens.SearchScreen.name){
                isNavBarVisible = true
                SearchScreen(navController)
            }
            composable(route = Screens.ShareScreen.name){
                isNavBarVisible = false
                ShareScreen(navController = navController)
            }
            composable(route = Screens.NewsScreen.name){
                isNavBarVisible = true
                NewsScreen()
            }
            composable(route = Screens.ProfileScreen.name){
                isNavBarVisible = true
                ProfileScreen(navController)
            }
            composable("Ayarlar") {
                isNavBarVisible = true
                ProfileDetay(navController = navController,onNavigateToEdit = { isNavBarVisible = false })
            }
            composable("ProfilEdit"){
                isNavBarVisible = false
                ProfileEdit(navController = navController,onNavigateBack = {
                    isNavBarVisible = true
                })
            }
            composable("RegisterUp"){
                isNavBarVisible = false
                RegisterUp(navController, users = Users(email = null,
                    password = null,
                    user_name = null,
                    adi_soyadi = null,
                    user_id = null,
                    UsersDetails(follower = null
                        , followed = null,post = null,biography = null,web_site = null,profile_picture = null)
                ))
            }
            composable("Login"){
                isNavBarVisible = false
                Login(navController)
            }
            composable("sharenext/{mediaPath}") { backStackEntry ->
                val mediaPath = Uri.decode(backStackEntry.arguments?.getString("mediaPath") ?: "")
                ShareNext(mediaPath = mediaPath,navController)
            }
            composable("HomeCamera"){
                isNavBarVisible = false
                HomeCamera()
            }
            composable("Comment"){
                isNavBarVisible = false
                CommentSheet(post = UserPosts())
            }
            composable("OtherUser/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                OtherUserProfile(userId)
            }
            composable("HomeMessages"){
                isNavBarVisible = false
                HomeMessages(navController,postTime = PostTime())
            }
            composable("Chat/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                Chat(userId, message = Message(),navController)
            }
            composable("LikePosts"){
                isNavBarVisible = false
                LikePosts()
            }
            composable("ChangePassword"){
                isNavBarVisible = false
                ChangePassword(onNavigateBack = {
                    isNavBarVisible = true
                },navController)
            }
            composable("SearchUsers"){
                SearchUsers(navController)
            }
        }
    }
}

@Composable
// Geçerli rotayı almak için yardımcı fonksiyon
private fun currentDestination(navController: NavHostController): NavDestination? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination
}
// NavController'ın geçerli rotasını almak için currentBackStackEntryAsState() kullanılır
// Eğer navBackStackEntry boş değilse, geçerli rotanın hedefini döndürür, aksi halde null döner