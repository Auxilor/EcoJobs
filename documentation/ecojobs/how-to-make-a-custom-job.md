---
title: "How to Make a Job"
sidebar_position: 1
---

A **job** is a config file that earns players rewards for doing something, like mining or fighting, and levels up to pay out more over time. Each job is one `.yml` file built from a few parts: **job info**, **progression**, and **effects**. This page takes you from an empty file to a working, levelling job.

## Quick start

1. Open the `/plugins/EcoJobs/jobs/` folder.
2. Copy `_example.yml` and rename it, e.g. `miner.yml`. The file name is the job ID.
3. Edit `name`, `description`, and `icon` so the job shows up the way you want.
4. Set how XP is earned under `xp-gain-methods` and what the job pays under `effects`.
5. Run `/ecojobs reload` in-game.
6. Open `/jobs`, join your new job, and do its action (e.g. mine a block) to confirm you gain XP and rewards.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real job. You can also organise jobs into subfolders inside `jobs/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the job ID, and it's what you use in commands, placeholders, and effects (so `miner.yml` is the job `miner`). The `icon` field uses the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system) for its item syntax.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the job will not load.
:::

## The structure of a job

A job config is made of these parts:

| Part | What it controls |
| --- | --- |
| **Job info** | The name, description, icon, and unlock/reset behaviour |
| **Joining and leaving** | Prices, lore, and effects for entering or leaving the job |
| **Progression** | The XP needed per level and how XP is earned |
| **Descriptions** | Custom placeholders and the text shown for rewards and effects |
| **Level up** | The messages and effects fired when the job levels up |
| **Effects** | What the job actually does while active |

Here is a complete job with every part in place:

```yaml
# === Job info: how the job presents itself ===
name: "&6Miner" # Display name of the job
description: "&8&oLevel up by mining blocks" # Short description shown in the GUI
icon: player_head texture:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU3MDVjZjg2NGRmMmMxODJlMzJjNDg2YjcxNDdjYmY3ODJhMGFhM2RmOGE2ZDYxNDUzOTM5MGJmODRmYjE1ZCJ9fX0=" # GUI icon; uses the Item Lookup System
unlocked-by-default: true # Whether players can join without unlocking it first
reset-on-quit: false # Whether job progress wipes when the player leaves the job

# === Joining and leaving: cost and side effects of entering/leaving ===
join-price:
  value: 0 # Cost to join; set to 0 to disable
  type: coins
  display: "&a$%value%"
join-lore: [] # Extra lore on the join button; reference with %join_lore%
join-effects:
  - id: broadcast # Effects run when a player joins the job
    args:
      message: "&8» &a%player% &8joined the &6Miner &8job!"
leave-price:
  value: 20000 # Cost to leave; set to 0 to disable
  type: coins
  display: "&a$%value%"
leave-effects:
  - id: send_message # Effects run when a player leaves the job
    args:
      message: "&8» &8You left the &6Miner &8job!"
leave-lore:
  - " &8» This will cost %leave_price%" # Lore on the confirm-leave button; reference with %leave_lore%

# === Progression: XP per level and how XP is earned ===
xp-requirements: # XP to reach each level, from level 1; list length is the max level
  - 50
  - 125
  - 200
  - 300
  - 500
xp-gain-methods:
  - trigger: mine_block # The trigger that grants XP
    multiplier: 0.5 # Multiplies the value from the trigger; use "value" for a flat amount instead
    conditions: []
    filters:
      items:
        - "*diamond_pickaxe" # Only count blocks mined with these items

# === Descriptions: custom placeholders and GUI reward text ===
level-placeholders: # Custom placeholders for use in descriptions; do not add %, it's automatic
  - id: "money"
    value: "%level% * 0.4" # Value is a math expression using %level%
  - id: "blocks"
    value: "ceil(10 - %level% / 10)"
effects-description: # Text shown by %effects%; key is the minimum level it shows from
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
rewards-description: # Same as above, but shown by %rewards%
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

# === Level up: what happens on level up ===
level-up-messages: # Message sent on level up; key is the minimum level it shows from
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
level-up-effects:
  - id: give_item
    args:
      items:
        - diamond
      every: 5 # Run every 5 levels
      require: "%level% = 5" # Only run when this expression is true

# === Effects: what the job does while active ===
effects:
  - id: give_money
    args:
      every: "ceil(10 - %level% / 10)"
      amount: "0.4 * %level%"
    filters:
      items:
        - "*diamond_pickaxe"
    triggers:
      - mine_block
