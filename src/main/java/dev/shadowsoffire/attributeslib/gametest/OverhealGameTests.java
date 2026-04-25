package dev.shadowsoffire.attributeslib.gametest;

import static dev.shadowsoffire.attributeslib.AttributesLib.MODID;

import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import dev.shadowsoffire.attributeslib.api.AttributeHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MODID)
@PrefixGameTestTemplate(false)
public final class OverhealGameTests {
    private OverhealGameTests() {}

    @GameTest(template = "overheal_debug", timeoutTicks = 20)
    public static void permanentOverhealModifierGrantsAbsorption(GameTestHelper helper) {
        helper.runAtTickTime(
                1,
                () -> {
                    ServerPlayer player = helper.makeMockServerPlayerInLevel();
                    AttributeHelper.modify(
                            player,
                            ALObjects.Attributes.OVERHEAL.asHolder(),
                            "test_overheal",
                            0.45D,
                            AttributeModifier.Operation.ADD_VALUE);
                    Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);

                    float before = player.getAbsorptionAmount();
                    target.hurt(helper.getLevel().damageSources().playerAttack(player), 10.0F);

                    if (player.getAttributeValue(ALObjects.Attributes.OVERHEAL.asHolder()) < 0.44D) {
                        helper.fail("mock player did not receive permanent overheal attribute");
                    }
                    if (player.getAbsorptionAmount() <= before) {
                        helper.fail("permanent overheal modifier did not grant absorption");
                    }
                });
        helper.runAtTickTime(2, helper::succeed);
    }

    @GameTest(template = "overheal_debug", timeoutTicks = 30)
    public static void itemOverhealModifierAppliesAndGrantsAbsorption(GameTestHelper helper) {
        Mob[] attackerRef = new Mob[1];

        helper.runAtTickTime(
                1,
                () -> {
                    Mob attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
                    attackerRef[0] = attacker;

                    ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
                    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                    builder.add(
                            ALObjects.Attributes.OVERHEAL.asHolder(),
                            new AttributeModifier(
                                    AttributesLib.loc("test_overheal_weapon"),
                                    0.45D,
                                    AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);
                    sword.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
                    attacker.setItemSlot(EquipmentSlot.MAINHAND, sword);
                });

        helper.runAtTickTime(
                5,
                () -> {
                    Mob attacker = attackerRef[0];
                    if (attacker == null) {
                        helper.fail("mock attacker was not created");
                    }

                    if (attacker.getAttributeValue(ALObjects.Attributes.OVERHEAL.asHolder()) < 0.44D) {
                        helper.fail("item modifier did not apply overheal to attacker");
                    }

                    Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 1, 1);
                    float before = attacker.getAbsorptionAmount();
                    target.hurt(helper.getLevel().damageSources().mobAttack(attacker), 10.0F);

                    if (attacker.getAbsorptionAmount() <= before) {
                        helper.fail("item-based overheal modifier did not grant absorption");
                    }
                });

        helper.runAtTickTime(6, helper::succeed);
    }
}
