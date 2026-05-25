---
title: "Commands and Permissions"
sidebar_position: 4
---

| Command                                   | Description                                          | Permission               |
|-------------------------------------------|------------------------------------------------------|--------------------------|
| `/ecojobs reload`                         | Reloads the plugin                                   | `ecojobs.command.reload` |
| `/ecojobs unlock <player> <job>`          | Unlocks a job for a player                           | `ecojobs.command.unlock` |
| `/ecojobs reset <player/all> <job/all>`   | Reset the job for a player                           | `ecojobs.command.reset`  |
| `/ecojobs givexp <player> <job> <amount>` | Give job xp for a player                             | `ecojobs.command.givexp` |
| `/jobs`                                   | Opens the jobs GUI                                   | `ecojobs.command.jobs`   |
| `/jobs join <job>`                        | Joins a job                                          | `ecojobs.command.join`   |
| `/jobs leave <job>`                       | Leaves a job                                         | `ecojobs.command.leave`  |
| `/ecojobs import <id>`                    | Import a job from [lrcdb](https://lrcdb.auxilor.io/) | `ecojobs.command.import` |
| `/ecojobs export <id>`                    | Export a job to [lrcdb](https://lrcdb.auxilor.io/)   | `ecojobs.command.export` |

### Additional Permissions

| Permission                         | Description                                                                                         |
|------------------------------------|-----------------------------------------------------------------------------------------------------|
| `ecojobs.limit.<limit>`            | Limit the amount of jobs a player can join at once                                                  |
| `ecojobs.xpmultiplier.<%increase>` | Multiply job XP gain. The math is `1 + (<%increase> / 100)`. Example: `200` = 3x XP, `50` = 1.5x XP |
| `ecojobs.xpmultiplier.50percent`   | Gives 50% more job XP (1.5x multiplier)                                                             |
| `ecojobs.xpmultiplier.double`      | Gives double job XP (2x multiplier)                                                                 |
| `ecojobs.xpmultiplier.triple`      | Gives triple job XP (3x multiplier)                                                                 |
| `ecojobs.xpmultiplier.quadruple`   | Gives quadruple job XP (4x multiplier)                                                              |
