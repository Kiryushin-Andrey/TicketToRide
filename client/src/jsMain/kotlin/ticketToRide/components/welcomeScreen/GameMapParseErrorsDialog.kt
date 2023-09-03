package ticketToRide.components.welcomeScreen

import csstype.Display
import csstype.FontWeight
import csstype.NamedColor
import csstype.number
import emotion.react.css
import mui.icons.material.Close
import mui.icons.material.ExpandMore
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import ticketToRide.GameMapParseError
import ticketToRide.GameMapPropertyNames
import ticketToRide.Locale
import ticketToRide.LocalizedStrings

external interface GameMapParseErrorsDialogProps : Props {
    var locale: Locale
    var filename: String
    var errors: List<GameMapParseError>
    var onClose: () -> Unit
}

val GameMapParseErrorsDialog = FC<GameMapParseErrorsDialogProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }

    Dialog {
        open = true
        fullScreen = true

        AppBar {
            color = AppBarColor.secondary
            position = AppBarPosition.sticky
            Toolbar {
                Typography {
                    variant = TypographyVariant.h6
                    sx { flexGrow = number(1.0) }
                    +str.header(props.filename)
                }
                IconButton {
                    Close()
                    onClick = { props.onClose() }
                }
            }
        }
        List {
            props.errors.groupBy { it::class }.forEach { (_, errors) ->
                Accordion {

                    AccordionSummary {
                        expandIcon = Icon.create { ExpandMore }
                        sx {
                            backgroundColor = NamedColor.lightgoldenrodyellow
                        }
                        Typography {
                            variant = TypographyVariant.h6
                            +str.errorDescription(errors.first())
                        }
                    }

                    AccordionDetails {
                        sx { display = Display.block }

                        when (errors.first()) {
                            is GameMapParseError.MapCenter.Missing -> {
                                Typography {
                                    variant = TypographyVariant.body2
                                    +str.mapCenterMissingDetails
                                }
                                Typography {
                                    variant = TypographyVariant.body2
                                    sx { fontWeight = FontWeight.bold }
                                    +"map-center: 57.6012967 40.4744424"
                                }
                            }

                            is GameMapParseError.MapZoom.Missing -> {
                                Typography {
                                    variant = TypographyVariant.body2
                                    +str.mapZoomMissingDetails
                                }
                                Typography {
                                    variant = TypographyVariant.body2
                                    sx { fontWeight = FontWeight.bold }
                                    +"map-zoom: 4"
                                }
                            }

                            else -> {
                                List {
                                    dense = true

                                    val maxLines = 20
                                    errors.take(maxLines).forEach {
                                        ListItem {
                                            ListItemText {
                                                primary = ReactNode(it.line.trimToMaxLength(250))
                                                secondary = ReactNode(str.lineNumber(it.lineNumber))
                                            }
                                        }
                                    }
                                    if (errors.size > maxLines) {
                                        ListItem {
                                            ListItemText {
                                                +str.andMoreLines(errors.size - maxLines)
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
    }
}

private fun String.trimToMaxLength(maxLength: Int) =
    if (length > maxLength) substring(0..maxLength) + "..." else this

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val header by locWithParam<String>(
        Locale.En to { filename -> "Errors in \"$filename\" map file" },
        Locale.Ru to { filename -> "Ошибки в файле карты \"$filename\"" }
    )

    val errorDescription by locWithParam<GameMapParseError>(
        Locale.En to { err ->
            when (err) {
                is GameMapParseError.MapZoom.Missing ->
                    "map-center property is missing - should be \"map-center: latitude; longitude\""
                is GameMapParseError.MapZoom.BadFormat ->
                    "map-center property value has bad format - should be \"latitude; longitude\""
                is GameMapParseError.MapCenter.Missing ->
                    "map-zoom property is missing - should be \"map-zoom: 3..10\""
                is GameMapParseError.MapCenter.BadFormat ->
                    "map-zoom property has bad format - expected number between 3 and 10"
                is GameMapParseError.UnexpectedRoute ->
                    "Unexpected route (line starting with '-') encountered - routes can only follow cities"
                is GameMapParseError.City.Unknown ->
                    "City is not present in file but is mentioned in the route"
                is GameMapParseError.City.BadFormat ->
                    "City description should consist of three semicolon-delimited parts - \"name; latitude; longitude\""
                is GameMapParseError.City.InvalidLatLong ->
                    "Failed to read city latitude and longitude - should be \"name; latitude; longitude\""
                is GameMapParseError.BadRouteFormat ->
                    "Route description should contain target city name and route length: \"- toCity; length\""
                is GameMapParseError.BadPropertyFormat ->
                    "Failed to read property value"
                is GameMapParseError.UnknownProperty ->
                    "Unknown property (a line of \"name: value\" format). The following properties are recognized: ${GameMapPropertyNames.all.joinToString(
                        ","
                    )}"
            }
        },
        Locale.Ru to { err ->
            when (err) {
                is GameMapParseError.MapZoom.Missing ->
                    "Пропущено свойство map-center - строка вида \"map-center: широта; долгота\""
                is GameMapParseError.MapZoom.BadFormat ->
                    "Неправильно указано значение map-center - должно быть \"широта; долгота\""
                is GameMapParseError.MapCenter.Missing ->
                    "Пропущено свойство map-zoom - строка вида \"map-zoom: 3..10\""
                is GameMapParseError.MapCenter.BadFormat ->
                    "Неправильно указано значение map-zoom - должно быть число от 3 до 10"
                is GameMapParseError.UnexpectedRoute ->
                    "Описание перегона (строка, начинающаяся с '-') должно идти следом за описанием города"
                is GameMapParseError.City.Unknown ->
                    "Город не описан отдельной строкой в файле, но встречается в маршруте"
                is GameMapParseError.City.BadFormat ->
                    "Строка города должна состоять из трех частей, отделенных точкой с запятой - \"Город; широта; долгота\""
                is GameMapParseError.City.InvalidLatLong ->
                    "Не удалось прочитать долготу и/или широту города - должно быть \"Город; широта; долгота\""
                is GameMapParseError.BadRouteFormat ->
                    "Описание перегона должно содержать имя города и длину перегона: \"- Город; Длина\""
                is GameMapParseError.BadPropertyFormat ->
                    "Не удалось прочитать значение свойства"
                is GameMapParseError.UnknownProperty ->
                    "Неизвестное свойство (строка формата \"имя: значение\"). Файл может содержать следующие свойства: ${GameMapPropertyNames.all.joinToString(
                        ","
                    )}"
            }
        }
    )

    val mapCenterMissingDetails by loc(
        Locale.En to "Map file should have a line specifying the center of the map (by the means of lat and long) when it is first opened. This line should look like this: ",
        Locale.Ru to "Файл с картой должен содержать строку с широтой и долготой центра карты. Эта строка должна выглядеть так: "
    )

    val mapZoomMissingDetails by loc(
        Locale.En to "Map file should have a line specifying the default zoom (an integer somewhat between 3 an 10) of the map when it is first opened. This line should look like this: ",
        Locale.Ru to "Файл с картой должен содержать строку с масштабом карты при открытии (это целое число, обычно в пределах между 3 и 10). Эта строка должна выглядеть так: "
    )

    val lineNumber by locWithParam<Int>(
        Locale.En to { n -> "Line $n" },
        Locale.Ru to { n -> "Строка $n" }
    )

    val andMoreLines by locWithParam<Int>(
        Locale.En to { n -> "... and $n more similar problems" },
        Locale.Ru to { n -> "... и еще $n строк с этой ошибкой" }
    )
}
