# The Train That Refused to Be Scored

*A cautionary tale from the codebase*

It was a quiet Tuesday night on the server. The websockets hummed softly, the JVM had just finished
a satisfying garbage collection, and somewhere in `common/`, a state machine was dozing in the
`WaitingForPlayers` state, dreaming of transitions.

Then four friends logged in.

"Lockdown game night!" typed Dmitry, who always picked red because, as he explained to anyone who
would listen, "red trains are faster."

"They are not faster," said Olga, picking green. "They're the same speed. They don't move."

"That's what they *want* you to think."

The state machine woke up, rubbed its eyes, and transitioned to `GameStarted` with the dignity of a
butler announcing dinner.

---

The first hour went smoothly. Cards were drawn. Tickets were taken. Pavel, a man of quiet ambition,
held a ticket from one corner of the map to the other and said nothing about it, which everyone
noticed, because Pavel always said something about everything.

Marina built a single, beautiful, utterly pointless loop of yellow trains around a small lake.

"What does that connect?" asked Olga.

"It connects me," said Marina, "to inner peace."

The scoring algorithm, which lived a few packages away and had been extensively property-based
tested, overheard this and felt a small chill run down its call stack. Inner peace was not a node
in the graph. It checked. Twice.

---

Meanwhile, Dmitry had a problem. He needed one more grey route to finish his longest path, and the
route required three cards of any single colour. He had two red cards and a locomotive.

"Does a locomotive count as red?" he asked.

"A locomotive counts as anything," said Olga.

"So it counts as red."

"It counts as *a wildcard*."

"Which, right now, is red."

"Dmitry, the locomotive does not have a favourite colour."

The locomotive, which did in fact have a favourite colour (teal, though nobody had ever asked),
stayed silent and was played. The route turned red. Dmitry made a *choo-choo* sound out loud, in
his apartment, alone, at 11:40 p.m. His neighbour knocked on the wall.

---

The final round arrived. Pavel placed his last train and finally spoke.

"Done."

The server sent the `GameEnded` message. The scoring algorithm cracked its knuckles, loaded the
graph, and began its depth-first search for the longest path.

It started at Marina's lake loop.

It went around.

It went around again.

The algorithm paused. *Have I been here before?* it wondered. It checked the visited set. Yes. Yes
it had. Good. It moved on, quietly grateful to whoever had written those property-based tests at
two in the morning during a pandemic.

Then it found Dmitry's network: a sprawling red octopus of routes, each one built in defiance of
geography, strategy, and, at one point, a river.

"Longest path: Dmitry," the algorithm declared.

"I KNEW red was faster," said Dmitry.

"That's not what longest means," said Olga.

---

Final scores appeared on screen. Pavel had completed his giant cross-map ticket and won by a single
point. He said nothing, which was, everyone agreed, the most Pavel way to win.

Marina came last but reported that her inner peace was "fully connected, no cycles detected, except
the lake, which is intentional."

Olga demanded a rematch.

Dmitry was already picking red.

And deep in the server, the state machine sighed, transitioned back to `WaitingForPlayers`, and
whispered to the locomotive card:

"Teal, huh?"

"Nobody ever asks," said the locomotive.

*The End.*
