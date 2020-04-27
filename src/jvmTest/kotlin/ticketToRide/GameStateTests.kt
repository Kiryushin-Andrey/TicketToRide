package ticketToRide

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.*

class GameStateTests: StringSpec({
    "advance to next turn from first player" {
        createGameState(5).advanceTurn().turn shouldBe 1
    }

    "advance to next turn from last player" {
        createGameState(5).copy(turn = 4).advanceTurn().turn shouldBe 0
    }

    "next player skips the move when thinking on tickets taken in advance" {
        var state = createGameState(5)
        state = state.updatePlayer(1) {
            copy(ticketsForChoice = PendingTicketsChoice(state.getRandomTickets(3, false), 1, false))
        }
        val nextState = state.advanceTurn()

        nextState.turn shouldBe  2
        nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
    }

    "next player doesn't skip the move when thinking on tickets not taken in advance" {
        var state = createGameState(5)
        state = state.updatePlayer(1) {
            copy(ticketsForChoice = PendingTicketsChoice(state.getRandomTickets(3, false), 1, true))
        }
        val nextState = state.advanceTurn()

        nextState.turn shouldBe 1
        nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
    }

    "player should choose from tickets before next move if takes tickets in turn" {
        val state = createGameState(5)
        val nextState = state.pickTickets(state.players[0].name)

        nextState.turn shouldBe 1
        nextState.players[0].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe true
    }

    "player doesn't have to choose from tickets before next move if takes tickets not in turn" {
        val state = createGameState(5).copy(turn = 3)
        val nextState = state.pickTickets(state.players[1].name)

        nextState.turn shouldBe 3
        nextState.players[0].ticketsForChoice shouldBe null
        nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn shouldBe false
    }
})

fun createGameState(playersCount: Int) =
    GameState(
        (1..playersCount).map {
            Player(
                PlayerName(it.toString()),
                Color.values().random(),
                45,
                (1..4).map { Card.random() },
                emptyList(),
                null
            )
        },
        (1..5).map { Card.random() },
        0,
        null
    )