---
title: "PlaceholderAPI"
sidebar_position: 3
---

These are the placeholders EcoJobs exposes through PlaceholderAPI, for use anywhere PlaceholderAPI is supported. Replace `<id>` with a job ID. They let you show a player's job level, progress, and leaderboard standing on scoreboards, holograms, and chat.

| Placeholder                                        | Description                                            |
|----------------------------------------------------|--------------------------------------------------------|
| `%ecojobs_<id>%`                                   | Get the level that a player has for any given job      |
| `%ecojobs_<id>_name%`                              | Get the formatted name (icon and color) of any job     |
| `%ecojobs_<id>_active%`                            | Get if a player has the job active (true / false)      |
| `%ecojobs_<id>_percentage_progress%`               | Shows the percentage progress until the next job level |
| `%ecojobs_<id>_current_xp%`                        | Shows the current job XP                               |
| `%ecojobs_<id>_required_xp%`                       | Shows the job XP required for the next job             |
| `%ecojobs_<id>_total_players%`                     | Shows the total amount of players with this job active |
| `%ecojobs_total_job_level%`                        | Shows the level of all jobs combined                   |
| `%ecojobs_limit%`                                  | Shows the max amount of jobs a player can join at once |
| `%ecojobs_in_jobs%`                                | Shows the amount of jobs a player is currently in      |
| `%ecojobs_top_<id>_<position[0-9]>_<name/amount>%` | Leaderboard placeholder for job level                  |

<hr/>

## Where to go next

- **Make a job:** [How to make a Job](how-to-make-a-custom-job) to create the jobs these placeholders read from.
- **Commands:** [Commands and Permissions](commands-and-permissions) to manage the jobs and levels shown here.
