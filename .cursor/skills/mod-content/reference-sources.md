# 本地源查找

版本只来自仓库根目录 `gradle.properties`：`minecraft_version`、`neo_version`、`geckolib_version`。文件名用下面的列举结果，不要把版本号写进技能或命令里当永久路径。

## 合并后的 Minecraft

目录：`build/moddev/artifacts/`

选取文件名同时包含 `minecraft-patched-`、当前 `neo_version`、以及 `-merged.jar` 的那一个。26.x 合并包里的源码使用未混淆名。

该目录为空时，从仓库根运行会填充 moddev artifacts 的 Gradle 任务，然后再列目录。

只知短类名时：

```text
jar tf "<上一步选定的 merged.jar>" | findstr SimpleName
```

合并包内若有 `*.java`，直接读该条目。

## Gradle 缓存

根目录：`%USERPROFILE%/.gradle/caches/modules-2/files-2.1/`。若环境变量 `GRADLE_USER_HOME` 已设置，把前缀换成该目录。

Maven 坐标 `group:artifact:version` 的布局：

```text
<group 以点分段>/<artifact>/<version>/<content-hash>/<file>
```

同一坐标同时有二进制包和 `*-sources.jar` 时，读 Java 优先打开 `*-sources.jar`。

| 依赖 | 路径 |
|---|---|
| NeoForge | `net/neoforged/neoforge/<neo_version>/` |
| GeckoLib | `com/geckolib/geckolib-neoforge-<minecraft_version>/<geckolib_version>/` |

GeckoLib 坐标与 `build.gradle` 中的 `com.geckolib:geckolib-neoforge-${minecraft_version}:${geckolib_version}` 一致。组或目录对不上时，在 `files-2.1` 下按文件夹名 `geckolib` 搜索，仍用 `gradle.properties` 里的版本。

JDK 标准库不在此查找范围内。
