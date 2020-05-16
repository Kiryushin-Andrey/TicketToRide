package ticketToRide

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*

val map = createMapOfRussia()

class GameStateTests : StringSpec({

    "advance to next turn from first player" {
        createGameState(5).apply {
            advanceTurnFrom(players[turn].name, map).turn shouldBe 1
        }
    }

    "advance to next turn from last player" {
        createGameState(5).copy(turn = 4).apply {
            advanceTurnFrom(players[turn].name, map).turn shouldBe 0
        }
    }

    "next player skips the move when thinking on tickets taken in advance" {
        createGameState(5).run {
            updatePlayer(1) {
                copy(ticketsForChoice = PendingTicketsChoice(getRandomTickets(map, 3, false), 1, false))
            }.advanceTurn(map)
        }.apply {
            turn shouldBe 2
            players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
        }
    }

    "next player doesn't skip the move when thinking on tickets not taken in advance" {
        createGameState(5).run {
            updatePlayer(1) {
                copy(ticketsForChoice = PendingTicketsChoice(getRandomTickets(map, 3, false), 1, true))
            }.advanceTurn(map)
        }.apply {
            turn shouldBe 1
            players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
        }
    }

    "player should choose from tickets before next move if takes tickets in turn" {
        val state = createGameState(5)
        val (nextState, _) = state.processRequest(PickTicketsRequest, map, state.players[0].name)

        nextState.turn shouldBe 1
        nextState.players[0].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
    }

    "player doesn't have to choose from tickets before next move if takes tickets not in turn" {
        val state = createGameState(5).copy(turn = 3)
        val (nextState, _) = state.processRequest(PickTicketsRequest, map, state.players[1].name)

        nextState.turn shouldBe 3
        nextState.players[0].ticketsForChoice shouldBe null
        nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe false
    }
})

fun createGameState(playersCount: Int) =
    GameState(
        GameId("gameId"),
        (1..playersCount).map {
            Player(
                PlayerName(it.toString()),
                PlayerColor.values().random(),
                45,
                3,
                (1..4).map { Card.random(map) },
                emptyList(),
                emptyList(),
                null
            )
        },
        (1..5).map { Card.random(map) },
        0,
        null,
        45
    )