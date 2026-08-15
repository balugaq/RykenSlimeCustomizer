# AGENTS.md — RykenSlimeCustomizer 操作宪章

> 本文件是 AI 编程助手与本仓库开发者的**操作契约**。所有改动（代码、构建、文档）均须遵守本文约定；
> 当本文与代码冲突时，按末尾《文档更新契约》处理。

## TL;DR（太长了不看）

1. 本仓库是 **Minecraft Paper + Slimefun4 附属插件**（Java 21 编译 / Gradle Kotlin DSL），它本身不是一个自带物品的附属，而是一个**通过 YAML 配置文件驱动的"附属生成器"**——每个"附属"（addon）是 `plugins/RykenSlimefunCustomizer/addons/` 下的一个文件夹，由一系列 `*.yml` 文件描述，RSC 启动时按类别读取并动态注册为 Slimefun 物品/机器。
2. 所有改动必须通过 `gradle build` 验收后才能提交，禁止提交未编译验证的代码。
3. 玩家可见文案在 `config.yml` 及各附属 `*.yml` 中直接以 `&` 色码 + CMI 颜色标签书写；日志一律走 `Debug` 工具类与 `getLogger()`；**禁止** `System.out.println`、**禁止** `git push -f`、**禁止**改动物品/Slimefun ID。

---

## 1. 项目身份与边界

| 维度 | 事实 |
| --- | --- |
| 定位 | Minecraft Paper 服务器插件，**Slimefun4（GuguProject fork）附属**，本质上是一个"配置驱动的自定义附属引擎"（YAML → Slimefun 物品/机器） |
| 主类 | `org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer`（`extends JavaPlugin implements SlimefunAddon`） |
| 版本目标 | Java 25 工具链编译为 release 21、Paper 1.20.1 运行目标（runServer）、`api-version: 1.19` |
| 产物 | `build/libs/RykenSlimeCustomizer-<version>.jar`（fat jar，Shadow 打包并 relocate libby / uniitem） |
| 核心依赖 | `depend: [Slimefun, GuizhanLibPlugin]`；大量 `softdepend`（各 Slimefun 附属 + NBTAPI + PlaceholderAPI + JustEnoughGuide） |
| AI 职责范围 | 编写/修改功能代码、Bug 修复、构建/CI 维护、文档维护 |
| AI 不负责 | 发布版本、推送上游（推送动作由开发者执行） |

### 1.1 代码包地图（改代码前先定位）

所有源码在根包 `org.lins.mmmjjkx.rykenslimefuncustomizer` 下：

| 包 | 职责 |
| --- | --- |
| `（根包）` | 插件主类 `RykenSlimefunCustomizer`（启动/关闭流程）+ `ProjectAddonManager`（附属加载管理器） |
| `addon` | 附属模型：`ProjectAddon`（单个附属的对象模型/注册项列表）、`ProjectAddonLoader`（**附属加载总入口**）、`AddonConfig`（附属自定义配置） |
| `readers` | **YAML 读取器基类 `YamlReader<T>`** 及各类别 Reader（ItemGroupReader / MenuReader / RecipeTypeReader / ResearchReader / GenerationReader / item.* / machine.*）——每个 Reader 对应一种配置文件类别 |
| `customs` | 各类 Slimefun 物品/机器实现类（`CustomItem`、`AbstractEmptyMachine`、各机器类）、`groups`（物品组）、`menu`（自定义菜单）、`script_machine`、`simple_machine`、`super_multiblock`（超大多方块）、`generations`（世界生成） |
| `bulit_in` | 内置物品/机器运行时组件：`BuiltInItems`、配方体系（`recipes.*`）、机器 Ticker（`tickers.*`）、输入/输出包装（`wrappers.*`）、`SaveditemsGroup`、`CommandSafe`、`WitherProofBlockImpl` |
| `commands` | `/rsc` 主命令 `MainCommand` |
| `listeners` | 插件级监听器：`DropFromBlockListener`、`RecipeViewListener`、`SuperMultiBlockListener`、`ScriptableEventListener`（脚本监听基类） |
| `events` | 附属生命周期事件：`AddonLoadEvent` / `AddonEnableEvent` / `AddonDisableEvent` |
| `script` | JS 脚本引擎：`ScriptEval`（抽象）、`JavaScriptEval`（GraalJS 实现）、`Depend`（依赖声明） |
| `updater` | 附属自动更新：`GitHubUpdater` / `GitHubRelease` |
| `integrations` | 外部插件集成：`NBTAPIIntegration` |
| `libraries` | 被 Shadow 重定位的第三方库的宿主：`colors`（CMIChatColor）、`libby`、`uniitem` |
| `utils` | 工具类：`Debug`、`CommonUtils`、`BlockMenuUtil`、`ClassUtils`（ByteBuddy 动态类）、`StackUtils`、`ZipUtils`、`Keys`、`Constants`（配置文件文件名常量）、`RecipeTypeMap`、`DropFromBlock` 等 |

