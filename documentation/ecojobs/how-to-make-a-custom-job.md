---
title: How to make a Job
sidebar_position: 1
---

## How to add jobs
Each job is its own config file, placed in the `/jobs/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Job is the file name. This is what you use in commands, effects and placeholders.
ID's must be lowercase letters, numbers, and underscores only.

## Example Job Config

```yaml
name: "&6Miner"
description: "&8&oLevel up by mining blocks"
icon: player_head texture:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU3MDVjZjg2NGRmMmMxODJlMzJjNDg2YjcxNDdjYmY3ODJhMGFhM2RmOGE2ZDYxNDUzOTM5MGJmODRmYjE1ZCJ9fX0="
unlocked-by-default: true
reset-on-quit: false

join-price:
  value: 0
  type: coins
  display: "&a$%value%"

join-lore: []

join-effects:
  - id: broadcast
    args:
      message: "&8» &a%player% &8joined the &6Miner &8job!"

leave-price:
  value: 20000
  type: coins
  display: "&a$%value%"
  
leave-effects:
  - id: send_message
    args:
      message: "&8» &8You left the &6Miner &8job!"

leave-lore:
  - " &8» This will cost %leave_price%"

xp-requirements:  
- 50  
- 125  
- 200  
- 300  
- 500  
- 750  
- 1000  
- 1500  
- 2000  
- 3500  
- 5000  
- 7500  
- 10000

xp-gain-methods:
  - trigger: mine_block
    multiplier: 0.5
    conditions: [ ]
    filters:
      items:
        - "*wooden_pickaxe"
        - "*stone_pickaxe"
        - "*iron_pickaxe"
        - "*golden_pickaxe"
        - "*diamond_pickaxe"
        - "*netherite_pickaxe"

level-placeholders:
  - id: "money"
    value: "%level% * 0.4"
  - id: "blocks"
    value: "ceil(10 - %level% / 10)"

effects-description:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

rewards-description:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

level-up-messages:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

level-up-effects:
  - id: give_item
    args:
      items:
        - diamond
      every: 5
      require: "%level% = 5"

effects:
  - id: give_money
    args:
      every: "ceil(10 - %level% / 10)"
      amount: "0.4 * %level%"
    filters:
      items:
        - "*wooden_pickaxe"
        - "*stone_pickaxe"
        - "*iron_pickaxe"
        - "*golden_pickaxe"
        - "*diamond_pickaxe"
        - "*netherite_pickaxe"
    triggers:
      - mine_block

conditions: [ ]
```

## Understanding all the sections
### The Job Info Section
```yaml
name: "&6Miner" # The display name of the job
description: "&8&oLevel up by mining blocks" # The description of the job
# The icon in GUIs: https://plugins.auxilor.io/the-item-lookup-system
icon: player_head texture:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU3MDVjZjg2NGRmMmMxODJlMzJjNDg2YjcxNDdjYmY3ODJhMGFhM2RmOGE2ZDYxNDUzOTM5MGJmODRmYjE1ZCJ9fX0="
unlocked-by-default: true # If the job should be unlocked by default
reset-on-quit: false # If job progress should be reset when quitting
```

### The Joining & Leaving Section

```yaml
# The price to join this job (set to 0 to disable)
# Read here for more: https://plugins.auxilor.io/all-plugins/prices
join-price:
  value: 0
  type: coins
  display: "&a$%value%"

# Lore shown when clicking on the job icon to join it
# Reference with %join_lore%
join-lore: []

# A list of effects to run when the player joins the job.
# Read https://plugins.auxilor.io/effects/configuring-an-effect
join-effects:
  - id: broadcast
    args:
      message: "&8» &a%player% &8joined the &6Miner &8job!"

# The price to leave this job (set to 0 to disable)
# Read here for more: https://plugins.auxilor.io/all-plugins/prices
leave-price:
  value: 20000
  type: coins
  display: "&a$%value%"
  
# A list of effects to run when the player leaves the job.
# Read https://plugins.auxilor.io/effects/configuring-an-effect
leave-effects:
  - id: send_message
    args:
      message: "&8» &8You left the &6Miner &8job!"

# Lore shown on the confirm leave button
# Reference with %leave_lore%
leave-lore:
  - " &8» This will cost %leave_price%"
