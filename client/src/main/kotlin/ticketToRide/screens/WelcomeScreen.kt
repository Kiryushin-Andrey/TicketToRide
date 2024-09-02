package ticketToRide.screens

import csstype.*
import emotion.react.css
import js.core.jso
import kotlinx.browser.window
import kotlinx.coroutines.*
import mui.icons.material.Settings
import mui.material.*
import mui.material.styles.PaletteColor
import mui.material.styles.ThemeProvider
import mui.material.styles.TypographyVariant
import mui.material.styles.createTheme
import mui.system.sx
import org.w3c.notifications.*
import react.*
import react.dom.html.ReactHTML.div
import react.dom.onChange
import ticketToRide.*
import ticketToRide.components.welcomeScreen.*

external interface WelcomeScreenProps : Props {
    var locale: Locale
    var onLocaleChanged: (Locale) -> Unit
    var onStartGame: (ConnectRequest.StartGameMap, PlayerName, PlayerColor, Int, Boolean) -> Unit
    var onJoinGame: (GameId, PlayerName, PlayerColor) -> Unit
    var onJoinAsObserver: (GameId) -> Unit
    var onReconnect: (GameId, PlayerName) -> Unit
}

data class WelcomeScreenState(
    val playerName: String = "",
    val playerColor: PlayerColor? = PlayerColor.values().first(),
    val otherPlayers: List<PlayerView> = emptyList(),
    val errorText: String? = null,
    val showSettings: Boolean = false,
    val carsNumber: Int = 45,
    val calculateScoresInProcess: Boolean = false,
    val joinAsObserver: Boolean = false,
    val map: ConnectRequest.StartGameMap? = ConnectRequest.StartGameMap.BuiltIn(listOf("default")),
    val gameMapParseErrors: CustomGameMapParseErrors? = null,
    val weatherConditions: List<WeatherCondition> = emptyList()
)

data class WeatherCondition(val city: String, val temperature: Double, val description: String)

val WelcomeScreenState.availableColors get() =
    otherPlayers.find { it.name.value == playerName }
        ?.let {
            // if a player is reconnecting back into the game under the same name
            // she should keep the same color as before
            listOf(it.color)
        }
        ?: PlayerColor.values().filter { c -> !otherPlayers.map { it.color }.contains(c) }

val gameId = window.location.pathname
    .takeIf { it.startsWith("/game/") }
    ?.let { GameId(it.substringAfterLast('/')) }

val startingNewGame = gameId == null

val WelcomeScreen = FC<WelcomeScreenProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val (state, setState) = useState(WelcomeScreenState())

    val processGameStateUpdate = useCallback(state, setState) { gameState: GameStateForObserver ->
        setState(
            state.copy(
                otherPlayers = gameState.players,
                playerColor = if (state.otherPlayers.map { it.color }.contains(state.playerColor))
                    state.availableColors.firstOrNull()
                else
                    state.playerColor
            )
        )
    }

    useEffect(*emptyArray()) {
        if (gameId != null) {
            val scope = CoroutineScope(Dispatchers.Default + Job())
            val peekPlayersConnection = ServerConnection(scope, gameId.webSocketUrl, GameStateForObserver.serializer()) {
                val response = connect(ConnectRequest.Observe)
                if (response is ConnectResponse.ObserverConnected) {
                    processGameStateUpdate(response.state)
                    responses().collect(processGameStateUpdate)
                }
            }
            cleanup {
                peekPlayersConnection.close()
            }
        }
    }

    useEffect(*emptyArray()) {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.launch {
            val response = window.fetch("https://api.openweathermap.org/data/2.5/group?id=2643743,2950159,2968815,2988507,3117735&units=metric&appid=YOUR_API_KEY")
            if (response.ok) {
                val data = response.json().await()
                val weatherList = data.asDynamic().list as Array<dynamic>
                val weatherConditions = weatherList.map {
                    WeatherCondition(
                        city = it.name as String,
                        temperature = it.main.temp as Double,
                        description = it.weather[0].description as String
                    )
                }
                setState(state.copy(weatherConditions = weatherConditions))
            }
        }
        cleanup {
            scope.cancel()
        }
    }

    Dialog {
        sx {
            width = 100.pct
            margin = 0.px
        }
        open = state.gameMapParseErrors == null
        maxWidth = "sm"
        fullWidth = true

        DialogContent {
            if (!state.joinAsObserver) {
                playerName(gameId, props, state, setState, str)
                playerColor(state, setState, str)
            }
            if (startingNewGame) {
                chooseGameMap(state, setState, str)
            } else {
                joinAsObserver(state, setState, str)
            }
            if (state.showSettings) {
                settings(state, setState, startingNewGame, str)
            }
            if (Notification.permission == NotificationPermission.DEFAULT) {
                Typography {
                    variant = TypographyVariant.body1
                    +str.notificationNote
                }
            }
            weatherConditions(state.weatherConditions)
        }
        DialogActions {
            if (startingNewGame) {
                Tooltip {
                    title = ReactNode(str.settings)

                    IconButton {
                        onClick = { setState(state.copy(showSettings = !state.showSettings)) }
                        Settings()
                    }
                }
            }

            Select {
                variant = SelectVariant.standard
                sx {
                    marginLeft = 15.px
                    marginRight = 15.px
                }

                value = props.locale.toString()
                onChange = { e, _ ->
                    val value = e.target.asDynamic().value as String
                    props.onLocaleChanged(Locale.valueOf(value))
                }
                Locale.values().forEach {
                    MenuItem {
                        value = it.name
                        selected = props.locale == it
                        +it.name
                    }
                }
            }

            Button {
                +(if (startingNewGame) str.startGame else str.joinGame)

                color = ButtonColor.primary
                variant = ButtonVariant.contained
                disabled = state.errorText != null
                onClick = { proceed(gameId, props, state, setState, str) }
            }
        }
    }

    state.gameMapParseErrors?.let {
        GameMapParseErrorsDialog {
            locale = props.locale
            filename = it.filename
            errors = it.errors
            onClose = { setState(state.copy(gameMapParseErrors = null)) }
        }
    }
}

