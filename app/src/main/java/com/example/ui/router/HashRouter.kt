package com.example.ui.router

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen

/**
 * Metadata descriptor for each route managed by the HashRouter.
 */
data class HashRouteInfo(
    val hashPath: String,
    val title: String,
    val category: String,
    val icon: ImageVector,
    val badge: String,
    val summary: String
)

/**
 * HashRouter implementation for Jetpack Compose:
 * Manages view switching via hash-based route keys (e.g. `#/aes`, `#/rsa`, `#/diffie-hellman`).
 * Maintains complete forward & back history stack, enabling browser-like navigation.
 */
class HashRouter(initialRoute: String = "#/aes") {
    private val _currentRoute: MutableState<String> = mutableStateOf(normalizeRoute(initialRoute))
    val currentRoute: MutableState<String> get() = _currentRoute

    val backStack = mutableStateListOf<String>()
    val forwardStack = mutableStateListOf<String>()

    val canGoBack: Boolean get() = backStack.isNotEmpty()
    val canGoForward: Boolean get() = forwardStack.isNotEmpty()

    var navigationCount by mutableStateOf(0)
        private set

    fun navigate(targetRoute: String) {
        val normalized = normalizeRoute(targetRoute)
        if (_currentRoute.value == normalized) return

        backStack.add(_currentRoute.value)
        forwardStack.clear()
        _currentRoute.value = normalized
        navigationCount++
    }

    fun goBack(): Boolean {
        if (!canGoBack) return false
        val prev = backStack.removeAt(backStack.size - 1)
        forwardStack.add(_currentRoute.value)
        _currentRoute.value = prev
        navigationCount++
        return true
    }

    fun goForward(): Boolean {
        if (!canGoForward) return false
        val next = forwardStack.removeAt(forwardStack.size - 1)
        backStack.add(_currentRoute.value)
        _currentRoute.value = next
        navigationCount++
        return true
    }

    fun replace(targetRoute: String) {
        val normalized = normalizeRoute(targetRoute)
        _currentRoute.value = normalized
        navigationCount++
    }

    fun reload() {
        navigationCount++
    }

    companion object {
        fun normalizeRoute(route: String): String {
            val trimmed = route.trim()
            return when {
                trimmed.startsWith("#/") -> trimmed
                trimmed.startsWith("#") -> "#/" + trimmed.removePrefix("#").trimStart('/')
                trimmed.startsWith("/") -> "#$trimmed"
                else -> "#/$trimmed"
            }
        }

        val DEFAULT_ROUTES = listOf(
            HashRouteInfo(
                hashPath = "#/aes",
                title = "AES-256 / 128",
                category = "Symmetric",
                icon = Icons.Default.Lock,
                badge = "GCM & CBC",
                summary = "Rijndael block cipher with interactive 4-step round matrix visualizer & GCM AEAD"
            ),
            HashRouteInfo(
                hashPath = "#/rsa",
                title = "RSA Asymmetric",
                category = "Asymmetric",
                icon = Icons.Default.VpnKey,
                badge = "Math & 2048-bit",
                summary = "Public key cryptography: step-by-step totient math sandbox and 2048-bit Keystore lab"
            ),
            HashRouteInfo(
                hashPath = "#/diffie-hellman",
                title = "Diffie-Hellman",
                category = "Key Exchange",
                icon = Icons.Default.SwapHoriz,
                badge = "Discrete Log",
                summary = "Alice & Bob shared symmetric key agreement over an insecure channel"
            ),
            HashRouteInfo(
                hashPath = "#/hash-integrity",
                title = "Hash & Avalanche",
                category = "Integrity",
                icon = Icons.Default.Fingerprint,
                badge = "SHA-256 / HMAC",
                summary = "Cryptographic digests with live bit-level avalanche effect visualizer"
            ),
            HashRouteInfo(
                hashPath = "#/caesar-vigenere",
                title = "Classical Ciphers",
                category = "Historical",
                icon = Icons.Default.Code,
                badge = "Brute Force",
                summary = "Caesar monoalphabetic shift cracker & Vigenère polyalphabetic cryptanalysis"
            ),
            HashRouteInfo(
                hashPath = "#/steganography",
                title = "Steganography",
                category = "Stego",
                icon = Icons.Default.VisibilityOff,
                badge = "Zero-Width",
                summary = "Conceal binary secrets invisibly inside standard text using Unicode zero-width chars"
            )
        )
    }
}

@Composable
fun rememberHashRouter(initialRoute: String = "#/aes"): HashRouter {
    return remember { HashRouter(initialRoute) }
}

/**
 * Interactive Address Bar and Route Pill Bar for the HashRouter.
 */
@Composable
fun HashRouteBar(
    router: HashRouter,
    availableRoutes: List<HashRouteInfo> = HashRouter.DEFAULT_ROUTES,
    onRouteSelected: (String) -> Unit = { router.navigate(it) },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentHash = router.currentRoute.value
    val currentInfo = availableRoutes.find { it.hashPath == currentHash }
        ?: HashRouteInfo(currentHash, currentHash, "Custom", Icons.Default.AltRoute, "Route", "")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Address Bar Card (Browser/Terminal simulator)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { router.goBack() },
                    enabled = router.canGoBack,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("router_btn_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (router.canGoBack) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Forward Button
                IconButton(
                    onClick = { router.goForward() },
                    enabled = router.canGoForward,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("router_btn_forward")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (router.canGoForward) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = { router.reload() },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("router_btn_reload")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Route",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // URL Display with Hash Path
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "cryptolab://sandbox/",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currentHash,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Copy Route
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Route URL", "cryptolab://sandbox/$currentHash"))
                        Toast.makeText(context, "Copied hash route: $currentHash", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("router_btn_copy")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Hash Route",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Horizontal Hash Route Switcher Pills
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableRoutes.forEach { route ->
                val isSelected = currentHash == route.hashPath
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onRouteSelected(route.hashPath) }
                        .testTag("hash_pill_${route.hashPath.removePrefix("#/").replace("-", "_")}"),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) CyberCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) CyberCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = route.icon,
                            contentDescription = route.title,
                            tint = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = route.hashPath,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurface
                        )
                        // Tiny category badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) CyberCyan.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = route.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Host container that renders views matching the active hash route.
 * Also wires Compose BackHandler to pop router history first before exiting the screen.
 */
@Composable
fun HashRouterHost(
    router: HashRouter,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    // Intercept back button when router has history
    BackHandler(enabled = router.canGoBack) {
        router.goBack()
    }

    AnimatedContent(
        targetState = router.currentRoute.value,
        transitionSpec = {
            (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> -width / 3 } + fadeOut())
        },
        modifier = modifier,
        label = "HashRouterHostTransition"
    ) { route ->
        content(route)
    }
}