---

## 2. 技术栈与项目结构（事实核对）

- **构建**：Gradle Kotlin DSL（`build.gradle.kts`）+ Shadow 插件 + run-paper；`tasks.build` 依赖 `shadowJar`；`tasks.jar.enabled = false`（产物只出 fat jar）。`processResources` 会对 `**/*.yml` 做 `${version}` 展开。
- **依赖仓库**：Maven Central + Sonatype + JitPack + CodeMC + Paper + 多个第三方仓库 + `lib/` 本地 JAR（`compileOnly(fileTree(...))`）。**无公司私服，不要新增私服配置。**
- **核心框架**：Paper API + Slimefun4 + GuizhanLibPlugin + Lombok（`compileOnly`+`annotationProcessor`）+ jspecify（`@NullMarked`/`@NonNull`/`@Nullable` 标注）+ ByteBuddy（运行时动态生成实现类）+ GraalJS（脚本引擎，`compileOnly` 由 Libby 运行时下载）+ Libby（`implementation`，打包并 relocate）+ uni-item（`implementation`）。
- **配置**：`src/main/resources/plugin.yml`（命令/权限/软硬依赖）+ `config.yml`（插件级配置：saveExample / debug / pluginUpdate / update.* / super-multi-block-stackable / repositories）+ 内置示例 `src/main/resources/addons/example/info.yml`。
- **运行时目录**：`plugins/RykenSlimefunCustomizer/addons/`（各附属文件夹）、`addon_configs/`（附属自定义配置）、`cache/`（脚本/库缓存）。
- **附属结构**：每个附属文件夹含 `info.yml`（元信息）+ 一系列类别配置文件（文件名见 `utils.Constants`）+ 可选 `scripts/`（JS 脚本）、`saveditems/`（保存的物品）、`default_config.yml`（附属默认配置）。
- **启动流程**（`RykenSlimefunCustomizer.onEnable()`，新增初始化逻辑须插入对应阶段，不要打乱顺序）：
  1. `INSTANCE = this` → Adventure 日志警告检查
  2. `CommonUtils.completeFile("config.yml")`
  3. `jeg = isPluginEnabled("JustEnoughGuide")`
  4. `addonManager = new ProjectAddonManager()`（创建 addons/addon_configs 目录）
  5. `smbm = new SuperMultiBlockManager()`（含静态异步 dirty 队列处理器）
  6. `saveExample()`（按配置）
  7. `getCommand(...).setExecutor(new MainCommand())`
  8. **`addonManager.setup()`**（加载 `addons/` 下所有附属，见第 3 节）
  9. 注册监听器：`DropFromBlockListener` / `RecipeViewListener` / `SuperMultiBlockListener`
  10. 为每个世界添加 `BlockPopulator`（世界生成）
  11. JEG 适配（`handleJEG()`，若启用）
  12. GuizhanBuildsUpdater（若配置启用更新）
  13. 1 tick 后 `runtime = true` → `handleLogitech()`（禁用 LogiTech 中特定机器的可堆叠属性）
- **`onLoad()`**：`setupLibraries()`（Libby 下载 GraalJS 等库）+ 设置缓存目录 + 关闭 polyglot 警告。
- **`onDisable()`**：移除所有世界的 `BlockPopulator`、置空 `addonManager`/`smbm`。
- **`onEnable` 之后的脚本缓存/生命周期**：`ScriptEval`/`JavaScriptEval` 使用 ThreadLocal GraalJS Context；`clearScriptCache()` 清除失败函数缓存。
- **CI**：`.github/workflows/build.yml`（JDK 25，`gradle build`）。

---

## 3. 加载链路（核心：附属如何被读取与注册）

这是本插件的灵魂，改动任何类别时都要先理解这条链：

