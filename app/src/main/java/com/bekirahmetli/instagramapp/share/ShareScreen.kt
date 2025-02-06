package com.bekirahmetli.instagramapp.share

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekirahmetli.instagramapp.R
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.bekirahmetli.instagramapp.navigation.Screens

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(navController: NavController) {
    var selectedMedia by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Kamera izni istemek için launcher
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
        }
    }

    val requestReadPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
        }
    }

    // MANAGE_EXTERNAL_STORAGE izni istemek için launcher
    val requestManageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
    }

    // İzinleri kontrol etme ve isteme
    LaunchedEffect(Unit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:" + context.packageName)
                    }
                    requestManageStorageLauncher.launch(intent)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestReadPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    val pagerState = rememberPagerState(pageCount = { 3 }) // Sayfa sayısını belirtir
    val scope = rememberCoroutineScope()
    val liste = listOf("İndirilenler","Kamera","Ekran Görüntüleri")
    val menuAcilisKontrol = remember { mutableStateOf(false) }
    val secilenIndeks = remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(50.dp),
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screens.SearchScreen.name)
                    }) {
                        Icon(painter = painterResource(id = R.drawable.close), contentDescription = null)
                    }
                },
                actions = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(100.dp, 50.dp)
                                .clickable {
                                    menuAcilisKontrol.value = true
                                }
                        ) {
                            Text(text = liste[secilenIndeks.value])
                        }

                        DropdownMenu(
                            expanded = menuAcilisKontrol.value,
                            onDismissRequest = { menuAcilisKontrol.value = false }) {
                            liste.forEachIndexed { indeks, dosya ->
                                DropdownMenuItem(
                                    onClick = {
                                        menuAcilisKontrol.value = false
                                        secilenIndeks.value = indeks
                                    },
                                    text = { Text(text = dosya) },
                                )
                            }
                        }
                    }
                    Text(text = "İleri", fontStyle = FontStyle.Italic, fontSize = 27.sp, color = Color.Blue,
                        modifier = Modifier.clickable {
                            if (selectedMedia != null) {
                                Log.e("ShareScreen", "Seçilen medya yolu: $selectedMedia")
                                navController.navigate("sharenext/${Uri.encode(selectedMedia)}")
                            } else {
                                Log.e("ShareScreen", "Lütfen bir medya dosyası seçin.")
                            }
                        })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.topbar),
                    titleContentColor = colorResource(id = R.color.black)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> ShareGallery(
                        selectedFolderIndex = secilenIndeks.value,
                        onMediaSelected = { mediaPath -> selectedMedia = mediaPath }
                    )
                    1 -> ShareCamera()
                    2 -> ShareVideo()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text(text = "Galeri") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text(text = "Fotoğraf") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.outline,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        text = { Text(text = "Video") }
                    )
                }
            }
        }
    }
}