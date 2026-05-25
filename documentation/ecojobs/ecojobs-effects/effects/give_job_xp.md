# `give_job_xp`
:::infoRequires:
EcoJobs
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Gives experience points for a certain job
# Effect Syntax
```yaml
- id: give_job_xp
  args:
    amount: 100 # The amount of xp to give
    job: miner # The job to give the xp for
  ...other config (eg triggers, filters, mutators, etc)
```