```
RykenSlimefunCustomizer.onEnable()
  └─ ProjectAddonManager.setup()
       ├─ checkFiles()          // 检查配置文件是否放错位置
       └─ 遍历 addons/ 下每个文件夹
            └─ loadAddon(folder)
                 ├─ ProjectAddonLoader.load()
                 │    ├─ 读取 info.yml（id/name/version/depends/pluginDepends/repo/...）
                 │    ├─ Depend.load() 校验依赖
                 │    ├─ loadListenerScript()  （ByteBuddy 生成脚本监听器）
                 │    ├─ loadAddonConfig()     （default_config.yml → addon_configs/<id>/config.yml）
                 │    ├─ 创建并运行全部 Reader（见下表）
                 │    └─ ResearchReader 最后读取
                 └─ 触发 AddonEnableEvent / 加入 projectAddons 映射
```

**各类别 Reader → 配置文件 → 实现类对照表（改动前必查）：**

| 配置文件（Constants 常量） | Reader 入口类 | 产出/实现类 | 说明 |
| --- | --- | --- | --- |
| `groups.yml` | `readers.ItemGroupReader` | `customs.groups.BaseRSCItemGroup`（`RSCItemGroupJEG` / `RSCItemGroupLegacy`） | 物品组（菜单树），`BaseRSCItemGroup.create()` 按是否启用 JEG 选择实现 |
| `recipe_types.yml` | `readers.RecipeTypeReader` | `customs.CustomRecipeType` | 自定义配方类型，经 `RecipeTypeMap` 注册 |
| `menus.yml` | `readers.MenuReader` | `customs.menu.CustomMenu` | 自定义机器菜单（供 machines 引用），可克隆既有 BlockMenuPreset |
| `items.yml` | `readers.item.ItemReader` | `customs.CustomItem` | 普通物品（可加辐射/灵魂绑定/彩虹/防凋零/充能等属性，ByteBuddy 动态实现） |
| `mob_drops.yml` | `readers.item.MobDropsReader` | `customs.CustomMobDrop` | 生物掉落物 |
| `geo_resources.yml` | `readers.item.GeoResourceReader` | `customs.CustomGeoResource` | 地质资源 |
| `armors.yml` | `readers.item.ArmorReader` | `customs.CustomArmorPiece` | 盔甲（一组 4 件） |
| `capacitors.yml` | `readers.item.CapacitorsReader` | `customs.CustomCapacitor` | 电容 |
| `foods.yml` | `readers.item.FoodReader` | `customs.CustomFood` | 食物 |
| `machines.yml` | `readers.machine.MachineReader` | `ScriptMachine` / `ScriptMachineNoEnergy` / `CustomEnergyGenerator` | 脚本机器（含/无能量、能量发生器），基类 `AbstractEmptyMachine` |
| `generators.yml` | `readers.machine.GeneratorReader` | `customs.CustomGenerator` | 发电机 |
| `solar_generators.yml` | `readers.machine.SolarGeneratorReader` | `customs.CustomSolarGenerator` | 太阳能发电机 |
| `mat_generators.yml` | `readers.machine.MaterialGeneratorReader` | `AdvancedCustomMachine` | 材料发生器 |
| `recipe_machines.yml` | `readers.machine.RecipeMachineReader` | `AdvancedCustomMachine` | 配方机器 |
| `simple_machines.yml` | `readers.machine.SimpleMachineReader` | `customs.simple_machine.SimpleMachineFactory` | 简单机器（预设类型） |
| `mb_machines.yml` | `readers.machine.MultiBlockMachineReader` | `customs.CustomMultiBlockMachine` | 多方块机器 |
| `supers.yml` | `readers.machine.SuperReader` | （高级机器） | 由各实现类注册 |
| `template_machines.yml` | `readers.machine.TemplateMachineReader` | `AdvancedCustomMachine` | 模板机器 |
| `linked_recipe_machines.yml` | `readers.machine.LinkedRecipeMachineReader` | `AdvancedCustomMachine` | 链式配方机器 |
| `workbenches.yml` | `readers.machine.WorkbenchReader` | `customs.CustomWorkbench` | 工作台 |
| `super_multi_block_machines.yml` | `readers.machine.SuperMultiBlockMachineReader` | `customs.CustomSuperMultiBlockMachine` | 超大多方块机器 |
| `generations.yml` | `readers.GenerationReader` | `customs.generations.GenerationInfo` / `GenerationArea` | 世界生成（由 `BlockPopulator` 消费） |
| `researches.yml` | `readers.ResearchReader` | `io.github...Research` | 研究/解锁 |

### 3.1 各大类入口点详解（按类别）

