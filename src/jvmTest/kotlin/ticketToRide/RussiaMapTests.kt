package ticketToRide

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RussiaMapTests : StringSpec({
    val map = createMapOfRussia()

    "there should be 20 long tickets" {
        map.longTickets.count() shouldBe 20
    }

    "there should be 452 short tickets" {
        map.shortTickets.count() shouldBe 452
    }
})