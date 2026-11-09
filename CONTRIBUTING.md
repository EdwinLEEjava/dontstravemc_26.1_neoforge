# 贡献指南

## 分支模型

| 分支 | 用途 | 规则 |
|------|------|------|
| `main` | 稳定版，每个提交都可发布 | 受保护，只接受来自 `dev`（或紧急 `hotfix/*`）的 PR |
| `dev` | 日常开发集成 | 受保护，只接受功能分支的 PR |
| `feat/<名称>` | 新功能，例如 `feat/pigman-trade` | 从 `dev` 拉出，PR 回 `dev` |
| `fix/<名称>` | Bug 修复 | 从 `dev` 拉出，PR 回 `dev` |
| `hotfix/<名称>` | 线上版本紧急修复 | 从 `main` 拉出，PR 回 `main`，再同步回 `dev` |

```bash
git checkout dev
git pull
git checkout -b feat/my-feature
# ……开发、提交……
git push -u origin feat/my-feature
# 然后在 GitHub 上向 dev 发起 PR
```

## 提交信息

使用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)：

- `feat: 添加猪人交易`
- `fix: 修复浆果丛在雪地不生长`
- `refactor:` / `perf:` / `docs:` / `chore:` / `assets:`（贴图、模型、动画）

## 本地开发

需要 JDK 25。

```bash
./gradlew runClient   # 启动测试客户端
./gradlew runData     # 重新生成数据（模型、语言、配方等）
./gradlew build       # 构建 jar，输出在 build/libs/
```

## PR 要求

- `./gradlew build` 必须通过（CI 会自动检查）
- 至少一位维护者 Approve 后才能合并
- 一个 PR 只做一件事；涉及游戏内容请附截图
- 不要提交反编译的 Minecraft / 第三方源码、日志或 `run/` 目录

## 发版流程（维护者）

1. 把 `dev` 通过 PR 合并到 `main`
2. 修改 `gradle.properties` 里的 `mod_version`
3. 在 `main` 上打标签并推送：`git tag v0.3.1 && git push origin v0.3.1`
4. GitHub Actions 会自动构建并发布到 [Releases](../../releases)
