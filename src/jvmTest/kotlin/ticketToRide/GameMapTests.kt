package ticketToRide

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameMapTests : StringSpec({
    "there should be 20 long tickets" {
        GameMap.longTickets.count() shouldBe 20
    }
    "there should be 452 short tickets" {
        GameMap.shortTickets.count() shouldBe 452
    }
})