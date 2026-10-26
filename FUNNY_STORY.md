# The Great Pittsburgh Incident

A QA engineer, a train conductor, and a Kotlin compiler walk into a routing
dispute. This is the true(ish) story of how `tickettoride` almost shipped a
bug that would have let a player claim the same route twice.

It started innocently: someone noticed that two players could both "win" the
same gray route between Pittsburgh and Toronto on the same turn, as long as
they clicked fast enough. The team nicknamed it the **Pittsburgh Incident**,
because nothing says "high-stakes infrastructure crisis" like a board game
rules bug.

Investigation minutes (reconstructed, mostly accurate):

1. **09:02** — Player A claims the route. Server says "nice."
2. **09:02** — Player B, in a parallel universe apparently, also claims it.
   Server says "nice" to them too.
3. **09:03** — Both players place little plastic trains on a route that now,
   according to the database, belongs to everyone and no one — a genuinely
   bipartisan outcome.
4. **09:04** — A third player, watching this unfold, asks if they can also
   claim it "for solidarity." Request denied.
5. **09:05** — Someone finally says the sentence every backend engineer
   dreads: "Wait, is this route claim not atomic?"

The root cause, naturally, was a classic race condition — two requests
checking "is this route free?" before either had finished claiming it,
like two people checking if a parking spot is empty by opening their car
doors at the exact same moment.

The fix took four lines of code. The Slack thread arguing about whose turn
it "really" was took four hours, and ended only when someone pointed out
that in real Ticket to Ride, this is also how most family game nights end
anyway — in a dispute, not a victory.

**Moral of the story:** if two trains can occupy the same track at once,
you don't have a game, you have a GTFS schedule.
