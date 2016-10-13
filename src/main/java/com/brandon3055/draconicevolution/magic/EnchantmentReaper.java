package com.brandon3055.draconicevolution.magic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * Created by Brandon on 17/11/2014.
 */
public class EnchantmentReaper extends Enchantment {
	public EnchantmentReaper(int id) {
		super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});

		//super(id, 2, EnumEnchantmentType.WEAPON);
		setName("draconicevolution.reaperEnchant");
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public int getMinEnchantability(int level) {
		return 1 + 10 * (level - 1);
	}

	@Override
	public int getMaxEnchantability(int level) {
		return super.getMinEnchantability(level) + 50;
	}
}
