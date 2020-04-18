package ticketToRide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameStateTests {
    @Test
    fun `advance to next turn from first player`() {
        assertEquals(1, createGameState(5).advanceTurn().turn)
    }

    @Test
    fun `advance to next turn from last player`() {
        assertEquals(0, createGameState(5).copy(turn = 4).advanceTurn().turn)
    }

    @Test
    fun `next player skips the move when thinking on tickets taken in advance`() {
        var state = createGameState(5)
        state = state.updatePlayer(1) {
            copy(ticketsForChoice = PendingTicketsChoice(state.getRandomTickets(3, false), 1, false))
        }
        val nextState = state.advanceTurn()

        assertEquals(2, nextState.turn)
        assertEquals(true, nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn)
    }

    @Test
    fun `next player doesn't skip the move when thinking on tickets not taken in advance`() {
        var state = createGameState(5)
        state = state.updatePlayer(1) {
            copy(ticketsForChoice = PendingTicketsChoice(state.getRandomTickets(3, false), 1, true))
        }
        val nextState = state.advanceTurn()

        assertEquals(1, nextState.turn)
        assertEquals(true, nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn)
    }

    @Test
    fun `player should choose from tickets before next move if takes tickets in turn`() {
        val state = createGameState(5)
        val nextState = state.pickTickets(state.players[0].name)

        assertEquals(1, nextState.turn)
        assertEquals(true, nextState.players[0].ticketsForChoice!!.shouldChooseOnNextTurn)
    }

    @Test
    fun `player doesn't have to choose from tickets before next move if takes tickets not in turn`() {
        val state = createGameState(5).copy(turn = 3)
        val nextState = state.pickTickets(state.players[1].name)

        assertEquals(3, nextState.turn)
        assertNull(nextState.players[0].ticketsForChoice)
        assertEquals(false, nextState.players[1].ticketsForChoice!!.shouldChooseOnNextTurn)
    }

    private fun createGameState(playersCount: Int) =
        GameState(
            (1..playersCount).map { Player(PlayerName(it.toString()), Color.values().random(), 45, (1..4).map { Card.random() }, null) },
            (1..5).map { Card.random() },
        0)
}