package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityCoreCreativeInjector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerCoreCreativeInjector extends Container {

	private TileEntityCoreCreativeInjector injector;
	
	public ContainerCoreCreativeInjector(InventoryPlayer invPlayer, TileEntityCoreCreativeInjector tedf) {
		injector = tedf;
		// No slots - this is a fuel selector GUI only
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2)
    {
		return ItemStack.EMPTY;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return injector.isUseableByPlayer(player);
	}
}
