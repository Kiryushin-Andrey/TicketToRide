package ticketToRide.components.welcomeScreen

import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.dialog.mDialog
import com.ccfraser.muirwik.components.expansionpanel.*
import com.ccfraser.muirwik.components.list.mList
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.list.mListItem
import kotlinx.css.*
import react.*
import styled.css
import ticketToRide.*

class GameMapParseErrorsDialog : RComponent<GameMapParseErrorsDialog.Props, RState>() {

    interface Props : RProps {
        var locale: Locale
        var filename: String
        var errors: List<GameMapParseError>
        var onClose: () -> Unit
    }

    override fun RBuilder.render() {
        mDialog {
            attrs {
                open = true
                fullScreen = true
            }
            mAppBar(MColor.secondary, MAppBarPosition.sticky) {
                mToolbar {
                    mTypography(str.header(props.filename), MTypographyVariant.h6) {
                        css { flexGrow = 1.0 }
                    }
                    mIconButton("close") {
                        attrs {
                            onClick = { props.onClose() }
                        }
                    }
                }
            }
            mList {
                props.errors.groupBy { it::class }.forEach { (_, errors) ->
                    mExpansionPanel {

                        mExpansionPanelSummary {
                            attrs {
                                expandIcon = buildElement { mIcon("expand_more") }!!
                                css {
                                    backgroundColor = Color.lightGoldenrodYellow
                                }
                            }
                            mTypography(str.errorDescription(errors.first()), MTypographyVariant.h6)
                        }

                        mExpansionPanelDetails {
                            attrs {
                                css { display = Display.block }
                            }
                            when (errors.first()) {
                                is GameMapParseError.MapCenter.Missing -> {
                                    mTypography(str.mapCenterMissingDetails, MTypographyVariant.body2)
                                    mTypography("map-center: 57.6012967 40.4744424", MTypographyVariant.body2) {
                                        attrs { css { fontWeight = FontWeight.bold } }
                                    }
                                }
                                is GameMapParseError.MapZoom.Missing -> {
                                    mTypography(str.mapZoomMissingDetails, MTypographyVariant.body2)
                                    mTypography("map-zoom: 4", MTypographyVariant.body2) {
                                        attrs { css { fontWeight = FontWeight.bold } }
                                    }
                                }
                                else -> {
                                    mList(true) {
                                        val maxLines = 20
                                        errors.take(maxLines).forEach {
                                            mListItem(it.line.trimToMaxLength(250), str.lineNumber(it.lineNumber))
                                        }
                                        if (errors.size > maxLines) {
                                            mListItem(str.andMoreLines(errors.size - maxLines))
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

    fun String.trimToMaxLength(maxLength: Int) = if (length > maxLength) substring(0..maxLength) + "..." else this

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()
}

fun RBuilder.gameMapParseErrorsDialog(builder: GameMapParseErrorsDialog.Props.() -> Unit) {
    child(GameMapParseErrorsDialog::class) {
        attrs(builder)
    }
}