---
title: "How to Make a Job Category"
sidebar_position: 2
---

Job categories split jobs into separate groups, such as classes and races, each with its own GUI, command and job limit. Jobs in one category do not count towards the limit of another. This page covers making one from scratch.

## Quick start

1. Open the `/categories/` folder inside the EcoJobs plugin folder.
2. Copy `_example.yml` and rename it to your category's ID, e.g. `classes.yml`.
3. Set the `name`, the `limit` and the `command` that opens it.
4. Add `category: classes` to each job config that belongs to it.
5. Run `/ecojobs reload`, then `/classes` to open the category's GUI.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real category. You can also organise categories into subfolders inside `categories/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the category's ID. This is what jobs reference with their `category` option, and what the limit permission and placeholders use.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the category will not load.
:::

## The structure of a category

| Part | What it controls |
| --- | --- |
| **Info** | The category's name and job limit |
| **Command** | The command that opens the category's GUI |
| **GUI** | Changes to the jobs GUI for this category |

```yaml
# === Info: name and job limit ===
name: "&6Classes" # The display name of the category.
limit: 1 # The most jobs in this category a player can have active at once.

# === Command: how players open it ===
command: classes # Optional. A command that opens this category's jobs GUI.

# === GUI: changes to the jobs GUI ===
gui: # Optional. Replaces sections of the gui section in config.yml.
  title: "Classes &7(&f%page%&7/&f%max_page%&7)"
```

### Info

The `limit` works like `jobs.limit` in `config.yml`, but only counts jobs in this category. Give a player a higher limit with the `ecojobs.limit.<category>.<number>` permission, e.g. `ecojobs.limit.classes.2`.

```yaml
name: "&6Classes" # The display name of the category.
limit: 1 # The most jobs in this category a player can have active at once.
```

### Command

The command opens a jobs GUI that lists only this category's jobs. It uses the `ecojobs.command.jobs` permission, like `/jobs`.

```yaml
command: classes # Optional. A command that opens this category's jobs GUI.
```

:::info
`/jobs` lists only the jobs with no category, and `jobs.limit` only counts those jobs. With no categories, every job stays in `/jobs` as before.
:::

### GUI

The category's GUI uses the `gui` section from `config.yml`. Any key set here replaces the matching key there, and each key replaces the whole section under it, so copy the full section you want to change.

```yaml
gui:
  title: "Classes &7(&f%page%&7/&f%max_page%&7)"
  player-info: # The whole player-info section, copied from config.yml and changed.
    ...
```

## Internal placeholders

| Placeholder | Description |
| --- | --- |
| `%ecojobs_limit_<category>%` | The most jobs in the category the player can have active at once |
| `%ecojobs_in_jobs_<category>%` | The jobs in the category the player has active |

:::tip Troubleshooting
- **Category command does nothing?** Check that `command` is set and does not clash with another plugin's command, then reload.
- **A job still shows in `/jobs`?** Its `category` must match the category's file name exactly.
- **Players can join more jobs than the limit?** Check for an `ecojobs.limit.<category>.<number>` permission giving them a higher one.
:::

<hr/>

## Where to go next

- [How to make a Job](how-to-make-a-custom-job): add jobs to your category with `category: <id>`.
- [PlaceholderAPI](placeholderapi): show category limits in GUIs and scoreboards.
- **Defaults:** the example category lives [here](https://github.com/Auxilor/EcoJobs/tree/master/eco-core/core-plugin/src/main/resources/categories).
