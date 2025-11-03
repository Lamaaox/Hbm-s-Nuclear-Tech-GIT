package com.hbm.items.gear;

import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.ArmorSets;
import com.hbm.lib.RefStrings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ArmorAntiSchrabbidium extends ItemArmor implements ISpecialArmor {

	public ArmorAntiSchrabbidium(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.COMBAT);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if(stack.getItem().equals(ArmorSets.anti_schrabidium_helmet) || stack.getItem().equals(ArmorSets.anti_schrabidium_plate) || stack.getItem().equals(ArmorSets.anti_schrabidium_boots)) {
			return (RefStrings.MODID + ":textures/armor/anti_schrabidium_1.png");
		}
		if(stack.getItem().equals(ArmorSets.anti_schrabidium_legs)) {
			return (RefStrings.MODID + ":textures/armor/anti_schrabidium_2.png");
		}
		return null;
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase entity, ItemStack armor, DamageSource source, double damage, int slot) {
		// Complete invulnerability for ANY entity wearing full set
		if(hasFullArmorSet(entity)) {
			// Maximum priority, complete absorption, infinite damage capacity
			return new ArmorProperties(Integer.MAX_VALUE, Double.MAX_VALUE, Integer.MAX_VALUE);
		}
		return new ArmorProperties(0, 0, 0);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		if(slot == 0)
		{
			return 3;
		}
		if(slot == 1)
		{
			return 8;
		}
		if(slot == 2)
		{
			return 6;
		}
		if(slot == 3)
		{
			return 3;
		}
		return 0;
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		// Armor is indestructible - no damage
		stack.damageItem(0, entity);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}
	
	public static boolean hasFullArmorSet(EntityLivingBase entity) {
		if(entity instanceof EntityPlayer) {
			return ArmorUtil.checkArmor((EntityPlayer)entity, ArmorSets.anti_schrabidium_helmet, ArmorSets.anti_schrabidium_plate, ArmorSets.anti_schrabidium_legs, ArmorSets.anti_schrabidium_boots);
		}
		// Check for non-player entities
		ItemStack helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		ItemStack legs = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		ItemStack boots = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		
		return helmet != null && helmet.getItem() == ArmorSets.anti_schrabidium_helmet &&
		       chest != null && chest.getItem() == ArmorSets.anti_schrabidium_plate &&
		       legs != null && legs.getItem() == ArmorSets.anti_schrabidium_legs &&
		       boots != null && boots.getItem() == ArmorSets.anti_schrabidium_boots;
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		if(hasFullArmorSet(player))
		{
			// Maximum regeneration and resistance
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5, 127, true, false));
			
			// Nullify all negative effects
			player.removePotionEffect(MobEffects.POISON);
			player.removePotionEffect(MobEffects.WITHER);
			player.removePotionEffect(MobEffects.INSTANT_DAMAGE);
			player.removePotionEffect(MobEffects.SLOWNESS);
			player.removePotionEffect(MobEffects.MINING_FATIGUE);
			player.removePotionEffect(MobEffects.NAUSEA);
			player.removePotionEffect(MobEffects.BLINDNESS);
			player.removePotionEffect(MobEffects.HUNGER);
			player.removePotionEffect(MobEffects.WEAKNESS);
			player.removePotionEffect(MobEffects.LEVITATION);
			player.removePotionEffect(MobEffects.UNLUCK);
			
			// Prevent fall damage
			if(player.motionY < -0.25D)
			{
				player.motionY = -0.25D;
				player.fallDistance = 0;
			}
			
			// Keep health at maximum
			if(player.getHealth() < player.getMaxHealth()) {
				player.setHealth(player.getMaxHealth());
			}
			
			// Keep hunger at maximum
			if(player.getFoodStats().getFoodLevel() < 20) {
				player.getFoodStats().setFoodLevel(20);
			}
			player.getFoodStats().setFoodSaturationLevel(20.0F);
			
			// Extinguish fire
			if(player.isBurning()) {
				player.extinguish();
			}
			
			// Prevent void damage - teleport up if below Y=0
			if(player.posY < -64) {
				player.setPositionAndUpdate(player.posX, 100, player.posZ);
				player.fallDistance = 0;
			}
			
			// Clear bad potion effects from other mods
			player.clearActivePotions();
			
			// Apply omnipotent effects
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 5, 127, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, 10, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 5, 10, true, false));
			player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 5, 10, true, false));
			
			// Make player invulnerable
			player.capabilities.disableDamage = true;
			player.setEntityInvulnerable(true);
		}
	}
	
	@Override
	public void setDamage(ItemStack stack, int damage) {
		// Armor is indestructible
	}
	
	@Override
	public int getDamage(ItemStack stack) {
		return 0;
	}
	
	@Override
	public int getMaxDamage() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}
	
}