> 每个类别 = 一个配置文件 + 一个 `readers.*` 下的 Reader + 一个 `customs.*` 下的实现类。
> "入口点"指：**配置文件 key → Reader 的 `readEach(key)` → 生成实现类并 `register()`** 的完整路径。
> 改动某类别时，按"配置文件字段 → Reader.readEach → 实现类字段/构造"三层定位。

| 大类 | 入口 Reader（`readEach` 产出） | 实现类入口 | 运行期入口 |
| --- | --- | --- | --- |
| 物品组 | `readers.ItemGroupReader` → 经 `BaseRSCItemGroup.create()` 选 JEG/Legacy | `customs.groups.BaseRSCItemGroup`（`RSCItemGroupJEG`/`RSCItemGroupLegacy`） | `BaseRSCItemGroup.readAction()`（按钮动作：link/console/open_itemgroup/display_slimefunitem/script）、`addContent(...)` |
| 配方类型 | `readers.RecipeTypeReader` → `CustomRecipeType` | `customs.CustomRecipeType` | `utils.RecipeTypeMap`（注册/清除），`YamlReader.getRecipe()` 读取配方 |
| 菜单 | `readers.MenuReader` → `CustomMenu` | `customs.menu.CustomMenu`（`RSCClickHandler`） | `CustomMenu.apply(ChestMenu)`；脚本 `onClick/onOpen/onClose` |
| 物品 | `readers.item.ItemReader` → `CustomItem`（ByteBuddy 动态注入 Radioactive/Soulbound/NotPlaceable/WitherProof/Rechargeable/PiglinBarterDrop/Rainbow） | `customs.CustomItem` | `CustomItem.constructorArgs()`；脚本 `onUse/onWeaponHit/onToolUse`；`drop_from` 走 `YamlReader.resolveDropFrom` |
| 生物掉落 | `readers.item.MobDropsReader` → `CustomMobDrop` | `customs.CustomMobDrop` | `DropFromBlockListener` 消费 `utils.DropFromBlock` |
| 地质资源 | `readers.item.GeoResourceReader` → `CustomGeoResource` | `customs.CustomGeoResource` | 实现 `GEOResource`，注册到 `Slimefun.getRegistry().getGEOResources()` |
| 盔甲 | `readers.item.ArmorReader` → `List<CustomArmorPiece>`（一组 4 件） | `customs.CustomArmorPiece` | 单件/整套属性 |
| 电容 | `readers.item.CapacitorsReader` → `CustomCapacitor` | `customs.CustomCapacitor` | 实现 `Rechargeable` |
| 食物 | `readers.item.FoodReader` → `CustomFood` | `customs.CustomFood` | 食用效果 |
| 脚本机器 | `readers.machine.MachineReader` → `ScriptMachine` / `ScriptMachineNoEnergy` / `CustomEnergyGenerator` | `customs.script_machine.*`、`customs.CustomEnergyGenerator` | 基类 `customs.AbstractEmptyMachine`（`getBlockTicker()`）；脚本 `onTick/onPlace/onBreak` 等 |
| 发电机 | `readers.machine.GeneratorReader` → `CustomGenerator` | `customs.CustomGenerator` | 实现 `EnergyGenerator` |
| 太阳能 | `readers.machine.SolarGeneratorReader` → `CustomSolarGenerator` | `customs.CustomSolarGenerator` | 实现 `EnergyGenerator` + 昼夜判定 |
| 材料发生器 | `readers.machine.MaterialGeneratorReader` → `AdvancedCustomMachine` | `customs.AdvancedCustomMachine` | `bulit_in.tickers.MaterialGeneratorMachineTicker` |
| 配方机器 | `readers.machine.RecipeMachineReader` → `AdvancedCustomMachine` | `customs.AdvancedCustomMachine` | `bulit_in.tickers.RecipeMachineTicker` |
| 简单机器 | `readers.machine.SimpleMachineReader` → `SimpleMachineFactory`（按 `SimpleMachineType` 预设） | `customs.simple_machine.SimpleMachineFactory` | 预设 ticker |
| 多方块 | `readers.machine.MultiBlockMachineReader` → `CustomMultiBlockMachine` | `customs.CustomMultiBlockMachine` | 实现 `MultiBlockMachine` |
| 高级（supers） | `readers.machine.SuperReader` → 各高级机器实现 | 由各实现类注册 | 自定义 |
| 模板机器 | `readers.machine.TemplateMachineReader` → `AdvancedCustomMachine` | `customs.AdvancedCustomMachine` | `bulit_in.tickers.TemplateRecipeMachineTicker` |
| 链式配方机器 | `readers.machine.LinkedRecipeMachineReader` → `AdvancedCustomMachine` | `customs.AdvancedCustomMachine` | `bulit_in.tickers.LinkedRecipeMachineTicker` |
| 工作台 | `readers.machine.WorkbenchReader` → `CustomWorkbench` | `customs.CustomWorkbench` | `bulit_in.tickers.WorkbenchMachineTicker` |
| 超大多方块 | `readers.machine.SuperMultiBlockMachineReader` → `CustomSuperMultiBlockMachine` | `customs.CustomSuperMultiBlockMachine` | `customs.super_multiblock.SuperMultiBlock` / `SuperMultiBlockManager`（由 `SuperMultiBlockListener` 驱动） |
| 世界生成 | `readers.GenerationReader` → `GenerationInfo`（含 `GenerationArea` 列表） | `customs.generations.GenerationInfo` / `GenerationArea` | `customs.generations.BlockPopulator`（`onEnable` 时加入每个世界） |
| 研究 | `readers.ResearchReader` → `Research`（最后读取） | `io.github.thebusybiscuit.slimefun4.api.researches.Research` | Slimefun 研究注册表 |

