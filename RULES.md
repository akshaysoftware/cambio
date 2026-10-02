# House rules

## Confirmed

- Four initial cards. Look at two.
- Players may take from the discard pile.

| Card | Points |
| --- | --- |
| Ace | 1 |
| Jack, Queen, black King | 10 |
| Red King | 0 |
| Joker | -1 |

Number cards 2–10 are provisionally face value; confirm before scoring implementation.

| Draw | Power |
| --- | --- |
| 7 or 8 | Look at someone else's card |
| 9 or 10 | Look at your own card |
| Jack | Blind swap |
| Queen | Look at one card on the table and blind swap |
| King | Look at two cards and blind swap |

- You can snap whenever, but only one snap can occur.
- You can snap someone else's card and give them one of your cards.
- An incorrect snap adds a card to your hand.
- A snap that is only too slow carries no penalty.

## Resolve before the relevant gameplay commit

- Number of jokers; which two initial cards may be viewed.
- Whether powers require discarding a freshly drawn card and whether taking
  from the discard pile can activate a power.
- Whether powers/swaps are optional, and permitted swap targets.
- Whether a snap matches rank or points; what opens/closes a snap opportunity
  and whether an incorrect snap consumes it.
- The server must distinguish an invalid match from an already-consumed snap
  opportunity. Do not penalise a valid attempt just because it arrived too late.
- When Cambio may be called; final turns, ties and caller penalties.