```

### The Progression Section
#### XP Requirements

There are two ways to specify level XP requirements:  
1. A formula to calculate for infinite levels  

```yaml
xp-formula: (2 ^ %level%) * 25 # The formula to calculate XP requirements for each level, where %level% is the level to calculate for. See here for math https://plugins.auxilor.io/all-plugins/math
max-level: 100 # (Optional) The max level, if not specified, there is no max level  
```

2. A list of XP requirements for each level
```yaml
xp-requirements: # The XP required to reach each level, from Level 1. The length of the list is the max level.
- 50 # XP required to reach level 1
- 125 # XP required to reach level 2
- 200  
- 300  
- 500  
- 750  
- 1000  
- 1500  
- 2000  
- 3500  
- 5000  
- 7500  
- 10000
```
#### XP Gain Methods

```yaml
# An XP gain method takes a trigger, a multiplier, conditions, and filters.
# The 'multiplier' takes the value produced by the trigger and multiplies it
# Alternatively, you can use 'value' to count a specific number and not a multiplier
xp-gain-methods:
  - trigger: mine_block
    multiplier: 0.5 # You can also use "value" here (see above comment)
    conditions: [ ]
    filters:
      items:
        - "*wooden_pickaxe"
        - "*stone_pickaxe"
        - "*iron_pickaxe"
        - "*golden_pickaxe"
        - "*diamond_pickaxe"
        - "*netherite_pickaxe"
```

### The Additional Options Section

```yaml
# Custom placeholders to be used in descriptions,
# Don't add % to the IDs, this is done automatically
# The value takes a %level% placeholder and is a mathematical expression
level-placeholders:
  - id: "money"
    value: "%level% * 0.4"
  - id: "blocks"
    value: "ceil(10 - %level% / 10)"

# The text shown with the %effects% placeholder
# The number dictates the minimum level for this text to show for
# Adding new levels will override this text on those levels or above
effects-description:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

# Same as above, but for %rewards%
rewards-description:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"
```

### The Level Up Section
```yaml
# The message sent when the player levels up
level-up-messages:
  1:
    - "&8» &8Earn &a$%money%&8 for each &a%blocks%&8 blocks mined"

# Effects to run when the job levels up  
# %level% is the level the job leveled up to.  
# If you want to restrict this to certain levels, you can use  
# require: %level% = 20, or require: %level% < 50, etc.  
# If you want a reward to run every x levels, you can use  
# every: 1, or every: 12, etc  
level-up-effects:
  - id: give_item
    args:
      items:
        - diamond
      every: 5 # Gives the reward every 5 levels
      require: "%level% = 5" # Requires level 5 before receiving rewards
```

### The Effects Section
:::danger Effects Section

The effects section is the core functionality of the job. You can configure effects, conditions, filters, mutators and triggers in this section to run whilst the job is active.

Check out [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) to understand how to configure this section correctly.

For more advanced users or setups, you can configure chains in this section to string together different effects under one trigger. Check out [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) for more info.

:::
```yaml
# The effects for the job (i.e. the functionality)
# See here: https://plugins.auxilor.io/effects/configuring-an-effect
effects:
  - id: give_money
    args:
      every: "ceil(10 - %level% / 10)"
      amount: "0.4 * %level%"
    filters:
      items:
        - "*wooden_pickaxe"
        - "*stone_pickaxe"
        - "*iron_pickaxe"
        - "*golden_pickaxe"
        - "*diamond_pickaxe"
        - "*netherite_pickaxe"
    triggers:
      - mine_block

# The conditions required for the effects to activate
conditions: [ ]
```

## Internal Placeholders

| Placeholder       | Value                                                                    |
| ----------------- |--------------------------------------------------------------------------|
| `%level%`         | The player's job level. Useful for creating scaling effects              |
| `%level_numeral%` | The player's job level shown in Roman Numerals                           |
| `%level_x%`         | The player's job level, +/- a value. eg. `%level_-1%` is current level-1 |
| `%level_x_numeral%` | The player's job level, +/- a value, shown as Numerals                   |

<hr/>

## Default configs
The default configs can be found [here](https://github.com/Auxilor/EcoJobs/tree/master/eco-core/core-plugin/src/main/resources/jobs). <br/>
You can find additional user-created configs on [lrcdb](https://lrcdb.auxilor.io/).