package ticketToRide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class MapsTreeItem {

    @Serializable
    @SerialName("map")
    class Map(val name: String) : MapsTreeItem()

    @Serializable
    @SerialName("folder")
    class Folder(val name: String, val children: List<MapsTreeItem>) : MapsTreeItem()
}
