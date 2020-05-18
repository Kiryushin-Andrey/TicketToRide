package ticketToRide

import kotlinx.collections.immutable.*
import kotlin.math.ceil
import kotlin.random.Random

fun createMapOfRussia(): GameMap {
    fun city(name: String, lat: Double, long: Double) = City(CityName(name), LatLong(lat, long))
    val cities = persistentListOf(
        city("Санкт-Петербург", 59.938732, 30.316229),
        city("Мурманск", 68.970665, 33.07497),
        city("Псков", 57.817398, 28.334368),
        city("Смоленск", 54.77897, 32.0471812),
        city("Курск", 51.739433, 36.179604),
        city("Москва", 55.7504461, 37.61749431),
        city("Архангельск", 64.543022, 40.537121),
        city("Вологда", 59.218876, 39.893276),
        city("Сыктывкар", 61.6685237, 50.8352024),
        city("Нарьян-Мар", 67.6380175, 53.0071044),
        city("Воркута", 67.494957, 64.0401),
        city("Ивдель", 60.6973287, 60.4172583),
        city("Салехард", 66.5375387, 66.6157469),
        city("Нижний Новгород", 56.328571, 44.003506),
        city("Рязань", 54.6295687, 39.7425039),
        city("Киров", 58.6035257, 49.6639029),
        city("Казань", 55.7823547, 49.1242266),
        city("Саратов", 51.530018, 46.034683),
        city("Самара", 53.198627, 50.113987),
        city("Уфа", 54.726288, 55.947727),
        city("Ижевск", 56.866557, 53.2094166),
        city("Кудымкар", 59.014606, 54.664135),
        city("Пермь", 58.014965, 56.246723),
        city("Екатеринбург", 56.839104, 60.60825),
        city("Оренбург", 51.767452, 55.097118),
        city("Воронеж", 51.6605982, 39.2005858),
        city("Ростов-на-Дону", 47.2213858, 39.7114196),
        city("Урюпинск", 50.7970972, 42.0051866),
        city("Волгоград", 48.7081906, 44.5153353),
        city("Ставрополь", 45.0433245, 41.9690934),
        city("Элиста", 46.306999, 44.270187),
        city("Астрахань", 46.3498308, 48.0326203),
        city("Краснодар", 45.0352566, 38.9764814),
        city("Владикавказ", 43.024593, 44.68211),
        city("Магнитогорск", 53.4242184, 58.983136),
        city("Челябинск", 55.1598408, 61.4025547),
        city("Пенза", 53.200001, 45.0),
        city("Петрозаводск", 61.790039, 34.390007),
        city("Кандалакша", 67.151442, 32.4130551),
        city("Махачкала", 42.9830241, 47.5048717),
        city("Тюмень", 57.153534, 65.542274),
        city("Тобольск", 58.1998048, 68.2512924),
        city("Ханты-Мансийск", 61.00346, 69.019157),
        city("Нижневартовск", 60.9339411, 76.5814274),
        city("Омск", 54.991375, 73.371529),
        city("Новосибирск", 55.0282171, 82.9234509),
        city("Лонгъюган", 64.7782522, 70.9559136),
        city("Томск", 56.488712, 84.952324),
        city("Новый Уренгой", 66.085196, 76.6799167),
        city("Сабетта", 71.2844523, 72.0468727)
    )

    fun segment(from: String, to: String, points: Int) =
        Segment(CityName(from), CityName(to), null, points)

    val segments = sequenceOf(
        segment("Санкт-Петербург", "Москва", 3),
        segment("Санкт-Петербург", "Петрозаводск", 2),
        segment("Санкт-Петербург", "Вологда", 3),
        segment("Мурманск", "Архангельск", 4),
        segment("Мурманск", "Нарьян-Мар", 6),
        segment("Псков", "Москва", 3),
        segment("Псков", "Санкт-Петербург", 1),
        segment("Псков", "Смоленск", 2),
        segment("Смоленск", "Москва", 3),
        segment("Смоленск", "Курск", 3),
        segment("Курск", "Воронеж", 1),
        segment("Курск", "Москва", 3),
        segment("Москва", "Вологда", 2),
        segment("Москва", "Нижний Новгород", 2),
        segment("Москва", "Воронеж", 3),
        segment("Москва", "Рязань", 1),
        segment("Вологда", "Архангельск", 4),
        segment("Вологда", "Сыктывкар", 3),
        segment("Вологда", "Нижний Новгород", 2),
        segment("Сыктывкар", "Нарьян-Мар", 4),
        segment("Сыктывкар", "Воркута", 6),
        segment("Воркута", "Салехард", 1),
        segment("Ивдель", "Салехард", 6),
        segment("Ивдель", "Ханты-Мансийск", 4),
        segment("Нижний Новгород", "Казань", 2),
        segment("Нижний Новгород", "Киров", 3),
        segment("Нижний Новгород", "Пенза", 2),
        segment("Рязань", "Пенза", 2),
        segment("Киров", "Сыктывкар", 3),
        segment("Киров", "Пермь", 3),
        segment("Казань", "Киров", 2),
        segment("Казань", "Уфа", 3),
        segment("Казань", "Ижевск", 2),
        segment("Казань", "Самара", 2),
        segment("Саратов", "Самара", 2),
        segment("Самара", "Оренбург", 3),
        segment("Уфа", "Екатеринбург", 3),
        segment("Уфа", "Челябинск", 2),
        segment("Уфа", "Магнитогорск", 1),
        segment("Уфа", "Оренбург", 2),
        segment("Ижевск", "Пермь", 2),
        segment("Кудымкар", "Пермь", 1),
        segment("Кудымкар", "Ивдель", 3),
        segment("Пермь", "Екатеринбург", 2),
        segment("Екатеринбург", "Ивдель", 3),
        segment("Екатеринбург", "Челябинск", 1),
        segment("Воронеж", "Урюпинск", 1),
        segment("Воронеж", "Ростов-на-Дону", 3),
        segment("Ростов-на-Дону", "Волгоград", 2),
        segment("Ростов-на-Дону", "Ставрополь", 2),
        segment("Урюпинск", "Волгоград", 2),
        segment("Волгоград", "Астрахань", 2),
        segment("Волгоград", "Саратов", 2),
        segment("Ставрополь", "Элиста", 2),
        segment("Элиста", "Астрахань", 2),
        segment("Элиста", "Волгоград", 2),
        segment("Элиста", "Махачкала", 3),
        segment("Краснодар", "Ростов-на-Дону", 1),
        segment("Краснодар", "Ставрополь", 1),
        segment("Краснодар", "Владикавказ", 3),
        segment("Владикавказ", "Махачкала", 1),
        segment("Магнитогорск", "Челябинск", 2),
        segment("Челябинск", "Тюмень", 2),
        segment("Челябинск", "Омск", 4),
        segment("Пенза", "Самара", 2),
        segment("Пенза", "Саратов", 1),
        segment("Петрозаводск", "Вологда", 3),
        segment("Кандалакша", "Петрозаводск", 4),
        segment("Кандалакша", "Мурманск", 2),
        segment("Кандалакша", "Архангельск", 4),
        segment("Махачкала", "Астрахань", 3),
        segment("Тюмень", "Омск", 3),
        segment("Тюмень", "Тобольск", 1),
        segment("Тобольск", "Омск", 3),
        segment("Тобольск", "Ханты-Мансийск", 3),
        segment("Ханты-Мансийск", "Нижневартовск", 2),
        segment("Нижневартовск", "Томск", 3),
        segment("Нижневартовск", "Новый Уренгой", 4),
        segment("Омск", "Новосибирск", 4),
        segment("Новосибирск", "Томск", 1),
        segment("Лонгъюган", "Салехард", 2),
        segment("Лонгъюган", "Ханты-Мансийск", 3),
        segment("Лонгъюган", "Новый Уренгой", 3),
        segment("Сабетта", "Салехард", 4),
        segment("Сабетта", "Мурманск", 8)
    )

    val segmentsByCities =
        segments.flatMap { sequenceOf(it.from to it, it.to to it) }.groupBy({ it.first }) { it.second }

    val totalLength = segments.filter { it.length <= 4 }.sumBy { it.length }
    val countPerColor = ceil(totalLength.toDouble() / CardColor.values().size).toInt()
    val usedByColor = mutableMapOf<CardColor, Int>()

    val colorMap = mutableMapOf<Segment, CardColor?>()
    for (segment in segments.sortedByDescending { it.length }.dropWhile { it.length > 4 }) {
        val takenColors = sequenceOf(segment.from, segment.to)
            .flatMap { segmentsByCities[it]?.asSequence() ?: throw Error("City ${it.value} not found in map") }
            .mapNotNull { colorMap[it] }
            .toSet()

        val next = CardColor.values().asSequence()
            .filter { !takenColors.contains(it) && (usedByColor[it] ?: 0) + segment.length <= countPerColor }
            .flatMap { color -> sequence { repeat(countPerColor - usedByColor.getOrElse(color, { 0 })) { yield(color) } } }
            .let { seq ->
                val until = seq.count() - 1
                if (until > 0) seq.drop(Random.nextInt(until)).first()
                else CardColor.values().random()
            }
        colorMap[segment] = next
        usedByColor[next] = (usedByColor[next] ?: 0) + 1
    }

    return GameMap(
        cities,
        segments.map { Segment(it.from, it.to, colorMap[it], it.length) }.toList().toPersistentList(),
        LatLong(57.6012967, 40.4744424),
        4
    )
}