conditions: [] # Conditions required for the effects to run
```

### Job info

These fields control how the job looks and how players gain access to it.

```yaml
name: "&6Miner" # Display name of the job
description: "&8&oLevel up by mining blocks" # Short description shown in the GUI
icon: player_head texture:"..." # GUI icon; uses the Item Lookup System
unlocked-by-default: true # Whether players can join without unlocking it first
reset-on-quit: false # Whether job progress wipes when the player leaves the job
```

A locked job (`unlocked-by-default: false`) only becomes joinable once you run `/ecojobs unlock <player> <job>` or unlock it through an effect.

### Joining and leaving

Set what it costs to enter or leave the job, plus the lore and effects fired on each.

```yaml
join-price:
  value: 0 # Cost to join; set to 0 to disable
  type: coins
  display: "&a$%value%"
join-effects:
  - id: broadcast # Effects run when a player joins the job
    args:
      message: "&8» &a%player% &8joined the &6Miner &8job!"
leave-price:
  value: 20000 # Cost to leave; set to 0 to disable
  type: coins
  display: "&a$%value%"
```

:::info
Prices are their own shared system, so `type` can be coins, XP levels, items, or anything else eco supports. See [Prices](https://plugins.auxilor.io/all-plugins/prices).
:::

### Progression

Progression has two parts: how much XP each level needs, and how players earn that XP.

There are two ways to set the XP curve. Use a formula for infinite levels:

```yaml
xp-formula: (2 ^ %level%) * 25 # XP per level, where %level% is the level being calculated; see https://plugins.auxilor.io/all-plugins/math
max-level: 100 # Optional; with no max-level, levelling is infinite
```

Or list the requirement for each level explicitly:

```yaml
xp-requirements: # XP to reach each level, from level 1; list length is the max level
  - 50 # XP to reach level 1
  - 125 # XP to reach level 2
  - 200
```

Then define how XP is earned:

```yaml
xp-gain-methods:
  - trigger: mine_block # The trigger that grants XP
    multiplier: 0.5 # Multiplies the value from the trigger; use "value" for a flat amount instead
    conditions: []
    filters:
      items:
        - "*diamond_pickaxe" # Only count blocks mined with these items
```

### Descriptions

Custom placeholders let you compute values once and reuse them in the reward and effect text shown in GUIs.

```yaml
level-placeholders: # Custom placeholders; do not add %, it's automatic
  - id: "money"
    value: "%level% * 0.4" # Value is a math expression using %level%
  - id: "blocks"
    value: "ceil(10 - %level% / 10)"
effects-description: # Text shown by %effects%; key is the minimum level it shows from
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
rewards-description: # Same, but shown by %rewards%
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
```

### Level up

Configure the message and any effects that fire when the job gains a level.

```yaml
level-up-messages: # Message sent on level up; key is the minimum level it shows from
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
level-up-effects:
  - id: give_item
    args:
      items:
        - diamond
      every: 5 # Run every 5 levels
      require: "%level% = 5" # Only run when this expression is true
```

### Effects

This is what the job does while active: the effects it runs, filtered and triggered however you like.

```yaml
effects:
  - id: give_money
    args:
      every: "ceil(10 - %level% / 10)"
      amount: "0.4 * %level%"
    filters:
      items:
        - "*diamond_pickaxe"
    triggers:
      - mine_block
conditions: [] # Conditions required for the effects to run
```

:::danger Effects are their own system
Effects, conditions, filters, and triggers are a shared eco system with its own documentation, far larger than this page can cover.

- [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect)
- [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain)
:::

## Internal placeholders

These placeholders are available inside a job config:

| Placeholder | Value |
| --- | --- |
| `%level%` | The player's job level. Useful for scaling effects |
| `%level_numeral%` | The player's job level in Roman numerals |
| `%level_x%` | The player's job level, +/- a value, e.g. `%level_-1%` is the current level minus one |
| `%level_x_numeral%` | The player's job level, +/- a value, in Roman numerals |

:::tip Troubleshooting
- **Job not showing in `/jobs`?** The file failed to load. Check the ID has only lowercase letters, numbers, and underscores, and that the YAML is valid.
- **Not gaining XP?** The trigger or filters don't match what the player is doing. Confirm the `trigger` is correct and the `filters` allow the item or block in use.
- **Rewards not paying out?** The `effects` triggers or filters don't match, or a `condition` is blocking them. Test with the filters removed first.
- **Changes not applying?** You didn't reload. Run `/ecojobs reload` after every edit.
:::

<hr/>

## Where to go next

- **Effects and conditions:** [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) is the deep dive on the `effects` section.
- **Plugin config:** [Plugin Config](plugin-config) covers the GUIs, leaderboard, and storage.
- **Commands:** [Commands and Permissions](commands-and-permissions) for unlocking, resetting, and granting jobs.
- **More jobs:** browse community configs on [lrcdb](https://lrcdb.auxilor.io/), or the [default jobs](https://github.com/Auxilor/EcoJobs/tree/master/eco-core/core-plugin/src/main/resources/jobs).