/**

 * 访问与 AT：{@code GameRenderer} 使用 {@code setPostEffect}/{@code clearPostEffect}/

 * {@code currentPostEffect} 与 public 字段 {@code effectActive}；{@code ServerLevel#blockEntityTickers}

 * 在开发映射下可直接遍历，无需 Mixin Accessor，亦未再单独维护 accesstransformer（若你的工具链

 * 下某成员不可见，再补 META-INF 即可）。

 * <p>

 * Mixin 保留策略（与 NeoForge 事件/客户端 API 对照，26.1）：

 * <ul>

 *   <li><b>LivingEntityFoliageArmorMixin</b>：在 {@code hurtServer} 内对胸甲先扣耐、再缩放对玩家伤害、并改写

 *   {@code doHurtEquipment} 的槽位数组以避免双重胸甲扣耐。用 {@code LivingDamageEvent} / {@code LivingIncomingDamageEvent} /

 *   {@code ArmorHurtEvent} 难以在<strong>同一次伤害</strong>中保证与 {@code invulnerable}、无敌帧、盾反等

 *   的调用序完全一致，<strong>继续保留 Mixin</strong>。</li>

 *   <li><b>EquipmentWearDurabilityBarMixin</b>：改 {@code ItemStack} 的耐久条三方法。若将来按物品用

 *   {@code IClientItemExtensions} 能覆盖同等展示，可再收束；当前<strong>继续保留 Mixin</strong>

 *   （除非为每类注册物品加扩展，改动面大）。</li>

 * </ul>

 */

@org.jetbrains.annotations.ApiStatus.Internal

package com.theconstant.dontstravemc.mixin;

