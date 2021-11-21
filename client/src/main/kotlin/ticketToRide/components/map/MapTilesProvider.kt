package ticketToRide.components.map

enum class MapTilesProvider(val provider: (x: Number, y: Number, z: Number, dpr: Number) -> String) {
    Terrain({ x, y, z, _ -> "https://stamen-tiles.a.ssl.fastly.net/terrain/$z/$x/$y.jpg"}),
    Watermark({ x, y, z, _ -> "https://stamen-tiles.a.ssl.fastly.net/watercolor/$z/$x/$y.jpg"})
}