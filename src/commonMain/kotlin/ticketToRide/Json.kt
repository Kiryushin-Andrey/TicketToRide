package ticketToRide

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

val json = Json(JsonConfiguration.Default.copy(allowStructuredMapKeys = true))
