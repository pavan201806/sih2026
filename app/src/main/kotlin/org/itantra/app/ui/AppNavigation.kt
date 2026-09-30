package org.itantra.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.itantra.bench.UtteranceTrace

/**
 * Destinations across the RakshaVaani application.
 */
enum class Destination(val title: String) {
    OPERATING("RakshaVaani"),
    MENU("SETTINGS"),
    MESSAGES("MESSAGES"),
    LANGUAGE("LANGUAGE"),
    METRICS("METRICS"),
    MODE("MODE & TRANSPORT"),
    STORAGE("STORAGE"),
    MODEL_SETUP("AI MODEL SETUP"),
    UNIT_NAME("DEVICE NAME"),
    LOCATE("LOCATE"),
    LOCATE_UNIT("LOCATE"),
    TEXT_SIZE("TEXT SIZE"),
    LICENCES("LICENCES"),
    LICENCE_TEXT("LICENCE"),
}

/** Everything the shell needs from the engine. */
data class AppState(
    val operating: OperatingState,
    val traces: List<UtteranceTrace> = emptyList(),
    val latencyHealth: LatencyHealth = LatencyHealth(),
    val languages: List<LanguageOption> = emptyList(),
    val transports: List<TransportOption> = emptyList(),
    val packs: List<PackRow> = emptyList(),
    val licences: List<LicenceRow> = emptyList(),
    val distributionNotice: String? = null,
    val packStatus: String? = null,
    val downloads: List<Download> = emptyList(),
    val buildLine: String? = null,
    val ready: Boolean = true,
    val loadingLabel: String? = null,
    val loadingProgress: Float? = null,
    val relayMode: Boolean = false,
    val ttl: Int = 3,
    val locked: Boolean = false,
    val textScale: Float = 1f,
    val locate: LocateState? = null,
    val unitsHeard: List<UnitInfo> = emptyList(),
    val defaultUnitName: String = "",
)

/** What the shell can ask the engine to do. */
data class AppActions(
    val onTransmitChange: (Boolean) -> Unit,
    val onAlert: () -> Unit,
    val onLanguageChosen: (String) -> Unit,
    val readLicence: (String) -> String?,
    val onReplay: (String) -> Unit,
    val onImportPacks: () -> Unit,
    val onDownload: (Download) -> Unit,
    val onDownloadAll: (List<Download>) -> Unit = { downloads -> downloads.forEach(onDownload) },
    val onDeletePack: (PackRow) -> Unit,
    val onExportCsv: () -> Unit,
    val onModeChange: (String) -> Unit = {},
    val onUnitName: (String) -> Unit = {},
    val onStartLocating: (Int) -> Unit = {},
    val onStopLocating: () -> Unit = {},
    val onLocateSound: (SoundFrom) -> Unit = {},
    val onTextScale: (Float) -> Unit = {},
    val onRelayMode: (Boolean) -> Unit = {},
    val onTtl: (Int) -> Unit = {},
    val onRoad: (id: String, on: Boolean) -> Unit = { _, _ -> },
    val onOpened: () -> Unit = {},
)

@Composable
fun ItantraApp(
    state: AppState,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    val base = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(base.density, base.fontScale * state.textScale)) {
        Routed(state, actions, modifier)
    }
}