**Reader 基类 `YamlReader<T>`（所有 Reader 的公共骨架）：**
- `getFileName()`：返回对应配置文件文件名（子类实现，多返回 `Constants.XXX_FILE`）。
- `preload()`：先把各条目的 `SlimefunItemStack` 放入 `addon.preloadItems`（供后续交叉引用）。
- `readAll()` / `loadLateInits()`：读取（或延迟读取）每个 key 并调用子类 `readEach(section)`。
- `readEach(String s)`：**子类必须实现**——把单个配置条目解析为一个对象并 `register(...)`。
- `preloadItems(String s)`：子类决定该类别用哪种预加载（`blockPreloadItems`/`anyPreloadItems`/自定义）。
- `checkForRegistration(...)`：处理 `register.conditions`（hasplugin / itemexist / version / config.* 等）决定是否注册。
- `getBase(...)`：解析通用基础信息（item_group / item / recipe_type / recipe / recipeOutput），返回 `BaseResult` record。
- `getScriptOrNull(...)`：按 `script` 字段加载对应 JS 脚本。

---

## 4. 决策树：当遇到 A 时，执行 B

### 4.1 新增/修改一个"类别"（配置文件类型）

- **当需要新增一种配置类别（新的 `*.yml`）时**：在 `utils.Constants` 增加文件名常量 → 在 `readers` 新建 `XxxReader extends YamlReader<T>` 实现 `getFileName/readEach/preloadItems` → 在 `ProjectAddonLoader.load()` 中实例化并在 `preload/readAll/loadLateInits` 链中挂接 → 在 `ProjectAddon` 增加对应 `List<T>` 字段并在 `unregister()` 中清理。**不要绕过 `ProjectAddonLoader` 单独读取配置文件。**

### 4.2 新增/修改一个物品类别内的"属性"

- **当新增物品/机器时**：在对应的 Reader 的 `readEach` 中创建实现类并 `register(RykenSlimefunCustomizer.INSTANCE)`，无需修改 `addonManager`。新增属性时参照 `ItemReader.readEach` 用 `ClassUtils.generateClass`（ByteBuddy）动态注入接口（如 `Radioactive`/`Soulbound`/`NotPlaceable`/`WitherProofBlockImpl`/`Rechargeable`），不要手工写死多个子类。
- **当物品展示名/描述需要文案时**：直接在附属配置文件的 `item`/`lore` 中写（`&` 色码 + `<Theme>`/CMI 标签），代码里不硬编码文案；需要颜色时用 `CMIChatColor.translate(...)`。
- **当需要引用本附属或其他附属的已注册物品时**：用 `addon.getSfStack(id)` 或 `addon.getPreloadItems()`，ID 会经 `addon.getId(...)`/`idPattern` 处理。

### 4.3 脚本与监听

- **当需要给物品/机器绑定脚本逻辑时**：在对应 Reader 中用 `getScriptOrNull(section, "script")` 拿到 `JavaScriptEval`，再 `eval.evalFunction("onXxx", args...)` 调用脚本函数；脚本文件放附属 `scripts/*.js`。
- **当需要新增监听器**：改内置监听器（`listeners.*`）或让附属通过 `scriptListener` 字段 + `ScriptableEventListener` 基类生成脚本监听器（`ProjectAddonLoader.loadListenerScript`）。
- **当脚本报错时**：`JavaScriptEval` 会缓存失败函数；`/rsc clearScriptCache` 或重启可清除。不要绕过 `ScriptEval` 体系。

