package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mock data structures
data class WatchItem(
    val id: String,
    val title: String,
    val currentProgress: Int,
    val totalProgress: Int,
    val type: String // "Série" or "Film"
)

val mockInProgress = listOf(
    WatchItem("1", "Dragon Ball Z", 120, 291, "Série"),
    WatchItem("2", "Loki", 4, 6, "Série"),
    WatchItem("3", "Daredevil", 2, 13, "Série")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onSurpriseClick: () -> Unit = {}) {
    val cosmicGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF31103F), Color(0xFF000000))
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSurpriseClick,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF9333EA), Color(0xFF3B82F6))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "IA Surprise", modifier = Modifier.padding(end = 8.dp))
                Text("IA : Trouve-moi une surprise !", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cosmicGradient)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // 1. En-tête personnalisé
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Salut, Noam !", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "Prêt à reprendre l'aventure ?", fontSize = 14.sp, color = Color.LightGray)
                }
                
                // Profile Selector Mock
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF475569))
                        .clickable { /* TODO: Profile switch */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 2. Section Statistiques & Gamification
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Temps de visionnage", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("14 Jours, 6 Heures", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Badges débloqués", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BadgeIcon(icon = Icons.Default.Star, color = Color(0xFFFACC15), tooltip = "Super Saiyan")
                        BadgeIcon(icon = Icons.Default.AutoAwesome, color = Color(0xFFEF4444), tooltip = "Avenger")
                        BadgeIcon(icon = Icons.Default.PlayArrow, color = Color(0xFF3B82F6), tooltip = "Cinéphile")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 3. Section "En ce moment" (Core Tracking)
            Text("En ce moment", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(mockInProgress) { item ->
                    WatchCard(item = item)
                }
            }
        }
    }
}

@Composable
fun BadgeIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, tooltip: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color.copy(alpha = 0.2f), CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = tooltip, tint = color)
    }
}

@Composable
fun WatchCard(item: WatchItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Text(item.type, fontSize = 12.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ép. ${item.currentProgress} / ${item.totalProgress}", 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Medium, 
                    color = Color.White
                )
                IconButton(
                    onClick = { /* TODO: Incrementation logic */ },
                    modifier = Modifier
                        .background(Color(0xFF3B82F6), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add episode", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            val progress = item.currentProgress.toFloat() / item.totalProgress.coerceAtLeast(1).toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFC084FC),
                trackColor = Color.DarkGray
            )
        }
    }
}
