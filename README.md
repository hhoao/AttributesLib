## 概述

AttributesLib 是一个库模组，该模组来源于 Apothic Attributes。

功能主要与各项属性相关。

## 属性界面

AttributesLib 提供了一个属性 GUI，可通过物品栏中的剑按钮访问。此 GUI 显示附加到玩家的所有属性。

可以筛选显示的属性以仅显示修改过的，也就是不同于原版的属性，将鼠标悬停在属性上将显示描述和详细信息。

## 护甲计算

为了支持护甲穿透、护甲撕碎、保护穿透和保护撕碎，AttributesLib 改变了护甲和保护减少伤害计算的方式。

在原版中，护甲会根据你的护甲韧性值和传入的伤害值来确定减少伤害的数值，这使得伤害减少与护甲的函数是非线性的。模组会将公式还原为线性，也即 DR = 50 /（50 +护甲），其中的分母 （50+护甲） 就是被改变为线性的部分。

但这个公式整体并不是线性，甚至也不是反比例。

护甲韧性属性会降低敌方护甲撕碎和护甲穿孔的效力，但不会减少所受到的伤害。

## 附魔工具提示

另一个功能是对附魔工具提示的改动。描述将被添加到提供了药水效果，而不是简单地提供属性修饰的药水物品中，当你将鼠标指针悬停在药水效果的图标上时，工具提示上将显示这些描述（以及属性修饰，也即带来的属性变化）。

## 属性格式

AttributesLib 允许属性为自己的工具提示设置自定义的格式。具有自定义格式的属性最显著的示例是那些基于百分比的属性，无论其修饰词缀类型如何，它始终以百分比形式显示其值。比如，原版属性速度和击退抗性以基于百分比的模式显示。

此功能的另一部分是附加工具提示信息。启用高级工具提示 （F3+H） 后将显示有关修饰词缀类型和基础数值的其他信息。

## 属性扩展
AttributesLib 扩展了很多的属性，要使用这些属性可以依赖该库并引用相关属性:
* draw_speed: 远程武器充能速度
* arrow_damage: 箭矢伤害
* arrow_velocity: 箭矢速度
* armor_pierce: 护甲穿透
* armor_shred: 百分比护甲穿透
* cold_damage: 寒冰伤害
* crit_chance: 暴击几率
* crit_damage: 暴击伤害
* current_hp_damage: 当前生命百分比伤害
* fire_damage: 火焰伤害
* life_steal: 生命吸取
* overheal: 物理攻击转化为吸收生命值的百分比
* mining_speed: 挖掘速度
* experience_gained: 经验获取
* ghost_health: 当没有受到伤害时，额外的生命值恢复
* healing_received: 治疗效果
* dodge_chance: 闪避几率
* prot_pierce: 护甲穿透防护
* prot_shred: 百分比护甲穿透防护
* elytra_flight: 鞘翅飞行
* creative_flight: 创造模式飞行
* horse.jump_strength: 马跳跃力量
* zombie.spawn_reinforcements: 僵尸支援