### 4.4 文案、配置与日志

- **当新增插件级配置项时**：在 `src/main/resources/config.yml` 加默认值（带注释），并在 `RykenSlimefunCustomizer` 中用 `getConfig().getXxx(...)` 读取；禁止散落魔法字符串。
- **当需要日志时**：用 `utils.Debug`（`Debug.info/warn/error/debug(...)`，注意 `Debug.debug(Supplier<String>)` 形式）或 `getLogger()`；**禁止** `System.out.println()` / 裸 `e.printStackTrace()`。
- **当需要给玩家发消息时**：`MainCommand.sendMessage(...)` / `CMIChatColor.translate(...)`；禁止散落 `§` 色码。

### 4.5 容器菜单与持久化

- **当需要操作容器菜单（推/取/检查物品）时**：统一用 `utils.BlockMenuUtil`（`pushItem` / `fits` / `consumeItem` 等），禁止手写堆叠合并逻辑。
- **当访问/修改已注册 Slimefun 方块持久化数据时**：使用 `StorageCacheUtils` / `SlimefunBlockData`（GuguProject 存储层），不要绕过；注意 `SuperMultiBlockManager` 会用反射替换 `menu` 字段。
- **当需要周期任务时**：`Bukkit.getScheduler().runTaskTimer(...)`；异步排队任务参照 `SuperMultiBlockManager` 静态块里的 `runTaskTimerAsynchronously` + 队列模式。

### 4.6 依赖、构建与软依赖集成

- **当新增 Maven 依赖时**：加进 `build.gradle.kts` `dependencies`（区分 `compileOnly` 与 `implementation`），并 `gradle build` 验证。
- **当涉及运行时动态加载库（GraalJS 等）时**：在 `RykenSlimefunCustomizer.setupLibraries()` 用 Libby 声明（`addMavenCentral()` + 各 `Library.builder()` + `loadLibrary`），并保持 `compileOnly` 引用。
- **当需要与软依赖插件交互时**：先判 `Bukkit.getPluginManager().isPluginEnabled(...)`，再 try/catch 包 `NoClassDefFoundError`；集成入口放在 `integrations.*`。
- **当构建失败时**：先读报错定位（依赖缺失 / 编译错误 / Shadow relocation 冲突），修复后重跑；**禁止**为绕过失败而硬跳过任务。

---

## 5. 红线（绝对禁止操作）

> 违反以下任一条都属于严重事故。AI 在执行任务时若发现可能触碰红线，必须停下并向开发者说明。

1. **禁止改动物品 ID / Slimefun ID**：删除、重命名、改变 ID 会破坏玩家存档/方块数据。**改动即视为破坏存档。**
2. **禁止 `git push -f` 或改写共享历史**：禁止 force push、rebase 改写、`git reset --hard` 后强推等任何改写历史的行为。
3. **禁止未构建验证就提交**：任何代码改动（含注释、文案、构建脚本）提交前必须通过 `gradle build`；禁止 `--offline`、跳过任务等绕过方式。
4. **禁止在生产路径使用 `System.out.println()`**：日志只能走 `Debug` 工具类与 `getLogger()`。
5. **禁止绕过注册/加载链路**：新增类别/物品必须走 `ProjectAddonLoader` + `YamlReader` 体系，禁止单独 `SlimefunItemStack` 注册绕过链路。
6. **禁止绕过脚本/监听体系**：新增脚本逻辑必须挂到 `ScriptEval`/`ScriptableEventListener`，禁止裸开线程或绕过缓存机制。
7. **禁止随意改动 `onEnable()`/`onDisable()` 的初始化/清理顺序**：加载顺序影响附属依赖解析与物品预加载，`addonManager.setup()` 必须在监听器注册与 `runtime=true` 之前。

---

## 6. 质量规范