@Composable
private fun Routed(
    state: AppState,
    actions: AppActions,
    modifier: Modifier = Modifier,
) {
    var where by remember { mutableStateOf(Destination.OPERATING) }
    var licence by remember { mutableStateOf<LicenceRow?>(null) }

    BackHandler(enabled = where != Destination.OPERATING) {
        if (where == Destination.LOCATE_UNIT) actions.onStopLocating()
        where = back(where)
    }

    if (state.locked) {
        HoldScreen(onOpened = actions.onOpened, modifier = modifier)
        return
    }

    if (!state.ready) {
        SplashScreen(
            loading = state.loadingLabel,
            progress = state.loadingProgress,
            modifier = modifier,
        )
        return
    }

    if (where == Destination.OPERATING) {
        OperatingScreen(
            state = state.operating,
            onTransmitChange = actions.onTransmitChange,
            onAlert = actions.onAlert,
            onLanguageSelected = actions.onLanguageChosen,
            onMenu = { where = Destination.MENU },
            onReplay = actions.onReplay,
            onModeChange = actions.onModeChange,
            onLocate = { where = Destination.LOCATE },
            modifier = modifier,
        )
        return
    }

    if (where in OwnHeader) {
        when (where) {
            Destination.MENU ->
                ControlRoomScreen(
                    state = state,
                    onOpen = { where = it },
                    onBack = { where = back(where) },
                    onRelayMode = actions.onRelayMode,
                    onTtl = actions.onTtl,
                    modifier = modifier,
                )

            Destination.UNIT_NAME ->
                UnitNameScreen(
                    current = state.operating.unitName,
                    defaultName = state.defaultUnitName,
                    onSave = {
                        actions.onUnitName(it)
                        where = back(where)
                    },
                    onBack = { where = back(where) },
                    modifier = modifier,
                )

            Destination.LOCATE ->
                LocateListScreen(
                    units = state.unitsHeard,
                    onSelect = {
                        actions.onStartLocating(it.src)
                        where = Destination.LOCATE_UNIT
                    },
                    onBack = { where = back(where) },
                    modifier = modifier,
                )

            Destination.LOCATE_UNIT -> {
                val walk = state.locate
                if (walk != null) {
                    LocateScreen(
                        state = walk,
                        onStop = {
                            actions.onStopLocating()
                            where = Destination.LOCATE
                        },
                        onSound = actions.onLocateSound,
                        modifier = modifier,
                    )
                } else {
                    LocateListScreen(
                        units = state.unitsHeard,
                        onSelect = { actions.onStartLocating(it.src) },
                        onBack = { where = back(where) },
                        modifier = modifier,
                    )
                }
            }

            Destination.TEXT_SIZE ->
                TextSizeScreen(
                    onBack = { where = back(where) },
                    scale = state.textScale,
                    onScale = actions.onTextScale,
                    modifier = modifier,
                )

            else -> Unit
        }
        return
    }

    SubScreen(title = where.title, onBack = { where = back(where) }, modifier = modifier) {
        when (where) {
            Destination.MESSAGES ->
                MessageLogScreen(
                    messages = state.operating.messages,
                    onReplay = { actions.onReplay(it.text) },
                )

            Destination.LANGUAGE ->
                LanguageScreen(
                    languages = state.languages,
                    selected = state.operating.languageCode,
                    onSelect = actions.onLanguageChosen,
                )

            Destination.METRICS ->
                MetricsScreen(
                    traces = state.traces,
                    onExportCsv = actions.onExportCsv,
                    status = state.packStatus,
                    health = state.latencyHealth,
                )

            Destination.MODE ->
                ModeAndTransportScreen(
                    transports = state.transports,
                    mode = state.operating.mode,
                    onModeChange = actions.onModeChange,
                    onRoad = actions.onRoad,
                )

            Destination.STORAGE ->
                StorageScreen(
                    packs = state.packs,
                    onDelete = actions.onDeletePack,
                    onImport = actions.onImportPacks,
                    status = state.packStatus,
                    downloads = state.downloads,
                    languages = state.languages,
                    currentLanguage = state.operating.languageCode,
                    onDownload = actions.onDownload,
                    onDownloadAll = actions.onDownloadAll,
                )

            Destination.MODEL_SETUP ->
                ModelSetupScreen(
                    onContinue = { where = Destination.OPERATING },
                )

            Destination.LICENCES ->
                AboutScreen(
                    components = state.licences,
                    distributionNotice = state.distributionNotice,
                    buildLine = state.buildLine,
                    onOpenLicence = {
                        licence = it
                        where = Destination.LICENCE_TEXT
                    },
                )

            Destination.LICENCE_TEXT ->
                licence?.let { row ->
                    LicenceTextScreen(
                        title = row.licence,
                        text =
                            row.licenceFile?.let(actions.readLicence)
                                ?: "This licence text is not bundled with this build.",
                    )
                }

            Destination.OPERATING,
            Destination.MENU,
            Destination.UNIT_NAME,
            Destination.LOCATE,
            Destination.LOCATE_UNIT,
            Destination.TEXT_SIZE,
            -> Unit
        }
    }
}

/** Destinations whose board draws its own back header. */
private val OwnHeader =
    setOf(Destination.MENU, Destination.UNIT_NAME, Destination.LOCATE, Destination.LOCATE_UNIT, Destination.TEXT_SIZE)

/** One step towards the operating screen, wherever we are. */
private fun back(from: Destination): Destination =
    when (from) {
        Destination.OPERATING, Destination.MENU, Destination.LOCATE -> Destination.OPERATING
        Destination.LOCATE_UNIT -> Destination.LOCATE
        Destination.LICENCE_TEXT -> Destination.LICENCES
        else -> Destination.MENU
    }

/**
 * Tactical SubScreen Header matching Stitch styling.
 */
@Composable
private fun SubScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val p = palette
    Column(
        modifier
            .fillMaxSize()
            .background(p.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = Tokens.StatusBand)
                .background(p.surfaceContainerLowest)
                .border(Tokens.Hairline, p.hairline)
                .padding(horizontal = Tokens.ScreenMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .heightIn(min = Tokens.TouchTarget)
                    .width(Tokens.TouchTarget)
                    .clickable { onBack() }
                    .semantics { contentDescription = "Back from " + title },
                contentAlignment = Alignment.CenterStart,
            ) {
                Text("‹", fontSize = 32.sp, color = p.ink)
            }
            Text(
                text = title,
                fontSize = Tokens.Subtitle,
                fontWeight = FontWeight.Bold,
                color = p.ink,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        content()
    }
}
