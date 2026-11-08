package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlaceCategory
import com.example.data.model.PlaceItem
import com.example.ui.components.AuroraButton
import com.example.ui.components.AuroraCard
import com.example.ui.components.AuroraPill
import com.example.ui.components.AuroraSearchBar
import com.example.ui.theme.AuroraDarkCyan
import com.example.ui.theme.AuroraDarkElevated
import com.example.ui.theme.AuroraDarkPrimary
import com.example.ui.theme.AuroraDarkSuccess
import com.example.ui.theme.AuroraDarkViolet
import com.example.ui.viewmodel.AppViewModel

@Composable
fun MapsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val location by viewModel.currentLocation.collectAsState()
    val searchQuery by viewModel.mapSearchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearchingMaps.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val nearbyPlaces by viewModel.nearbyPlaces.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val routeInfo by viewModel.routeInfo.collectAsState()
    val savedPlaces by viewModel.savedPlaces.collectAsState(initial = emptyList())
    val strings = viewModel.strings

    // Map Pan and Zoom states
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var showSavedDrawer by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("maps_screen")
    ) {
        // Interactive Vector Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + panOffsetX
            val centerY = height / 2f + panOffsetY

            // Background canvas
            drawRect(color = Color(0xFF0B1222))

            // Grid lines / Coordinates
            val gridStep = 80f * zoomScale
            var gx = (panOffsetX % gridStep)
            while (gx < width) {
                drawLine(
                    color = Color(0xFF162138),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = (panOffsetY % gridStep)
            while (gy < height) {
                drawLine(
                    color = Color(0xFF162138),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // Stylized River / Water body
            val riverPath = Path().apply {
                moveTo(0f, centerY + 180f * zoomScale)
                cubicTo(
                    width * 0.3f, centerY + 120f * zoomScale,
                    width * 0.7f, centerY + 240f * zoomScale,
                    width, centerY + 160f * zoomScale
                )
                lineTo(width, centerY + 210f * zoomScale)
                cubicTo(
                    width * 0.7f, centerY + 290f * zoomScale,
                    width * 0.3f, centerY + 170f * zoomScale,
                    0f, centerY + 230f * zoomScale
                )
                close()
            }
            drawPath(
                path = riverPath,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF0F2B48), Color(0xFF123E68))
                )
            )

            // Primary arterial road network
            val roadColor = Color(0xFF24334C)
            drawLine(
                color = roadColor,
                start = Offset(centerX - 350f * zoomScale, centerY - 250f * zoomScale),
                end = Offset(centerX + 350f * zoomScale, centerY + 250f * zoomScale),
                strokeWidth = 12f * zoomScale
            )
            drawLine(
                color = roadColor,
                start = Offset(centerX - 300f * zoomScale, centerY + 200f * zoomScale),
                end = Offset(centerX + 300f * zoomScale, centerY - 200f * zoomScale),
                strokeWidth = 10f * zoomScale
            )
            drawLine(
                color = roadColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 8f * zoomScale
            )
            drawLine(
                color = roadColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = 8f * zoomScale
            )

            // Draw route line if a place is selected
            if (selectedPlace != null) {
                val target = selectedPlace!!
                val dx = ((target.longitude - location.longitude) * 25000f * zoomScale).toFloat()
                val dy = (-(target.latitude - location.latitude) * 25000f * zoomScale).toFloat()
                val targetOffset = Offset(centerX + dx, centerY + dy)

                val routePath = Path().apply {
                    moveTo(centerX, centerY)
                    // mid inflection
                    lineTo(centerX + dx * 0.4f, centerY)
                    lineTo(centerX + dx * 0.4f, targetOffset.y)
                    lineTo(targetOffset.x, targetOffset.y)
                }

                // Glowing route line
                drawPath(
                    path = routePath,
                    color = AuroraDarkCyan,
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                    )
                )
            }

            // Draw Nearby Places Pins
            nearbyPlaces.forEach { place ->
                val dx = ((place.longitude - location.longitude) * 25000f * zoomScale).toFloat()
                val dy = (-(place.latitude - location.latitude) * 25000f * zoomScale).toFloat()
                val pinCenter = Offset(centerX + dx, centerY + dy)

                val isSelected = selectedPlace?.id == place.id
                val pinColor = when (place.category) {
                    PlaceCategory.CAFE -> AuroraDarkViolet
                    PlaceCategory.RESTAURANT -> Color(0xFFFF9800)
                    PlaceCategory.TRANSIT -> AuroraDarkCyan
                    PlaceCategory.PARK -> AuroraDarkSuccess
                    PlaceCategory.HOSPITAL -> Color(0xFFEF4444)
                    else -> AuroraDarkPrimary
                }

                // Pin base glow
                drawCircle(
                    color = pinColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                    radius = if (isSelected) 22f else 14f,
                    center = pinCenter
                )
                // Pin inner core
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 10f else 6f,
                    center = pinCenter
                )
            }

            // Draw User Current Location Pulse
            drawCircle(
                color = AuroraDarkCyan.copy(alpha = 0.2f),
                radius = 36f * zoomScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = AuroraDarkCyan.copy(alpha = 0.4f),
                radius = 20f * zoomScale,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(centerX, centerY)
            )
        }

        // Top Overlay: Search Bar & Autocomplete List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AuroraSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onMapSearchQueryChange(it) },
                onSearch = { viewModel.onMapSearchQueryChange(it) },
                placeholder = strings.searchPlaceholder,
                onClear = { viewModel.onMapSearchQueryChange("") },
                testTag = "maps_search_input"
            )

            // Live autocomplete results
            if (searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setLocation(result.latitude, result.longitude, result.title)
                                        viewModel.selectPlace(result)
                                        viewModel.onMapSearchQueryChange("")
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = AuroraDarkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = result.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = result.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PlaceCategory.values()) { cat ->
                    AuroraPill(
                        text = cat.displayName,
                        selected = selectedCategory == cat,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }
        }

        // Floating Action Controls (Right side: Current Location, Zoom +, Zoom -, Bookmarks)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                    .testTag("recenter_gps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Current Location",
                    tint = AuroraDarkCyan
                )
            }

            IconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { showSavedDrawer = !showSavedDrawer },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (savedPlaces.isNotEmpty()) Brush.horizontalGradient(listOf(AuroraDarkPrimary, AuroraDarkViolet))
                        else Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                    .testTag("saved_places_toggle")
            ) {
                Icon(
                    imageVector = if (savedPlaces.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Saved Places",
                    tint = if (savedPlaces.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Bottom Selected Place & Route Navigation Card
        AnimatedVisibility(
            visible = selectedPlace != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            if (selectedPlace != null) {
                val place = selectedPlace!!
                AuroraCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = place.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${place.rating}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    AuroraPill(text = place.category.displayName)
                                }
                            }

                            Row {
                                IconButton(onClick = { viewModel.toggleSavePlace(place) }) {
                                    Icon(
                                        imageVector = if (place.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = if (place.isSaved) AuroraDarkCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.clearSelectedPlace() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Route details
                        if (routeInfo != null) {
                            val route = routeInfo!!
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = strings.distance, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "${route.distanceKm} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuroraDarkCyan)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = strings.eta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "${route.durationMinutes} mins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AuroraDarkViolet)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            AuroraButton(
                                text = strings.startNavigation,
                                onClick = { /* Navigate step-by-step */ },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                                },
                                testTag = "start_navigation_button"
                            )
                        }
                    }
                }
            }
        }

        // Saved Places Quick Bottom Sheet/Drawer
        AnimatedVisibility(
            visible = showSavedDrawer,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            AuroraCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.savedPlaces} (${savedPlaces.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showSavedDrawer = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (savedPlaces.isEmpty()) {
                        Text(
                            text = strings.noSavedPlaces,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height(160.dp)) {
                            items(savedPlaces) { saved ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectPlace(saved)
                                            showSavedDrawer = false
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = saved.title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = saved.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                    IconButton(onClick = { viewModel.toggleSavePlace(saved) }) {
                                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Remove", tint = AuroraDarkCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
