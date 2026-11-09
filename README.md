# TheConstant（dontstravemc）

[![Build](https://github.com/EdwinLEEjava/dontstravemc_26.1_neoforge/actions/workflows/build.yml/badge.svg?branch=dev)](https://github.com/EdwinLEEjava/dontstravemc_26.1_neoforge/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/EdwinLEEjava/dontstravemc_26.1_neoforge?include_prereleases)](https://github.com/EdwinLEEjava/dontstravemc_26.1_neoforge/releases)

把《饥荒》风格的内容带进 Minecraft 的 NeoForge 模组。

## 下载与安装（玩家）

1. 安装 Minecraft **26.1** 与 **NeoForge 26.1.x**
2. 安装前置 **GeckoLib 5.5**
3. 从 [Releases](https://github.com/EdwinLEEjava/dontstravemc_26.1_neoforge/releases) 下载最新的 `dontstravemc-*.jar`，放入 `mods` 文件夹

想尝鲜开发版：进入 [Actions](https://github.com/EdwinLEEjava/dontstravemc_26.1_neoforge/actions/workflows/build.yml)，打开 `dev` 分支最新一次成功的构建，下载底部 Artifacts（需登录 GitHub，可能不稳定）。

## 参与开发

- 日常开发在 `dev` 分支，`main` 只放稳定版本
- 新功能请从 `dev` 拉出 `feat/xxx` 分支并提交 PR，详见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 需要 JDK 25；`./gradlew runClient` 启动测试客户端

## 许可

All Rights Reserved（保留所有权利），未经作者许可请勿转载或二次发布。