- **代码风格**：跟随现有代码风格（Google 风格为主、4 空格缩进、`@NullMarked`/`@NonNull`/`@Nullable` 标注、Lombok 注解）。未配置 Checkstyle/SpotBugs/PMD，**不要**在本次任务中擅自引入新 linter 或格式化工具（Spotless 已在构建脚本中注释掉）。
- **Lombok**：项目已配置（`compileOnly` + `annotationProcessor`），新类可按需使用 `@Getter`/`@Setter`/`@RequiredArgsConstructor`/`@Data` 等；不要移除既有 Lombok 注解。
- **测试**：`build.gradle.kts` 已配置 `testImplementation(junit-jupiter)` + `testImplementation(mockito)` + `useJUnitPlatform()`；为纯逻辑类（无 Bukkit 运行时依赖）编写 JUnit 5 测试放在 `src/test/java`。涉及 Bukkit/Slimefun 运行时的代码不做单元测试，靠游戏内验证。
- **可编译性**：每个完成的改动都必须是"能编译、能运行"的完整状态；不要留下半成品/死代码/未使用 import。
- **向后兼容**：不得破坏玩家已有存档数据格式（物品 ID、数据库表结构、语言文件 key 结构）。

---

## 7. 常用 Gradle 命令速查表

> 本地推荐使用仓库 wrapper（`gradlew`）或系统 `gradle`；受限环境下可加 `--no-daemon --no-watch-fs`。

| 场景 | 命令 |
| --- | --- |
| 标准验收构建（必须通过，产出 fat jar） | `gradle build`（依赖 shadowJar） |
| 增量编译（快速检查） | `gradle compileJava` |
| 打 fat jar（含 relocation） | `gradle shadowJar` |
| 运行单元测试 | `gradle test` |
| 本地启动测试服务器（Paper 1.20.1） | `gradle runServer` |
| 查看依赖树 | `gradle dependencies` |
| 清理产物 | `gradle clean` |
| 产物位置 | `build/libs/RykenSlimeCustomizer-<version>.jar` |

---

## 8. Git 工作流

- **分支**：本地 `master`（或当前默认分支）直接开发，不建长命分支；推送上游由开发者执行。
- **提交信息**：Conventional Commits，格式 `type(scope): 描述`，中英文均可：
  - `feat:` 新功能/新类别/新机器
  - `fix:` Bug 修复
  - `docs:` 文档（含本文件）
  - `refactor:` 重构（行为不变）
  - `chore:` 构建/依赖/杂务
  - 例：`feat(reader): 新增 XXX 类别 Reader 支持`
- **提交前自检清单**：
  1. `gradle build` 通过；
  2. 未触碰第 5 节红线；
  3. 只提交相关文件（不提交 `build/`、`.gradle/`、IDE 临时文件、`run/`）。

---

## 9. 核心类 API 速查与 Contract

### 9.1 RykenSlimefunCustomizer — 主类（根包）

| 成员 | 用途 |
| --- | --- |
| `INSTANCE`（static） | 插件单例 |
| `addonManager`（static `ProjectAddonManager`） | 附属加载管理器 |
| `smbm`（`SuperMultiBlockManager`） | 超大多方块管理器 |
| `jeg`（static boolean） | 是否启用 JustEnoughGuide |
| `onLoad()` | `setupLibraries()` + 缓存目录 |
| `onEnable()` | 见第 2 节启动流程 |
| `reload()` | 重载 config + `addonManager.reload()` |
| `clearScriptCache()` / `clearDisplayProjectiles()` / `saveExample()` | 工具入口 |
| `allowUpdate(prjId)` | 判断某附属是否允许自动更新 |

### 9.2 ProjectAddonManager — 附属加载管理器（根包）

| 成员 | 用途 |
| --- | --- |
| `ADDONS_DIRECTORY` / `CONFIGS_DIRECTORY`（static File） | 附属/配置目录 |
| `setup()` | 遍历加载 `addons/` 下所有附属（总入口） |
| `loadAddon(File)` / `reloadAddon(addon)` / `unloadAddon(addon)` | 加载/重载/卸载单个附属 |
| `preloadAddon(File)` | 仅登记 ID、检查 info.yml |
| `reload()` | 全部卸载后重载 |
| `isLoaded(...)` / `get(id)` / `getAllAddons()` / `getAddonFolder(id)` | 查询 |
| `getPreaddRecipes(id)` / `addPreaddRecipe(...)` | 预添加配方 |
| `checkSC(File)` | 检测用户误放 SC 配置文件并提示 |

### 9.3 ProjectAddon — 附属对象模型（addon）

