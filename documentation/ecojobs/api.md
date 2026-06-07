---
title: "API"
sidebar_position: 6
---

This page is for developers who want to hook into EcoJobs from their own plugin, e.g. to read job levels or react to job events. EcoJobs is open-source, so you can read the full implementation alongside the API.

## Source code

The full source is on GitHub at [Auxilor/EcoJobs](https://github.com/Auxilor/EcoJobs).

## Adding the dependency

1. Add the Auxilor repository to your `build.gradle.kts`:
2. Add EcoJobs as a `compileOnly` dependency, replacing `<version>` with the version you want:

```kotlin
repositories {
    maven("https://repo.auxilor.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.willfp:EcoJobs:<version>")
}
```

The latest version available on the repo can be found [here](https://github.com/Auxilor/EcoJobs/tags).

<hr/>

## Where to go next

- **Shared APIs:** most cross-plugin APIs live in the [eco framework](https://github.com/Auxilor/eco).
- **Config-side setup:** [How to make a Job](how-to-make-a-custom-job) for building jobs without code.