package ticketToRide.components.map

enum class MapTilesProvider(val provider: (x: Number, y: Number, z: Number, dpr: Number) -> String) {
    Terrain({ x, y, z, _ -> "https://tiles.stadiamaps.com/tiles/stamen_terrain/$z/$x/$y.jpg" }),
    Watermark({ x, y, z, _ -> "https://tiles.stadiamaps.com/tiles/stamen_watercolor/$z/$x/$y.jpg" })
}