- 持有该附属所有已注册对象列表：`itemGroups` / `menus` / `geoResources` / `items` / `machines` / `researches` / `generators` / `materialGenerators` / `recipeMachines` / `multiBlockMachines` / `solarGenerators` / `mobDrops` / `capacitors` / `recipeTypes` / `simpleMachines` / `foods` / `armors` / `supers` / `templateMachines` / `linkedRecipeMachines` / `workbenches` / `superMultiBlockMachines` / `generationInfos`。
- `unregister()`：**卸载时把所有对象从 Slimefun 注册表移除**并清空列表（新增类别须在此补充清理）。
- `getId(configuredId, id_alias)` / `getSfStack(id)`：ID 规范化与物品栈查询。
- `getScriptsFolder()` / `getSavedItemsFolder()`：脚本/保存物品目录。

### 9.4 ProjectAddonLoader — 附属加载器（addon）

- `load()`：完整加载一个附属（见第 3 节链路），返回 `ProjectAddon` 或 null。
- `isLoadedOrTryLoad(depend)`：静态，加载依赖附属。
- `readYml(dir, file)`：读取 YAML（不存在则返回空配置）。

### 9.5 YamlReader<T> — 读取器基类（readers）

见第 3 节《Reader 基类》小节。关键抽象方法：`getFileName()` / `readEach(String)` / `preloadItems(String)`。

### 9.6 Debug — 日志/调试工具（utils，全部静态）

| 方法                                                                                     | 用途 |
|----------------------------------------------------------------------------------------| --- |
| `debug(...)` / `debug(Supplier<String>)`                                               | 开发期调试输出，仅在 `config.yml` `debug: true` 时打印 |
| `info(...)` / `warn(...)` / `error(...)`                                               | 常规/警告/错误日志（`error` 常带 Throwable） |
| `error(File, YamlConfiguration, msg, ...)` / `warn(File, YamlConfiguration, msg, ...)` | 带文件/配置上下文的错误输出 |
| `danger(...)`                                                                          | 高危操作提示（物品组按钮 console 命令用） |

Contract：所有方法仅产生日志副作用，不修改入参。

### 9.7 JavaScriptEval / ScriptEval — 脚本引擎（script）

- `ScriptEval`（抽象）：定义 `addThing` / `evalFunction` / `key` / `doInit` / `contextInit` 等。
- `JavaScriptEval`（GraalJS 实现）：`create(File, addon)` 工厂；`evalFunction(funName, args...)` 执行脚本函数；自动缓存/失败标记；`advancedSetup()` 注入 `SlimefunItems`/`SlimefunItem`/`StorageCacheUtils`/`SlimefunUtils`/`BlockMenu`/`BlockMenuUtil`/`PlayerProfile`/`Slimefun` 宿主对象。
- `clearScriptCache()`：清除失败函数缓存。

### 9.8 SuperMultiBlockManager — 超大多方块管理器（customs.super_multiblock）

- 静态存储：`coreStorage` / `monitoringLocations` / `correctLocations` / `projectiles`（Display 实体）。
- `startSuperMultiBlock` / `destroySuperMultiBlock` / `markDirty` / `processDirty`：启停与脏位置处理。
- 异步 dirty 队列在静态块中 `runTaskTimerAsynchronously` 消费。
- `onPlayerInteract` 由 `SuperMultiBlockListener` 调用。
- `getInstance()` → `RykenSlimefunCustomizer.INSTANCE.getSuperMultiBlockManager()`。

---

## 10. AI 对用户的回答规范

- **先一句话回答**：回答开头用一句话说清"我做了什么/结论是什么"（如"已修复 X：在 Y 中补了 Z"）。
- **再简短补充**：只补充代码/文件里看不出来的信息——决策依据、取舍、待确认事项、风险点。
- **不重复"代码可说明"的内容**：不要把刚写进代码/文档的东西再抄一遍；用户直接查看改动文件即可获得细节。
- **保持简短**：除非用户明确要求详细讲解，回答控制在几句话内。

---

## 11. 文档更新契约

1. **冲突即提示**：当 AI 发现本文与代码不一致时（例如：构建命令、Java 版本、加载链路、配置文件文件名、Reader 挂接与本文描述不符），**必须**在回复中主动指出冲突，并说明应以代码为准还是更新本文。
2. **惯例沉淀**：当本次任务产生了新的、可复用的约定（新的注册模式、新的类别、新的脚本规范）时，AI 应提议将其补充进本文对应章节（先提议，经开发者确认后修改）。
3. **保持精简**：更新本文时不得堆砌无行动含义的描述性文字；每一条规则都应能被"是否遵守"直接检查。
4. **变更记录**：修改本文后，提交信息使用 `docs(agents): ...`，并在提交说明中一句话概括变更点。
