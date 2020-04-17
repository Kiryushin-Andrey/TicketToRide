package ticketToRide

object RussiaMap {
    val mapCenter = LatLong(57.6012967,40.4744424)
    const val mapZoom = 4
    val cities = listOf(
        City("Москва", LatLong(55.7504461, 37.61749431),
            listOf(
                Route("Санкт-Петербург", Color.GREEN, 3),
                Route("Псков", Color.RED, 3),
                Route("Вологда", Color.BLACK, 2),
                Route("Нижний Новгород", Color.WHITE, 2),
                Route("Воронеж", Color.BLUE, 3),
                Route("Рязань", Color.MAGENTO, 1),
                Route("Курск", Color.YELLOW, 3)
            )),
        City("Санкт-Петербург", LatLong(59.938732, 30.316229),
            listOf(
                Route("Псков", Color.ORANGE, 1),
                Route("Петрозаводск", Color.MAGENTO, 2)
            )),
        City("Мурманск", LatLong(68.970665, 33.07497),
            listOf(
                Route("Архангельск", Color.BLACK, 4, 1),
                Route("Нарьян-Мар", null, 6, 2)
            )),
        City("Архангельск", LatLong(64.543022, 40.537121),
            listOf(
                Route("Вологда", Color.ORANGE, 4)
            )),
        City("Псков", LatLong(57.817398, 28.334368)),
        City("Вологда", LatLong(59.218876, 39.893276),
            listOf(
                Route("Сыктывкар", Color.BLUE, 3),
                Route("Нижний Новгород", Color.MAGENTO, 2),
                Route("Санкт-Петербург", Color.WHITE, 3),
                Route("Петрозаводск", Color.GREEN, 3)
            )),
        City("Смоленск", LatLong(54.77897, 32.0471812),
            listOf(
                Route("Псков", Color.BLACK, 2),
                Route("Москва", Color.MAGENTO, 3),
                Route("Курск", Color.RED, 3)
            )),
        City("Сыктывкар", LatLong(61.6685237, 50.8352024),
            listOf(
                Route("Нарьян-Мар", Color.YELLOW, 4)
            )),
        City("Казань", LatLong(55.7823547, 49.1242266),
            listOf(
                Route("Нижний Новгород", Color.ORANGE, 2),
                Route("Киров", Color.RED, 2),
                Route("Уфа", Color.BLUE, 3),
                Route("Ижевск", Color.BLACK, 2),
                Route("Самара", Color.WHITE, 2)
            )),
        City("Самара", LatLong(53.198627, 50.113987),
            listOf(
                Route("Саратов", Color.YELLOW, 3),
                Route("Оренбург", Color.MAGENTO, 3)
            )),
        City("Пермь", LatLong(58.014965, 56.246723),
            listOf(
                Route("Екатеринбург", Color.MAGENTO, 2),
                Route("Ижевск", Color.WHITE, 2),
                Route("Кудымкар", Color.GREEN, 1)
            )),
        City("Екатеринбург", LatLong(56.839104, 60.60825),
            listOf(
                Route("Ивдель", Color.ORANGE, 3),
                Route("Челябинск", Color.RED, 1)
            )),
        City("Уфа", LatLong(54.726288, 55.947727),
            listOf(
                Route("Екатеринбург", Color.RED, 3),
                Route("Челябинск", Color.BLACK, 2),
                Route("Магнитогорск", Color.GREEN, 1),
                Route("Оренбург", Color.ORANGE, 2)
            )),
        City("Оренбург", LatLong(51.767452, 55.097118)),
        City("Саратов", LatLong(51.530018, 46.034683)),
        City("Воронеж", LatLong(51.6605982, 39.2005858),
            listOf(
                Route("Урюпинск", Color.YELLOW, 1),
                Route("Курск", Color.GREEN, 1),
                Route("Ростов-на-Дону", Color.MAGENTO, 3)
            )),
        City("Киров", LatLong(58.6035257, 49.6639029),
            listOf(
                Route("Сыктывкар", Color.BLACK, 3),
                Route("Нижний Новгород", Color.GREEN, 3),
                Route("Пермь", Color.YELLOW, 3)
            )),
        City("Волгоград", LatLong(48.7081906, 44.5153353),
            listOf(
                Route("Астрахань", Color.BLACK, 3),
                Route("Урюпинск", Color.RED, 2),
                Route("Саратов", Color.MAGENTO, 2),
                Route("Ростов-на-Дону", Color.GREEN, 2)
            )),
        City("Астрахань", LatLong(46.3498308, 48.0326203),
            listOf(
                Route("Элиста", Color.ORANGE, 2)
            )),
        City("Ростов-на-Дону", LatLong(47.2213858, 39.7114196),
            listOf(
                Route("Краснодар", Color.BLUE, 1)
            )),
        City("Краснодар", LatLong(45.0352566, 38.9764814),
            listOf(
                Route("Владикавказ", Color.WHITE, 3)
            )),
        City("Владикавказ", LatLong(43.024593, 44.68211),
            listOf(
                Route("Махачкала", Color.YELLOW, 2)
            )),
        City("Нарьян-Мар", LatLong(67.6380175, 53.0071044)),
        City("Воркута", LatLong(67.494957, 64.0401),
            listOf(
                Route("Салехард", Color.YELLOW, 1),
                Route("Сыктывкар", null, 6)
            )),
        City("Магнитогорск", LatLong(53.4242184, 58.983136)),
        City("Нижний Новгород", LatLong(56.328571, 44.003506)),
        City("Пенза", LatLong(53.200001, 45),
            listOf(
                Route("Рязань", Color.RED, 3),
                Route("Самара", Color.ORANGE, 2),
                Route("Саратов", Color.BLACK, 1),
                Route("Нижний Новгород", Color.BLUE, 2)
            )),
        City("Курск", LatLong(51.739433, 36.179604)),
        City("Петрозаводск", LatLong(61.790039, 34.390007)),
        City("Кандалакша", LatLong(67.151442, 32.4130551),
            listOf(
                Route("Петрозаводск", Color.BLUE, 4),
                Route("Мурманск", Color.GREEN, 2),
                Route("Архангельск", Color.YELLOW, 4, 1)
            )),
        City("Челябинск", LatLong(55.1598408, 61.4025547),
            listOf(
                Route("Магнитогорск", Color.YELLOW, 2)
            )),
        City("Ставрополь", LatLong(45.0433245, 41.9690934),
            listOf(
                Route("Элиста", Color.RED, 2),
                Route("Краснодар", Color.YELLOW, 1),
                Route("Ростов-на-Дону", Color.ORANGE, 2)
            )),
        City("Махачкала", LatLong(42.9830241, 47.5048717),
            listOf(
                Route("Элиста", Color.MAGENTO, 3)
            )),
        City("Элиста", LatLong(46.306999, 44.270187)),
        City("Рязань", LatLong(54.6295687, 39.7425039)),
        City("Ижевск", LatLong(56.866557, 53.2094166)),
        City("Урюпинск", LatLong(50.7970972, 42.0051866)),
        City("Кудымкар", LatLong(59.014606, 54.664135),
            listOf(
                Route("Ивдель", Color.WHITE, 3)
            )),
        City("Ивдель", LatLong(60.6973287, 60.4172583),
            listOf(
                Route("Салехард", Color.MAGENTO, 4)
            )),
        City("Салехард", LatLong(66.5375387, 66.6157469))
    )
}