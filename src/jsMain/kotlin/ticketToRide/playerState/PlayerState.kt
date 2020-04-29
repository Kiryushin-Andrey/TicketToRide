package ticketToRide.playerState

import kotlinx.coroutines.channels.Channel
import ticketToRide.*
import kotlin.math.min

typealias PickedFirstCard = PlayerState.MyTurn.PickedFirstCard
typealias BuildingSegmentFrom = PlayerState.MyTurn.BuildingSegmentFrom
typealias BuildingSegment = PlayerState.MyTurn.BuildingSegment

sealed class PlayerState {

    companion object {
        fun initial(gameMap: GameMap, gameState: GameStateView, requests: Channel<Request>) = when {
            gameState.myPendingTicketsChoice != null ->
                with(gameState.myPendingTicketsChoice) {
                    ChoosingTickets(requests, tickets.map { TicketChoice(it) }, minCountToKeep)
                }
            gameState.myTurn -> MyTurn.Blank(gameMap, gameState, requests)
            else -> None
        }
    }

    object None : PlayerState()

    data class TicketChoice(val ticket: Ticket, val keep: Boolean = false)

    class ChoosingTickets internal constructor(
        private val requests: Channel<Request>,
        val items: List<TicketChoice>,
        val minCountToKeep: Int
    ) : PlayerState() {

        fun toggleTicket(ticket: Ticket) =
            ChoosingTickets(
                requests,
                items.map { if (it.ticket == ticket) it.copy(keep = !it.keep) else it },
                minCountToKeep
            )

        val isValid
            get() = items.count { it.keep } >= minCountToKeep

        fun confirm() =
            if (isValid) {
                requests.offer(ConfirmTicketsChoiceRequest(ticketsToKeep))
                None
            } else this

        val ticketsToKeep
            get() = items.filter { it.keep }.map { it.ticket }
    }

    sealed class MyTurn(
        internal val gameMap: GameMap,
        internal val gameState: GameStateView,
        internal val requests: Channel<Request>
    ) : PlayerState() {

        constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState, prev.requests)

        class Blank internal constructor(gameMap: GameMap, gameState: GameStateView, requests: Channel<Request>) :
            MyTurn(gameMap, gameState, requests) {
            internal constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState, prev.requests)
        }

        class PickedFirstCard internal constructor(prev: MyTurn, val chosenCardIx: Int) : MyTurn(prev)

        class BuildingSegmentFrom internal constructor(prev: MyTurn, val from: CityName) : MyTurn(prev)

        class BuildingSegment private constructor(prev: MyTurn, val segment: Segment, val chosenCardsToDropIx: Int?) :
            MyTurn(prev) {

            internal constructor(prev: BuildingSegmentFrom, segment: Segment) : this(prev, segment, null)

            internal constructor(prev: ticketToRide.playerState.BuildingSegment, chosenCardsToDropIx: Int) : this(
                prev,
                prev.segment,
                chosenCardsToDropIx
            )

            val optionsForCardsToDrop by lazy { getOptionsForCardToDrop() }

            private fun getOptionsForCardToDrop(): List<List<Card>> {
                val countByCar = myCards.filterIsInstance<Card.Car>().groupingBy { it }.eachCount()
                fun getOptionsForCars(locoCount: Int) =
                    if (locoCount == segment.length)
                        listOf(List(locoCount) { Card.Loco })
                    else countByCar
                        .filter {
                            it.value >= segment.length - locoCount && (segment.color == null || it.key.color == segment.color)
                        }
                        .map { (car, _) ->
                            val carsCount = segment.length - locoCount
                            if (locoCount > 0) List(locoCount) { Card.Loco } + List(carsCount) { car }
                            else List(carsCount) { car }
                        }

                val locoCount = min(segment.length, myCards.filterIsInstance<Card.Loco>().count())
                return (0..locoCount).flatMap { getOptionsForCars(it) }
            }

            fun chooseCardsToDrop(ix: Int) = BuildingSegment(this, ix)

            fun confirm() =
                (if (optionsForCardsToDrop.size == 1) 0 else chosenCardsToDropIx)?.let {
                    sendAndResetState(BuildSegmentRequest(segment.from, segment.to, optionsForCardsToDrop[it]))
                } ?: this
        }

        val openCards get() = gameState.openCards

        val myCards get() = gameState.myCards

        internal fun sendAndResetState(req: GameRequest): PlayerState {
            requests.offer(req)
            return None
        }
    }


    fun pickedOpenCard(cardIx: Int) = when {
        this is PickedFirstCard && openCards[cardIx] is Card.Car ->
            if (cardIx != chosenCardIx) {
                val card1 = openCards[chosenCardIx] as Card.Car
                val card2 = openCards[cardIx] as Card.Car
                sendAndResetState(PickCardsRequest.TwoCards(card1 to card2))
            }
            else
                MyTurn.Blank(this)
        this is MyTurn ->
            if (openCards[cardIx] is Card.Loco)
                sendAndResetState(PickCardsRequest.Loco)
            else
                PickedFirstCard(this, cardIx)
        else ->
            this
    }

    fun pickedClosedCard() = when (this) {
        is PickedFirstCard ->
            sendAndResetState(PickCardsRequest.TwoCards((openCards[chosenCardIx] as Card.Car) to null))
        is MyTurn ->
            sendAndResetState(PickCardsRequest.TwoCards(null to null))
        else ->
            this
    }

    fun pickedTickets() = when (this) {
        is MyTurn -> sendAndResetState(PickTicketsRequest)
        else -> this
    }

    fun onCityClick(cityName: CityName) = when (this) {
        is BuildingSegmentFrom ->
            gameMap.getSegmentBetween(from, cityName)?.let { BuildingSegment(this, it) }
                ?: BuildingSegmentFrom(this, cityName)
        is MyTurn ->
            BuildingSegmentFrom(this, cityName)
        else ->
            this
    }
}

val PlayerState.citiesToHighlight
    get() = when (this) {
        is BuildingSegmentFrom -> listOf(from)
        is BuildingSegment -> listOf(segment.from, segment.to)
        else -> emptyList()
    }