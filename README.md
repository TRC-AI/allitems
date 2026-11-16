# All Items Challenge

Collect every item in the game: in any order you like, or one random item at a time.

## Modes

- **Free mode** (`/allitems start`): every item counts the first time you have it. The order does not
  matter. Any item you do not have yet can be skipped by name with `/allitems skip <item>`.
- **Order mode** (`/allitems start order`): you are given one random item you do not have yet. As soon
  as you have it, it is ticked off and the next random item is drawn. The current item can be skipped
  with `/allitems skip`.

Every item registered in the game is part of the challenge, including the ones you cannot obtain in
survival (bedrock, command blocks, spawn eggs, ...). That is what skipping is for: skipped items count
as done, and the final summary tells you how many you skipped.

An item counts when it is in your hotbar, main inventory, offhand, armor slots, or held on the mouse
cursor. Your inventory is checked every tick while the challenge runs.

## Commands

Every player can use these commands. On a server, each player runs their own challenge.

| Command                 | Effect                                                                                                                          |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `/allitems start`       | Starts the challenge in free mode                                                                                               |
| `/allitems start order` | Starts the challenge in order mode                                                                                              |
| `/allitems pause`       | Pauses the challenge and the timer                                                                                              |
| `/allitems resume`      | Continues a paused challenge                                                                                                    |
| `/allitems skip`        | Skips the current item (order mode)                                                                                             |
| `/allitems skip <item>` | Skips an item you do not have yet, e.g. `/allitems skip bedrock` (free mode). Suggestions list only the missing items           |
| `/allitems reset`       | Stops and deletes the challenge after a confirmation: click the link in chat or run `/allitems reset confirm` within 30 seconds |
| `/allitems list`        | Opens the item list                                                                                                             |

A challenge can only be started once. To start over, reset it first.

Leaving the world does not pause the challenge, and dying does not lose it. When you come back, it
continues where it was. The timer only counts while you are in the world and the challenge is not paused.

## Display

A bar at the top of the screen shows how many items you have, out of how many, and the elapsed time. In
order mode it also shows the item you need next, with a small picture of it. The bar is green while
running, yellow while paused and purple when complete. Vanilla boss bars move down below it.

Every collected item plays a short sound and is announced in chat. The chat message can be turned off,
see [Settings](#settings).

## Item list

`/allitems list` opens a double chest:

- **Lime concrete** (far left): shows the items you already have. Skipped items are marked red.
- **Red concrete** (far right): shows the items you still need.
- **Arrows** next to them: previous and next page.
- **Clock**: mode, state, progress, skipped items and time.
- In order mode, the item you need next is shown next to the clock.

The title shows which list and which page you are on. Press Escape to close it.

### Search

The search field above the chest filters the current list by English item names and ids:

- Words can come in any order and can be the start of a word: `red candle`, `candle red`, `red cand`
- Small typos are tolerated: `red candel`
- `minecraft:red_candle` or `red_candle` searches by id
- `@minecraft` or `@mod name` filters by mod
- `#candles` or `#minecraft:logs` filters by item tag

Search terms can be combined, e.g. `@minecraft #logs oak`.

## Settings

In the TRC settings screen (press **U**, or the TRC button in the main menu) under
**All Items Challenge**, or with `/trc`. The setting names and the whole display are available in
English, German and Pirate Speak. Chat messages are in English.

| Setting                 | Id              | Default  | Effect                                                                               |
|-------------------------|-----------------|----------|--------------------------------------------------------------------------------------|
| Show timer              | `show_timer`    | on       | Shows the elapsed time in the bar and in the item list                               |
| Chat message on collect | `chat_messages` | on       | Announces every collected item in chat. Skips and the final summary are always shown |
| Open item list          | `list_key`      | unbound  | A key or mouse button that opens the item list, like `/allitems list`                |

All three are client settings, so every player chooses for themselves. To bind the key, click its
button in the settings screen and press the key you want; Escape unbinds it.

Example: `/trc allitems_client show_timer false`

## Advancement

Completing the challenge grants the hidden challenge advancement **Hoarder Supreme**.

## Requirements

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [TRC Core & Libs](https://modrinth.com/mod/trc-core) 1.2 or newer
- Java 25

There is one file per Minecraft version. The mod has to be installed on the server **and** on every
client: a server running it turns away players who do not have it.

## License

MIT, see [LICENSE](LICENSE).