package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. ARCHITECTURE ET DONNÉES

enum class MediaType {
    FILM, SERIE, ANIME
}

enum class MediaStatus(val label: String) {
    A_VOIR("À voir"),
    EN_COURS("En cours"),
    TERMINE("Terminé")
}

data class MediaItem(
    val id: String,
    val titre: String,
    val type: MediaType,
    val genre: String,
    val annee: Int,
    val synopsis: String,
    val note: Float,
    val totalEpisodes: Int = 1,
    val currentEpisode: Int = 0,
    val status: MediaStatus = MediaStatus.A_VOIR,
    val isFavorite: Boolean = false,
    val colorHint: Color,
    val imageUrl: String
)

class HomeViewModel : ViewModel() {
    private val initialData = listOf(
        MediaItem("1", "Dragon Ball Super", MediaType.ANIME, "Shonen", 2015, "Goku et ses amis affrontent de nouveaux dieux et des combattants incroyablement forts à travers de multiples univers. Le tournoi du pouvoir décidera de la survie de tout ce qu'ils connaissent.", 4.2f, 131, 45, MediaStatus.EN_COURS, true, Color(0xFFF59E0B), "https://picsum.photos/seed/dbsuper/400/600"),
        MediaItem("2", "Avengers: Endgame", MediaType.FILM, "Super-héros", 2019, "Après les événements dévastateurs d'Infinity War, les survivants des Avengers doivent se rassembler une dernière fois pour tenter d'annuler les actions de Thanos.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF4F46E5), "https://picsum.photos/seed/avengers/400/600"),
        MediaItem("3", "The Mandalorian", MediaType.SERIE, "Science-fiction", 2019, "Dans les bordures extérieures de la galaxie, loin de l'autorité de la Nouvelle République, un chasseur de primes solitaire, Din Djarin, trouve un enfant mystérieux.", 4.5f, 24, 8, MediaStatus.EN_COURS, false, Color(0xFF10B981), "https://picsum.photos/seed/mando/400/600"),
        MediaItem("4", "Spider-Man: Across the Spider-Verse", MediaType.FILM, "Super-héros", 2023, "Miles Morales voyage à travers le Multivers, où il rencontre une équipe de Spider-Héros chargée de protéger son existence même.", 4.9f, 1, 0, MediaStatus.A_VOIR, false, Color(0xFFE11D48), "https://picsum.photos/seed/spiderverse/400/600"),
        MediaItem("5", "L'Attaque des Titans", MediaType.ANIME, "Dark Fantasy", 2013, "L'humanité vit recluse dans une ville entourée d'immenses murs pour se protéger des Titans, de gigantesques créatures dévoreuses d'hommes.", 4.9f, 89, 89, MediaStatus.TERMINE, true, Color(0xFF991B1B), "https://picsum.photos/seed/snk/400/600"),
        MediaItem("6", "Loki", MediaType.SERIE, "Super-héros", 2021, "Le Dieu de la Malice est arrêté par le Tribunal des Variations Anachroniques et doit les aider à réparer le temps qu'il a altéré.", 4.3f, 12, 12, MediaStatus.TERMINE, false, Color(0xFF059669), "https://picsum.photos/seed/loki/400/600"),
        MediaItem("7", "Dune: Deuxième Partie", MediaType.FILM, "Science-fiction", 2024, "Paul Atréides s'unit à Chani et aux Fremen pour mener la rébellion contre ceux qui ont détruit sa famille et accomplir son destin.", 4.7f, 1, 0, MediaStatus.A_VOIR, true, Color(0xFFD97706), "https://picsum.photos/seed/dune2/400/600"),
        MediaItem("8", "Jujutsu Kaisen", MediaType.ANIME, "Shonen", 2020, "Pour sauver ses amis, Yuji Itadori avale le doigt d'un puissant fléau millénaire et rejoint une école secrète d'exorcistes.", 4.6f, 47, 12, MediaStatus.EN_COURS, false, Color(0xFF4338CA), "https://picsum.photos/seed/jjk/400/600"),
        MediaItem("9", "Star Wars : Un Nouvel Espoir", MediaType.FILM, "Science-fiction", 1977, "Un jeune fermier découvre la Force, fait équipe avec des rebelles, un contrebandier et deux droïdes pour sauver une princesse.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFFFBBF24), "https://picsum.photos/seed/sw4/400/600"),
        MediaItem("10", "WandaVision", MediaType.SERIE, "Super-héros", 2021, "Wanda et Vision vivent une vie de banlieue idéale, jusqu'à ce qu'ils commencent à soupçonner que tout n'est pas ce qu'il semble être.", 4.1f, 9, 9, MediaStatus.TERMINE, false, Color(0xFFBE185D), "https://picsum.photos/seed/wandavision/400/600"),
        
        // Massive Additions
        MediaItem("11", "Naruto Shippuden", MediaType.ANIME, "Shonen", 2007, "Naruto revient à Konoha plus fort pour sauver Sasuke et affronter l'Akatsuki.", 4.5f, 500, 250, MediaStatus.EN_COURS, true, Color(0xFFF97316), "https://picsum.photos/seed/naruto/400/600"),
        MediaItem("12", "The Boys", MediaType.SERIE, "Action / Satire", 2019, "Un groupe de justiciers traque des super-héros corrompus et puissants.", 4.7f, 32, 28, MediaStatus.EN_COURS, false, Color(0xFFDC2626), "https://picsum.photos/seed/theboys/400/600"),
        MediaItem("13", "Interstellar", MediaType.FILM, "Science-fiction", 2014, "Des explorateurs voyagent à travers un trou de ver pour trouver une nouvelle maison pour l'humanité.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF1E3A8A), "https://picsum.photos/seed/interstellar/400/600"),
        MediaItem("14", "One Piece", MediaType.ANIME, "Shonen", 1999, "Luffy navigue sur Grand Line avec son équipage pour trouver le One Piece et devenir le Roi des Pirates.", 4.8f, 1100, 1050, MediaStatus.EN_COURS, true, Color(0xFF38BDF8), "https://picsum.photos/seed/onepiece/400/600"),
        MediaItem("15", "Batman: The Dark Knight", MediaType.FILM, "Super-héros", 2008, "Batman, le lieutenant Gordon et le procureur Harvey Dent s'allient pour démanteler la pègre, mais le Joker sème le chaos.", 4.9f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF171717), "https://picsum.photos/seed/batman/400/600"),
        MediaItem("16", "Stranger Things", MediaType.SERIE, "Fantastique", 2016, "Dans les années 80, la disparition d'un garçon révèle un mystère impliquant des expériences secrètes et des forces surnaturelles.", 4.6f, 34, 34, MediaStatus.TERMINE, false, Color(0xFF991B1B), "https://picsum.photos/seed/strangerthings/400/600"),
        MediaItem("17", "Game of Thrones", MediaType.SERIE, "Fantasy", 2011, "Neuf familles nobles luttent tragiquement pour le contrôle des terres mythiques de Westeros.", 4.7f, 73, 73, MediaStatus.TERMINE, true, Color(0xFF78350F), "https://picsum.photos/seed/got/400/600"),
        MediaItem("18", "Demon Slayer", MediaType.ANIME, "Shonen", 2019, "Tanjiro devient tueur de démons pour venger sa famille et guérir sa petite sœur Nezuko.", 4.8f, 55, 55, MediaStatus.TERMINE, false, Color(0xFF14B8A6), "https://picsum.photos/seed/demonslayer/400/600"),
        MediaItem("19", "Le Seigneur des Anneaux : La Communauté de l'Anneau", MediaType.FILM, "Fantasy", 2001, "Frodon et ses compagnons entament leur quête terrifiante pour détruire l'Anneau Unique.", 4.9f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF047857), "https://picsum.photos/seed/lotr1/400/600"),
        MediaItem("20", "Cyberpunk: Edgerunners", MediaType.ANIME, "Science-fiction", 2022, "Dans une dystopie corrompue, un jeune de la rue tente de survivre en devenant un mercenaire.", 4.7f, 10, 10, MediaStatus.TERMINE, false, Color(0xFFEAB308), "https://picsum.photos/seed/edgerunners/400/600"),
        MediaItem("21", "The Matrix", MediaType.FILM, "Science-fiction", 1999, "Un hacker découvre la vérité sur la réalité et son rôle dans la guerre contre les machines.", 4.8f, 1, 0, MediaStatus.A_VOIR, true, Color(0xFF22C55E), "https://picsum.photos/seed/matrix/400/600"),
        MediaItem("22", "Hunter x Hunter", MediaType.ANIME, "Shonen", 2011, "Gon passe l'examen des Hunters pour retrouver son père disparu et explorer le monde.", 4.9f, 148, 148, MediaStatus.TERMINE, true, Color(0xFF16A34A), "https://picsum.photos/seed/hxh/400/600"),
        MediaItem("23", "Peaky Blinders", MediaType.SERIE, "Drame militaire", 2013, "L'ascension de Tommy Shelby et de son gang impitoyable à Birmingham après la Première Guerre mondiale.", 4.8f, 36, 0, MediaStatus.A_VOIR, false, Color(0xFF334155), "https://picsum.photos/seed/peaky/400/600"),
        MediaItem("24", "Gladiator", MediaType.FILM, "Action historique", 2000, "Un général romain trahi cherche à se venger en devenant un gladiateur.", 4.7f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF92400E), "https://picsum.photos/seed/gladiator/400/600"),
        MediaItem("25", "Invincible", MediaType.SERIE, "Super-héros", 2021, "Le fils du plus grand super-héros de la Terre commence à développer ses propres pouvoirs.", 4.6f, 16, 8, MediaStatus.EN_COURS, false, Color(0xFFFDE047), "https://picsum.photos/seed/invincible/400/600"),
        MediaItem("26", "Harry Potter à l'école des sorciers", MediaType.FILM, "Fantasy", 2001, "Un orphelin découvre sa véritable identité et entre à l'école de magie de Poudlard.", 4.6f, 1, 0, MediaStatus.A_VOIR, true, Color(0xFF7E22CE), "https://picsum.photos/seed/hp1/400/600"),
        MediaItem("27", "Arcane", MediaType.SERIE, "Steampunk / Animation", 2021, "Les tensions s'accroissent entre la riche ville de Piltover et les bas-fonds opprimés de Zaun.", 4.9f, 9, 9, MediaStatus.TERMINE, true, Color(0xFF8B5CF6), "https://picsum.photos/seed/arcane/400/600"),
        MediaItem("28", "Bleach", MediaType.ANIME, "Shonen", 2004, "Ichigo Kurosaki devient un faucheur d'âmes pour protéger les vivants des mauvais esprits.", 4.5f, 366, 120, MediaStatus.EN_COURS, false, Color(0xFFEA580C), "https://picsum.photos/seed/bleach/400/600"),
        MediaItem("29", "Inception", MediaType.FILM, "Science-fiction", 2010, "Un voleur expérimenté est chargé de planter une idée dans l'esprit du PDG d'une entreprise.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF64748B), "https://picsum.photos/seed/inception/400/600"),
        MediaItem("30", "Breaking Bad", MediaType.SERIE, "Drame", 2008, "Un professeur de chimie cancéreux se tourne vers la fabrication de méthamphétamine.", 4.9f, 62, 20, MediaStatus.EN_COURS, true, Color(0xFF15803D), "https://picsum.photos/seed/breakingbad/400/600"),
        MediaItem("31", "Death Note", MediaType.ANIME, "Thriller Psychologique", 2006, "Un lycéen brillant trouve un carnet capable de tuer n'importe qui s'il connaît son nom et son visage.", 4.8f, 37, 37, MediaStatus.TERMINE, true, Color(0xFF171717), "https://picsum.photos/seed/deathnote/400/600"),
        MediaItem("32", "Avatar, le dernier maître de l'air", MediaType.ANIME, "Fantasy", 2005, "Aang, le nouvel Avatar, doit maîtriser les quatre éléments pour mettre fin à la guerre.", 4.9f, 61, 61, MediaStatus.TERMINE, true, Color(0xFF38BDF8), "https://picsum.photos/seed/avatarmla/400/600"),
        MediaItem("33", "Jurassic Park", MediaType.FILM, "Aventure / S-F", 1993, "Des scientifiques clonent des dinosaures pour un parc d'attractions, mais tout dégénère.", 4.7f, 1, 1, MediaStatus.TERMINE, false, Color(0xFF166534), "https://picsum.photos/seed/jurassic/400/600"),
        MediaItem("34", "The Witcher", MediaType.SERIE, "Dark Fantasy", 2019, "Geralt de Riv, un tueur de monstres solitaire, est lié par le destin à une puissante sorcière et une jeune princesse.", 4.2f, 24, 0, MediaStatus.A_VOIR, false, Color(0xFFC2410C), "https://picsum.photos/seed/witcher/400/600"),
        MediaItem("35", "Vikings", MediaType.SERIE, "Action / Historique", 2013, "L'ascension du légendaire héros scandinave Ragnar Lothbrok, de simple fermier à roi des Vikings.", 4.6f, 89, 0, MediaStatus.A_VOIR, false, Color(0xFF64748B), "https://picsum.photos/seed/vikings/400/600"),
        MediaItem("36", "Le Voyage de Chihiro", MediaType.FILM, "Animation / Fantasy", 2001, "Une jeune fille se retrouve piégée dans le monde des esprits et doit travailler dans un établissement de bains pour sauver ses parents.", 4.9f, 1, 1, MediaStatus.TERMINE, true, Color(0xFFF43F5E), "https://picsum.photos/seed/chihiro/400/600"),
        MediaItem("37", "My Hero Academia", MediaType.ANIME, "Shonen", 2016, "Dans un monde où 80% de la population a des super-pouvoirs, le jeune Izuku, né sans alter, rêve de devenir le plus grand des héros.", 4.5f, 138, 40, MediaStatus.EN_COURS, false, Color(0xFF22C55E), "https://picsum.photos/seed/mha/400/600"),
        MediaItem("38", "Prison Break", MediaType.SERIE, "Action / Thriller", 2005, "Un homme se fait délibérément emprisonner pour aider son frère à s'évader d'un couloir de la mort.", 4.7f, 90, 90, MediaStatus.TERMINE, true, Color(0xFF1E293B), "https://picsum.photos/seed/prisonbreak/400/600"),
        MediaItem("39", "Se7en", MediaType.FILM, "Thriller", 1995, "Deux inspecteurs pourchassent un tueur en série qui utilise les sept péchés capitaux comme modus operandi.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF991B1B), "https://picsum.photos/seed/seven/400/600"),
        MediaItem("40", "Fullmetal Alchemist: Brotherhood", MediaType.ANIME, "Shonen / Fantasy", 2009, "Deux frères alchimistes parcourent le monde à la recherche de la pierre philosophale pour restaurer leurs corps mutilés.", 4.9f, 64, 64, MediaStatus.TERMINE, true, Color(0xFFDC2626), "https://picsum.photos/seed/fmab/400/600"),
        MediaItem("41", "Better Call Saul", MediaType.SERIE, "Drame", 2015, "L'évolution de Jimmy McGill, un avocat sans envergure, vers l'infâme Saul Goodman.", 4.9f, 63, 15, MediaStatus.EN_COURS, false, Color(0xFFD97706), "https://picsum.photos/seed/saul/400/600"),
        MediaItem("42", "Akira", MediaType.FILM, "Cyberpunk", 1988, "Dans Néo-Tokyo, une expérience secrète mène deux amis dans une lutte cataclysmique.", 4.7f, 1, 1, MediaStatus.TERMINE, true, Color(0xFFB91C1C), "https://picsum.photos/seed/akira/400/600"),
        MediaItem("43", "Dragon Ball Z", MediaType.ANIME, "Shonen", 1989, "Goku découvre ses origines extraterrestres et défend la Terre contre de redoutables tyrans intergalactiques.", 4.8f, 291, 291, MediaStatus.TERMINE, true, Color(0xFFF59E0B), "https://picsum.photos/seed/dbz/400/600"),
        MediaItem("44", "Doctor Who", MediaType.SERIE, "Science-fiction", 2005, "Un Seigneur du Temps mystérieux voyage à travers l'espace et le temps.", 4.5f, 175, 0, MediaStatus.A_VOIR, false, Color(0xFF2563EB), "https://picsum.photos/seed/doctorwho/400/600"),
        MediaItem("45", "Le Loup de Wall Street", MediaType.FILM, "Comédie noire", 2013, "L'ascension et la chute spectaculaires de Jordan Belfort, un courtier en bourse corrompu.", 4.7f, 1, 1, MediaStatus.TERMINE, true, Color(0xFFEAB308), "https://picsum.photos/seed/wallst/400/600"),
        MediaItem("46", "Rick et Morty", MediaType.SERIE, "Animation / Comédie", 2013, "Un scientifique fou entraîne son petit-fils dans des aventures interdimensionnelles.", 4.8f, 71, 71, MediaStatus.TERMINE, true, Color(0xFF10B981), "https://picsum.photos/seed/rickmorty/400/600"),
        MediaItem("47", "Steins;Gate", MediaType.ANIME, "Science-fiction", 2011, "Des amis modifient le passé via un micro-ondes, altérant la réalité.", 4.8f, 24, 0, MediaStatus.A_VOIR, false, Color(0xFF334155), "https://picsum.photos/seed/steinsgate/400/600"),
        MediaItem("48", "Squid Game", MediaType.SERIE, "Thriller / Drame", 2021, "Des centaines de personnes endettées participent à des jeux mortels.", 4.6f, 9, 9, MediaStatus.TERMINE, true, Color(0xFFE11D48), "https://picsum.photos/seed/squid/400/600"),
        MediaItem("49", "Fight Club", MediaType.FILM, "Drame / Thriller", 1999, "Un employé de bureau fonde un club de combat clandestin.", 4.8f, 1, 1, MediaStatus.TERMINE, true, Color(0xFF0F172A), "https://picsum.photos/seed/fightclub/400/600"),
        MediaItem("50", "Cowboy Bebop", MediaType.ANIME, "Science-fiction", 1998, "L'équipage du vaisseau Bebop voyage pour capturer des criminels.", 4.9f, 26, 26, MediaStatus.TERMINE, true, Color(0xFFCA8A04), "https://picsum.photos/seed/bebop/400/600")
    )

    private val _mediaList = MutableStateFlow(initialData)
    val mediaList: StateFlow<List<MediaItem>> = _mediaList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()
    
    private val _selectedMediaItem = MutableStateFlow<MediaItem?>(null)
    val selectedMediaItem: StateFlow<MediaItem?> = _selectedMediaItem.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun toggleFavorite(id: String) {
        updateItem(id) { it.copy(isFavorite = !it.isFavorite) }
    }

    fun updateStatus(id: String, status: MediaStatus) {
        updateItem(id) { item -> 
            val newEpisode = if (status == MediaStatus.TERMINE) item.totalEpisodes else item.currentEpisode
            item.copy(status = status, currentEpisode = newEpisode) 
        }
    }

    fun incrementEpisode(id: String) {
        updateItem(id) { item ->
            if (item.currentEpisode < item.totalEpisodes) {
                val newEps = item.currentEpisode + 1
                val newStatus = if (newEps == item.totalEpisodes) MediaStatus.TERMINE else MediaStatus.EN_COURS
                item.copy(currentEpisode = newEps, status = newStatus)
            } else {
                item
            }
        }
    }
    
    fun selectMedia(item: MediaItem?) {
        _selectedMediaItem.value = item
    }

    private fun updateItem(id: String, transform: (MediaItem) -> MediaItem) {
        _mediaList.value = _mediaList.value.map { if (it.id == id) transform(it) else it }
        // Update selected item as well if modifying the current open one
        if (_selectedMediaItem.value?.id == id) {
            _selectedMediaItem.value = _mediaList.value.find { it.id == id }
        }
    }
}

// 2. INTERFACE UTILISATEUR (UI)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val mediaList by viewModel.mediaList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val selectedMediaItem by viewModel.selectedMediaItem.collectAsStateWithLifecycle()
    
    val tabs = listOf("Tout", "Films", "Séries", "Animés", "Ma Liste")

    var showAiDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    val cosmicGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B1021), Color(0xFF1E112A), Color(0xFF000000))
    )

    // Filter Logic
    val filteredList = mediaList.filter { item ->
        val matchesSearch = item.titre.contains(searchQuery, ignoreCase = true) || item.genre.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTabIndex) {
            1 -> item.type == MediaType.FILM
            2 -> item.type == MediaType.SERIE
            3 -> item.type == MediaType.ANIME
            4 -> item.isFavorite
            else -> true
        }
        matchesSearch && matchesTab
    }

    // Crossfade to beautifully transition between Main Content and Details View
    Crossfade(targetState = selectedMediaItem, modifier = modifier.fillMaxSize(), label = "ScreenTransition") { selectedItem ->
        if (selectedItem != null) {
            DetailsScreen(
                item = selectedItem,
                onBack = { viewModel.selectMedia(null) },
                onFavoriteClick = { viewModel.toggleFavorite(selectedItem.id) },
                onIncrementClick = { viewModel.incrementEpisode(selectedItem.id) },
                onStatusChange = { viewModel.updateStatus(selectedItem.id, it) }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text("MediaVerse", fontWeight = FontWeight.Bold, color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1021).copy(alpha = 0.9f)),
                        actions = {
                            IconButton(onClick = { /* TODO */ }) {
                                BadgedBox(badge = { Badge { Text("9+") } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                                }
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Profil", tint = Color.White)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { showAiDialog = true },
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
                        Icon(Icons.Default.AutoAwesome, contentDescription = "IA Recommendation", modifier = Modifier.padding(end = 8.dp))
                        Text("Découvrir avec l'IA", fontWeight = FontWeight.Bold)
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cosmicGradient)
                        .padding(innerPadding)
                ) {
                    // Search Bar & Filter Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            placeholder = { Text("Chercher par titre, genre...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFC084FC),
                                unfocusedBorderColor = Color.DarkGray,
                                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .background(Color(0xFF1E293B), CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtres", tint = Color.White)
                        }
                    }

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFC084FC),
                        edgePadding = 16.dp,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.updateTabIndex(index) },
                                text = { 
                                    Text(
                                        title, 
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) Color(0xFFC084FC) else Color.LightGray
                                    ) 
                                }
                            )
                        }
                    }

                    // 3. INTERACTIONS AVANCÉES SUR LES CARTES
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            MediaCard(
                                item = item,
                                onCardClick = { viewModel.selectMedia(item) },
                                onFavoriteClick = { viewModel.toggleFavorite(item.id) },
                                onIncrementClick = { viewModel.incrementEpisode(item.id) },
                                onStatusChange = { viewModel.updateStatus(item.id, it) }
                            )
                        }
                    }
                }
            }

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                        Text("Filtres Avancés", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Bientôt disponible : Tri par note, année, et genres spécifiques.", color = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { 
                                    if (!sheetState.isVisible) showFilterSheet = false 
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fermer")
                        }
                    }
                }
            }

            // 4. FONCTIONNALITÉ BONUS "IA RECOMMENDATION"
            if (showAiDialog) {
                AlertDialog(
                    onDismissRequest = { showAiDialog = false },
                    containerColor = Color(0xFF1E293B),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFC084FC))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recommandation IA", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Text(
                            "D'après vos favoris et vos visionnages récents, vous semblez apprécier les univers épiques avec des batailles d'envergure galactique et des mystères profonds.\n\n" +
                            "🤖 Je vous recommande vivement : \"Neon Genesis Evangelion\" ou \"The Expanse\".\n" +
                            "Ces classiques incontournables explorent l'humanité face à l'inconnu, avec des scénarios sombres et complexes.",
                            color = Color.LightGray
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showAiDialog = false }) {
                            Text("Ajouter à la liste", color = Color(0xFFC084FC), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAiDialog = false }) {
                            Text("Ignorer", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MediaCard(
    item: MediaItem,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onIncrementClick: () -> Unit,
    onStatusChange: (MediaStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Column {
            // Poster with Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(item.colorHint.copy(alpha = 0.6f), Color(0xFF1E293B))
                        )
                    )
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Affiche ${item.titre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF1E293B)),
                                startY = 300f
                            )
                        )
                )

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (item.isFavorite) Color.Red else Color.White
                    )
                }
                
                // Status indicator top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status.label, 
                        color = when(item.status) {
                            MediaStatus.A_VOIR -> Color.LightGray
                            MediaStatus.EN_COURS -> Color(0xFF3B82F6)
                            MediaStatus.TERMINE -> Color(0xFF22C55E)
                        }, 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.titre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.genre, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Star, contentDescription = "Note", tint = Color(0xFFFACC15), modifier = Modifier.size(14.dp))
                    Text(text = "${item.note}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress UI (Séries & Animés)
                if (item.type != MediaType.FILM) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ép. ${item.currentEpisode} / ${item.totalEpisodes}",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        if (item.status != MediaStatus.TERMINE) {
                            IconButton(
                                onClick = onIncrementClick,
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(Color(0xFFC084FC), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    val progress = if (item.totalEpisodes > 0) item.currentEpisode.toFloat() / item.totalEpisodes else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFC084FC),
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Status menu
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(item.status.label, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        MediaStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label, color = Color.White) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// DETAILS SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onIncrementClick: () -> Unit,
    onStatusChange: (MediaStatus) -> Unit
) {
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0B1021),
        topBar = {
            TopAppBar(
                title = { Text(item.titre, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (item.isFavorite) Color.Red else Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient to blend image with background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0B1021)),
                                startY = 400f
                            )
                        )
                )
            }

            // Info Section
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = item.titre,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 38.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgeType(text = item.type.name)
                    BadgeType(text = item.annee.toString())
                    BadgeType(text = item.genre)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${item.note}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Synopsis", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.synopsis,
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Progression Section
                if (item.type != MediaType.FILM) {
                    Text("Progression", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Épisode ${item.currentEpisode} sur ${item.totalEpisodes}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                if (item.status != MediaStatus.TERMINE) {
                                    Button(
                                        onClick = onIncrementClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC084FC))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Vu")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val progress = if (item.totalEpisodes > 0) item.currentEpisode.toFloat() / item.totalEpisodes else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFC084FC),
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Actions
                Text("Statut", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                Box {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Actuellement : ${item.status.label}", fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B)).fillMaxWidth(0.9f)
                    ) {
                        MediaStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label, color = Color.White, fontSize = 16.sp) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeType(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