private fun ChildrenBuilder.playerName(gameId: GameId?, props: WelcomeScreenProps, state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    TextField {
        placeholder = str.yourName
        fullWidth = true
        error = state.errorText != null
        helperText = ReactNode(state.errorText ?: "")
        autoFocus = true
        onChange = { e ->
            val value = e.target.asDynamic().value.unsafeCast<String>()
            setState {
                state.copy(
                    playerName = value,
                    errorText = if (value.isBlank()) str.enterYourName else null,
                    playerColor = state.otherPlayers.find { it.name.value == value }?.color ?: state.playerColor
                )
            }
        }
        onKeyDown = { e ->
            if (e.key == "Enter") {
                val text = e.target.asDynamic().value.unsafeCast<String>()
                val newState = state.copy(playerName = text)
                setState(newState)
                proceed(gameId, props, newState, setState, str)
            }
        }
    }
}

val radioButtonThemes = PlayerColor.values().associateWith {
    createTheme(
        jso {
            palette = jso {
                primary = jso<PaletteColor> { main = Color(it.rgb) }
                text = jso { secondary = Color(it.rgb) }
            }
        }
    )
}

private fun ChildrenBuilder.playerColor(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    RadioGroup {
        sx {
            alignItems = AlignItems.center
        }

        value = state.playerColor?.name
        row = true
        onChange = { _, newValue ->
            setState(state.copy(playerColor = PlayerColor.valueOf(newValue)))
        }

        InputLabel {
            +str.yourColor
        }
        state.availableColors.forEach {
            ThemeProvider {
                theme = radioButtonThemes[it]
                Radio { value = it.name }
            }
        }
    }
}

private fun ChildrenBuilder.settings(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, startingNewGame: Boolean, str: WelcomeScreenStrings) {
    Paper {
        sx {
            marginTop = 15.px
            padding = 10.px
        }
        elevation = 2
        Typography {
            variant = TypographyVariant.h5
            +str.settings
        }
        if (startingNewGame) {
            initialCarsOnHand(state, setState, str)
            calculateScoresInProcess(state, setState, str)
        }
    }
}

private fun ChildrenBuilder.chooseGameMap(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    ChooseGameMapComponent {
        locale = str.locale
        map = state.map
        onMapChanged = { map -> setState(state.copy(map = map)) }
        onShowParseErrors = { err -> setState(state.copy(gameMapParseErrors = err)) }
    }
}

private fun ChildrenBuilder.initialCarsOnHand(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    InputLabel {
        sx {
            marginTop = 15.px
            color = NamedColor.black
        }

        +str.numberOfCarsOnHand
        Input {
            sx {
                marginLeft = 10.px
                width = 40.px
            }
            type = "number"
            value = state.carsNumber.toString()
            asDynamic().min = 5
            asDynamic().max = 60
            onChange = { e ->
                e.target.asDynamic().value.unsafeCast<String>().toIntOrNull()?.let {
                    setState(state.copy(carsNumber = it))
                }
            }
        }
    }
}

