package ticketToRide

import ticketToRide.PlayerState.MyTurn.*

sealed class PlayerState {

    companion object {
        fun initial(gameMap: GameMap, gameState: GameStateView) =
            when {
                gameState.myPendingTicketsChoice != null ->
                    ChoosingTickets
                gameState.myTurn ->
                    Blank(gameMap, gameState)
                else ->
                    None
            }
    }

    data object None : PlayerState()

    data object ChoosingTickets : PlayerState()

    sealed class MyTurn(
        internal val gameMap: GameMap,
        internal val gameState: GameStateView,
    ) : PlayerState() {

        constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState)

        class Blank internal constructor(gameMap: GameMap, gameState: GameStateView) :
            MyTurn(gameMap, gameState) {
            constructor(prev: MyTurn) : this(prev.gameMap, prev.gameState)
        }

        class PickedFirstCard internal constructor(prev: MyTurn, val chosenCardIx: Int) : MyTurn(prev)

        class PickedCity internal constructor(prev: MyTurn, val target: CityId) : MyTurn(prev) {
            fun buildStation() = BuildingStation(this)
        }

        class BuildingStation private constructor(prev: MyTurn, val target: CityId, val chosenCardsToDropIx: Int?) :
            MyTurn(prev) {
            internal constructor(prev: PickedCity) : this(prev, prev.target, null)
            internal constructor(prev: BuildingStation, chosenCardsToDropIx: Int) : this(
                prev,
                prev.target,
                chosenCardsToDropIx
            )

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
            val from: CityId,
            val to: CityId,
            val chosenCardsToDropIx: Int?
        ) : MyTurn(prev) {
            internal constructor(prev: PickedCity, to: CityId) : this(prev, prev.target, to, null)

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
            sendToServer(req)
            return None
        }
    }


    fun pickedOpenCard(cardIx: Int) = when {
        this is PickedFirstCard && openCards[cardIx] is Card.Car ->
            if (cardIx != chosenCardIx) {
                sendAndResetState(PickCardsRequest.TwoCards.bothOpen(chosenCardIx, cardIx, openCards))
            } else
                Blank(this)
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

    fun onCityClick(cityId: CityId) = when (this) {
        is PickedCity ->
            gameMap.getSegmentsBetween(target, cityId).takeUnless { it.isEmpty() }
                ?.let { BuildingSegment(this, cityId) }
                ?: PickedCity(this, cityId)
        is MyTurn ->
            PickedCity(this, cityId)
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