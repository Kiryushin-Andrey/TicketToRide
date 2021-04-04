package ticketToRide.playerState

import kotlinx.coroutines.channels.SendChannel
import ticketToRide.*
import ticketToRide.playerState.PlayerState.MyTurn.*

sealed class PlayerState {

    companion object {
        fun initial(gameMap: GameMap, gameState: GameStateView, requests: SendChannel<Request>) = when {
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
        private val requests: SendChannel<Request>,
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

        private val ticketsToKeep
            get() = items.filter { it.keep }.map { it.ticket }
    }

    sealed class MyTurn(
        internal val gameMap: GameMap,
        internal val gameState: GameStateView,
        internal val requests: SendChannel<Request>
    ) : PlayerState() {

        constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState, prev.requests)

        class Blank internal constructor(gameMap: GameMap, gameState: GameStateView, requests: SendChannel<Request>) :
            MyTurn(gameMap, gameState, requests) {
            internal constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState, prev.requests)
        }

        class PickedFirstCard internal constructor(prev: MyTurn, val chosenCardIx: Int) : MyTurn(prev)

        class PickedCity internal constructor(prev: MyTurn, val target: CityName) : MyTurn(prev) {
            fun buildStation() = BuildingStation(this)
        }

        class BuildingStation private constructor(prev: MyTurn, val target: CityName, val chosenCardsToDropIx: Int?) : MyTurn(prev) {
            internal constructor(prev: PickedCity) : this(prev, prev.target, null)
            internal constructor(prev: BuildingStation, chosenCardsToDropIx: Int) : this(prev, prev.target, chosenCardsToDropIx)

            val optionsForCardsToDrop by lazy {
                if (gameState.me.stationsLeft > 0)
                    myCards.getOptionsForCardsToDrop(gameState.me.placedStations.size + 1, null)
                else
                    emptyList()
            }

            fun chooseCardsToDrop(ix: Int) = BuildingStation(this, ix)

            fun confirm() =
                (if (optionsForCardsToDrop.size == 1) 0 else chosenCardsToDropIx)?.let {
                    sendAndResetState(BuildStationRequest(target, optionsForCardsToDrop[it].cards))
                } ?: this
        }

        class BuildingSegment private constructor(
            prev: MyTurn,
            val from: CityName,
            val to: CityName,
            val chosenCardsToDropIx: Int?)
            : MyTurn(prev)
        {
            internal constructor(prev: PickedCity, to: CityName) : this(prev, prev.target, to, null)

            internal constructor(prev: PickedCity, segment: Segment) : this(prev, segment.from, segment.to, null)

            internal constructor(prev: BuildingSegment, chosenCardsToDropIx: Int) : this(
                prev,
                prev.from,
                prev.to,
                chosenCardsToDropIx
            )

            val length by lazy {
                availableSegments.map { it.length }.distinct().single()
            }

            val availableSegments by lazy {
                gameMap.getSegmentsBetween(from, to)
                    .filter { segment -> !gameState.players.flatMap { it.occupiedSegments }.contains(segment) }
            }

            val optionsForCardsToDrop by lazy {
                availableSegments.flatMap { segment ->
                    myCards.getOptionsForCardsToDrop(segment.length, segment.color)
                        .map { segment to it }
                }
            }

            fun chooseCardsToDrop(ix: Int) = BuildingSegment(this, ix)

            fun confirm() =
                (if (optionsForCardsToDrop.size == 1) 0 else chosenCardsToDropIx)
                    ?.let { optionsForCardsToDrop[it] }
                    ?.let { (segment, option) ->
                        sendAndResetState(BuildSegmentRequest(segment, option.cards))
                    } ?: this
        }

        val openCards get() = gameState.openCards

        val myCards get() = gameState.myCards

        internal fun sendAndResetState(req: Request): PlayerState {
            requests.offer(req)
            return None
        }
    }


    fun pickedOpenCard(cardIx: Int) = when {
        this is PickedFirstCard && openCards[cardIx] is Card.Car ->
            if (cardIx != chosenCardIx) {
                sendAndResetState(PickCardsRequest.TwoCards.bothOpen(chosenCardIx, cardIx, openCards))
            } else
                MyTurn.Blank(this)
        this is MyTurn ->
            if (openCards[cardIx] is Card.Loco)
                sendAndResetState(PickCardsRequest.Loco(cardIx))
            else
                PickedFirstCard(this, cardIx)
        else ->
            this
    }

    fun pickedClosedCard() = when (this) {
        is PickedFirstCard ->
            sendAndResetState(PickCardsRequest.TwoCards.openAndClosed(chosenCardIx, openCards))
        is MyTurn ->
            sendAndResetState(PickCardsRequest.TwoCards.bothClosed())
        else ->
            this
    }

    fun pickedTickets() = when (this) {
        is MyTurn -> sendAndResetState(PickTicketsRequest)
        else -> this
    }

    fun onCityClick(cityName: CityName) = when (this) {
        is PickedCity ->
            gameMap.getSegmentsBetween(target, cityName).takeUnless { it.isEmpty() }
                ?.let { BuildingSegment(this, cityName) }
                ?: PickedCity(this, cityName)
        is MyTurn ->
            PickedCity(this, cityName)
        else ->
            this
    }

    fun onSegmentClick(segment: Segment) = when (this) {
        is MyTurn ->
            BuildingSegment(PickedCity(this, segment.from), segment)
        else ->
            this
    }
}

val PlayerState.citiesToHighlight
    get() = when (this) {
        is PickedCity -> listOf(target)
        is BuildingStation -> listOf(target)
        is BuildingSegment -> listOf(from, to)
        else -> emptyList()
    }