private fun ChildrenBuilder.calculateScoresInProcess(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    FormControlLabel {
        label = ReactNode(str.calculateScoresInProcess)
        checked = state.calculateScoresInProcess
        control = Checkbox.create {
            checked = state.calculateScoresInProcess
            color = CheckboxColor.primary
            onChange = { _, value -> setState(state.copy(calculateScoresInProcess = value)) }
        }
    }
}

private fun ChildrenBuilder.joinAsObserver(state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    FormControlLabel {
        label = ReactNode(str.joinAsObserver)
        checked = state.joinAsObserver
        control = Checkbox.create {
            checked = state.joinAsObserver
            color = CheckboxColor.primary
            onChange = { _, value -> setState(state.copy(joinAsObserver = value)) }
        }
    }
}

private fun proceed(gameId: GameId?, props: WelcomeScreenProps, state: WelcomeScreenState, setState: StateSetter<WelcomeScreenState>, str: WelcomeScreenStrings) {
    if (state.joinAsObserver && gameId != null) {
        props.onJoinAsObserver(gameId)
        return
    }

    if (state.playerName.isBlank()) {
        setState(state.copy(errorText = str.enterYourName))
        return
    }
    if (state.playerColor == null) {
        setState(state.copy(errorText = str.chooseYourColor))
        return
    }
    if (state.map == null) {
        setState(state.copy(errorText = str.chooseGameMap))
        return
    }
    Notification.requestPermission()
    val playerName = PlayerName(state.playerName)
    when {
        gameId == null -> {
            props.onStartGame(
                state.map,
                playerName,
                state.playerColor,
                state.carsNumber,
                state.calculateScoresInProcess
            )
        }

        state.otherPlayers.any { it.name == playerName } ->
            props.onReconnect(gameId, playerName)

        else ->
            props.onJoinGame(gameId, playerName, state.playerColor)
    }
}

private fun ChildrenBuilder.weatherConditions(weatherConditions: List<WeatherCondition>) {
    if (weatherConditions.isNotEmpty()) {
        div {
            css {
                marginTop = 20.px
            }
            Typography {
                variant = TypographyVariant.h6
                +"Current Weather Conditions in Major European Capitals"
            }
            weatherConditions.forEach { condition ->
                Typography {
                    variant = TypographyVariant.body1
                    +"${condition.city}: ${condition.temperature}°C, ${condition.description}"
                }
            }
        }
    }
}

private class WelcomeScreenStrings(val locale: Locale) : LocalizedStrings({ locale }) {

    val yourName by loc(
        Locale.En to "Your name is",
        Locale.Ru to "Ваше имя"
    )

    val enterYourName by loc(
        Locale.En to "Enter your name",
        Locale.Ru to "Введите ваше имя"
    )

    val yourColor by loc(
        Locale.En to "Your color",
        Locale.Ru to "Ваш цвет"
    )

    val chooseYourColor by loc(
        Locale.En to "Choose your color",
        Locale.Ru to "Выберите ваш цвет"
    )

    val chooseGameMap by loc(
        Locale.En to "Choose game map",
        Locale.Ru to "Выберите карту для игры"
    )

    val settings by loc(
        Locale.En to "Settings",
        Locale.Ru to "Настройки"
    )

    val numberOfCarsOnHand by loc(
        Locale.En to "Initial number of cars on hand",
        Locale.Ru to "Количество вагонов в начале игры"
    )

    val calculateScoresInProcess by loc(
        Locale.En to "Calculate scores during the game",
        Locale.Ru to "Подсчет очков в процессе игры"
    )

    val joinAsObserver by loc(
        Locale.En to "I'm an observer",
        Locale.Ru to "Наблюдать за игрой"
    )

    val notificationNote by loc(
        Locale.En to "Allow notifications to be notified when it's your turn to move even if the browser tab is inactive",
        Locale.Ru to "Уведомления подскажут, когда до вас дошла очередь ходить"
    )

    val startGame by loc(
        Locale.En to "Start game!",
        Locale.Ru to "Начать игру!"
    )

    val joinGame by loc(
        Locale.En to "Join game!",
        Locale.Ru to "Присоединиться к игре!"
    )
}

private fun strings(locale: Locale) = WelcomeScreenStrings(locale)
