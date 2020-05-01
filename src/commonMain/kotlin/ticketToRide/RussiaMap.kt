package ticketToRide

object RussiaMap {
    val mapCenter = LatLong(57.6012967,40.4744424)
    const val mapZoom = 4
    const val longTicketMinPoints = 20
    val shortTicketsPointsRange = 5 to 12
    val cities = listOf(
        City("Санкт-Петербург", LatLong(59.938732, 30.316229),
            listOf(
                Route("Москва", CardColor.GREEN, 3),
                Route("Петрозаводск", CardColor.MAGENTO, 2),
                Route("Вологда", CardColor.WHITE, 3)
            )),
        City("Мурманск", LatLong(68.970665, 33.07497),
            listOf(
                Route("Архангельск", CardColor.BLACK, 4),
                Route("Нарьян-Мар", null, 6)
            )),
        City("Псков", LatLong(57.817398, 28.334368),
            listOf(
                Route("Москва", CardColor.RED, 3),
                Route("Санкт-Петербург", CardColor.ORANGE, 1),
                Route("Смоленск", CardColor.BLACK, 2)
            )),
        City("Смоленск", LatLong(54.77897, 32.0471812),
            listOf(
                Route("Москва", CardColor.MAGENTO, 3),
                Route("Курск", CardColor.RED, 3)
            )),
        City("Курск", LatLong(51.739433, 36.179604),
            listOf(
                Route("Воронеж", CardColor.GREEN, 1),
                Route("Москва", CardColor.YELLOW, 3)
            )),
        City("Москва", LatLong(55.7504461, 37.61749431),
            listOf(
                Route("Вологда", CardColor.BLACK, 2),
                Route("Нижний Новгород", CardColor.WHITE, 2),
                Route("Воронеж", CardColor.BLUE, 3),
                Route("Рязань", CardColor.MAGENTO, 1)
            )),
        City("Архангельск", LatLong(64.543022, 40.537121)),
        City("Вологда", LatLong(59.218876, 39.893276),
            listOf(
                Route("Архангельск", CardColor.ORANGE, 4),
                Route("Сыктывкар", CardColor.BLUE, 3),
                Route("Нижний Новгород", CardColor.MAGENTO, 2)
            )),
        City("Сыктывкар", LatLong(61.6685237, 50.8352024),
            listOf(
                Route("Нарьян-Мар", CardColor.YELLOW, 4),
                Route("Воркута", null, 6)
            )),
        City("Нарьян-Мар", LatLong(67.6380175, 53.0071044)),
        City("Воркута", LatLong(67.494957, 64.0401),
            listOf(
                Route("Салехард", CardColor.YELLOW, 1)
            )),
        City("Ивдель", LatLong(60.6973287, 60.4172583),
            listOf(
                Route("Салехард", CardColor.MAGENTO, 4)
            )),
        City("Салехард", LatLong(66.5375387, 66.6157469)),
        City("Нижний Новгород", LatLong(56.328571, 44.003506),
            listOf(
                Route("Казань", CardColor.ORANGE, 2),
                Route("Киров", CardColor.GREEN, 3),
                Route("Пенза", CardColor.BLUE, 2)
            )),
        City("Рязань", LatLong(54.6295687, 39.7425039),
            listOf(
                Route("Пенза", CardColor.RED, 3)
            )),
        City("Киров", LatLong(58.6035257, 49.6639029),
            listOf(
                Route("Сыктывкар", CardColor.BLACK, 3),
                Route("Пермь", CardColor.YELLOW, 3)
            )),
        City("Казань", LatLong(55.7823547, 49.1242266),
            listOf(
                Route("Киров", CardColor.RED, 2),
                Route("Уфа", CardColor.BLUE, 3),
                Route("Ижевск", CardColor.BLACK, 2),
                Route("Самара", CardColor.WHITE, 2)
            )),
        City("Саратов", LatLong(51.530018, 46.034683),
            listOf(
                Route("Самара", CardColor.YELLOW, 3)
            )),
        City("Самара", LatLong(53.198627, 50.113987),
            listOf(
                Route("Оренбург", CardColor.MAGENTO, 3)
            )),
        City("Уфа", LatLong(54.726288, 55.947727),
            listOf(
                Route("Екатеринбург", CardColor.RED, 3),
                Route("Челябинск", CardColor.BLACK, 2),
                Route("Магнитогорск", CardColor.GREEN, 1),
                Route("Оренбург", CardColor.ORANGE, 2)
            )),
        City("Ижевск", LatLong(56.866557, 53.2094166),
            listOf(
                Route("Пермь", CardColor.WHITE, 2)
            )),
        City("Кудымкар", LatLong(59.014606, 54.664135),
            listOf(
                Route("Пермь", CardColor.GREEN, 1),
                Route("Ивдель", CardColor.WHITE, 3)
            )),
        City("Пермь", LatLong(58.014965, 56.246723),
            listOf(
                Route("Екатеринбург", CardColor.MAGENTO, 2)
            )),
        City("Екатеринбург", LatLong(56.839104, 60.60825),
            listOf(
                Route("Ивдель", CardColor.ORANGE, 3),
                Route("Челябинск", CardColor.RED, 1)
            )),
        City("Оренбург", LatLong(51.767452, 55.097118)),
        City("Воронеж", LatLong(51.6605982, 39.2005858),
            listOf(
                Route("Урюпинск", CardColor.YELLOW, 1),
                Route("Ростов-на-Дону", CardColor.MAGENTO, 3)
            )),
        City("Ростов-на-Дону", LatLong(47.2213858, 39.7114196),
            listOf(
                Route("Волгоград", CardColor.GREEN, 2),
                Route("Ставрополь", CardColor.ORANGE, 2)
            )),
        City("Урюпинск", LatLong(50.7970972, 42.0051866),
            listOf(
                Route("Волгоград", CardColor.RED, 2)
            )),
        City("Волгоград", LatLong(48.7081906, 44.5153353),
            listOf(
                Route("Астрахань", CardColor.BLACK, 3),
                Route("Саратов", CardColor.MAGENTO, 2)
            )),
        City("Ставрополь", LatLong(45.0433245, 41.9690934),
            listOf(
                Route("Элиста", CardColor.RED, 2)
            )),
        City("Элиста", LatLong(46.306999, 44.270187),
            listOf(
                Route("Астрахань", CardColor.ORANGE, 2),
                Route("Махачкала", CardColor.MAGENTO, 3)
            )),
        City("Астрахань", LatLong(46.3498308, 48.0326203)),
        City("Краснодар", LatLong(45.0352566, 38.9764814),
            listOf(
                Route("Ростов-на-Дону", CardColor.BLUE, 1),
                Route("Ставрополь", CardColor.YELLOW, 1),
                Route("Владикавказ", CardColor.WHITE, 3)
            )),
        City("Владикавказ", LatLong(43.024593, 44.68211),
            listOf(
                Route("Махачкала", CardColor.YELLOW, 2)
            )),
        City("Магнитогорск", LatLong(53.4242184, 58.983136),
            listOf(
                Route("Челябинск", CardColor.YELLOW, 2)
            )),
        City("Челябинск", LatLong(55.1598408, 61.4025547)),
        City("Пенза", LatLong(53.200001, 45),
            listOf(
                Route("Самара", CardColor.ORANGE, 2),
                Route("Саратов", CardColor.BLACK, 1)
            )),
        City("Петрозаводск", LatLong(61.790039, 34.390007),
            listOf(
                Route("Вологда", CardColor.GREEN, 3)
            )),
        City("Кандалакша", LatLong(67.151442, 32.4130551),
            listOf(
                Route("Петрозаводск", CardColor.BLUE, 4),
                Route("Мурманск", CardColor.GREEN, 2),
                Route("Архангельск", CardColor.YELLOW, 4)
            )),
        City("Махачкала", LatLong(42.9830241, 47.5048717))
    )
}