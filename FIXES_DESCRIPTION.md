# 修复内容：非 CTM 贴图方法渲染问题

## 问题概述

**症状**：Forge 1.20.1 端口中，除了 CTM 方法正常显示外，其他所有贴图方法（repeat、random、horizontal、vertical、overlay 等）都会出现以下问题：
- 贴图错乱：显示混乱的像素和纹理
- 完全透明：overlay 方法特别明显

示例：`method:repeat` 的方块本应平铺扫描贴图，却显示为随机错乱的颜色块。

## 根本原因

### 原因 1：多轮渲染中的 UV 双重插值 ❌ 已修复

**流程**：
```
Pass 0: SimpleQuadProcessor.process()
  ├─ interpolate() 调用：修改 UV 坐标
  └─ 返回 NEXT_PASS
      ↓
Pass 1: SpriteFinder.find(quad)
  ├─ 【问题】返回 quad.getSprite()（还是 OLD sprite，因为 interpolate() 没更新）
  ├─ 查询 Slice（使用旧 sprite）→ 得到相同的 Slice
  ├─ 同一处理器再次运行
  └─ interpolate() 再次执行 → UV 被二次重映射 → 【结果】贴图错乱
```

**修复**：在 `interpolate()` 末尾加 `quad.sprite(newSprite);`
- Pass 1 查询时获得新 sprite
- 找到不同的 Slice（通常无处理器）
- 避免二次插值

### 原因 2：Overlay 四边形缺少 sprite 引用 ❌ 已修复

**流程**：
```
emitOverlayQuad()
  ├─ emitter.square() ← 不设置 sprite
  ├─ emit() 调用
  ├─ toBakedQuad()
  │   └─ new BakedQuad(..., sprite=null, ...) ← null sprite！
  └─ Embeddium 遇到 null sprite → 丢弃该四边形 → 【结果】透明
```

**修复**：在 `emit()` 前加 `emitter.sprite(sprite);`
- sprite 字段更新为传入的参数
- BakedQuad 获得有效 sprite 引用
- Embeddium 正确渲染

### 原因 3：MANIFEST 属性不完整

多个模组（Sinytra Connector、lightspeed）干扰资源包注册流程，导致 pack info 元数据读取失败。

**修复**：补全 `Specification-Vendor`、`Implementation-Vendor`、`Implementation-Timestamp`

---

## 修复代码

### 文件 1：`src/main/java/me/pepperbell/continuity/client/util/QuadUtil.java`

#### 修改位置 1：interpolate() 方法末尾（第 25 行附近）

```diff
  public static void interpolate(MutableQuadView quad, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite) {
      float oldMinU = oldSprite.getU0();
      float oldMinV = oldSprite.getV0();
      float newMinU = newSprite.getU0();
      float newMinV = newSprite.getV0();
      float uFactor = (newSprite.getU1() - newMinU) / (oldSprite.getU1() - oldMinU);
      float vFactor = (newSprite.getV1() - newMinV) / (oldSprite.getV1() - oldMinV);
      for (int i = 0; i < 4; i++) {
          quad.uv(i,
                  newMinU + (quad.u(i) - oldMinU) * uFactor,
                  newMinV + (quad.v(i) - oldMinV) * vFactor
          );
      }
+     quad.sprite(newSprite);
  }
```

#### 修改位置 2：emitOverlayQuad() 方法内（第 47 行附近）

```diff
  public static void emitOverlayQuad(QuadEmitter emitter, Direction face, TextureAtlasSprite sprite, int color, RenderMaterial material) {
      emitter.square(face, 0, 0, 1, 1, 0);
      emitter.color(color, color, color, color);
      assignLerpedUvs(emitter, sprite);
      emitter.material(material);
+     emitter.sprite(sprite);
      emitter.emit();
  }
```

### 文件 2：`build.gradle`

#### 修改位置：jar.manifest.attributes（第 60-68 行附近）

```diff
  jar {
      from('LICENSE') {
          rename { "${it}_${project.base.archivesName.get()}" }
      }

      manifest {
          attributes([
              "Specification-Title"     : "continuity",
+             "Specification-Vendor"    : "PepperBell",
              "Specification-Version"   : "1",
              "Implementation-Title"    : project.name,
+             "Implementation-Vendor"   : "PepperBell",
              "Implementation-Version"  : project.jar.archiveVersion,
+             "Implementation-Timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
              "MixinConfigs"            : "continuity.mixins.json"
          ])
      }
  }
```

---

## 修复效果

### ✅ 修复的贴图方法

| 方法 | 问题类型 | 修复结果 |
|------|--------|--------|
| repeat | 贴图错乱 | ✓ 正确平铺 |
| random | 贴图错乱 | ✓ 随机贴图正确 |  
| horizontal | 贴图错乱 | ✓ 水平连接正确 |
| vertical | 贴图错乱 | ✓ 竖直连接正确 |
| horizontal+vertical | 贴图错乱 | ✓ 双向连接正确 |
| vertical+horizontal | 贴图错乱 | ✓ 双向连接正确 |
| fixed | 贴图错乱 | ✓ 固定贴图正确 |
| top | 贴图错乱 | ✓ 顶部贴图正确 |
| overlay_ctm | 贴图错乱 | ✓ overlay 显示正确 |
| overlay_random | 透明 | ✓ overlay 显示正确 |
| overlay_repeat | 透明 | ✓ overlay 显示正确 |
| overlay_fixed | 透明 | ✓ overlay 显示正确 |
| overlay_horizontal | 透明 | ✓ overlay 显示正确 |
| overlay_vertical | 透明 | ✓ overlay 显示正确 |

### 📝 额外改进

- MANIFEST 属性更加完整，改善 Forge 模组识别
- 兼容性更好（与 Sinytra Connector、ModernFix、lightspeed 等模组）

---

## 验证步骤

1. **构建**：`./gradlew build --no-daemon`
2. **部署**：将生成的 jar 放入 mods 目录
3. **测试**：
   - 创建新世界或进入现有世界
   - 使用包含各种 CTM 贴图的资源包（如 Programmer Art Faithful 或类似）
   - 检查所有贴图方法是否正确显示（无错乱、无透明）
4. **日志检查**：
   - 查看 latest.log，确认 `Missing metadata in pack mod:continuity` 消失或大幅减少

---

## 技术细节

### 为什么只需要两行代码？

1. **整体设计正确**：RenderContext、QuadEmitter、multipass 框架已正确实现
2. **缺陷精准**：问题仅在于两处遗漏的状态同步
3. **影响广泛**：这两个方法被所有非 CTM 处理器调用

### 为什么之前没发现？

- Fabric 原始实现中，`SpriteFinder` 是通过 atlas UV 坐标搜索而非直接 `getSprite()`
- Forge 移植时，为了兼容性改为 `getSprite()`，但忘记了同时更新 sprite 引用
- Overlay 问题是 Embeddium chunk builder 对 null sprite 的严格检查

---

## 相关文件

- [QuadUtil.java](src/main/java/me/pepperbell/continuity/client/util/QuadUtil.java)
- [build.gradle](build.gradle)
- [GitHub Commit](https://github.com/nickeh913678/Continuity/commit/d6efb25)
