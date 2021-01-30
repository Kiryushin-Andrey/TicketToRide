package ticketToRide.components.map

enum class MapTilesProvider(val provider: (x: Number, y: Number, z: Number, dpr: Number) -> String) {
    Terrain({ x, y, z, _ -> "http://c.tile.stamen.com/terrain/$z/$x/$y.jpg"}),
    Watermark({ x, y, z, _ -> "http://c.tile.stamen.com/watercolor/$z/$x/$y.jpg"})
}