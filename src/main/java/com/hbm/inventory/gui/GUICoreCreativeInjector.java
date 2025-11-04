package com.hbm.inventory.gui;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.inventory.container.ContainerCoreCreativeInjector;
import com.hbm.lib.RefStrings;
import com.hbm.packet.NBTControlPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.TileEntityCore;
import com.hbm.tileentity.machine.TileEntityCoreCreativeInjector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GUICoreCreativeInjector extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/dfc/gui_injector.png");
	private TileEntityCoreCreativeInjector injector;
	
	public GUICoreCreativeInjector(InventoryPlayer invPlayer, TileEntityCoreCreativeInjector tedf) {
		super(new ContainerCoreCreativeInjector(invPlayer, tedf));
		injector = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		int x = guiLeft + 8;
		int y = guiTop + 70;
		
		this.addButton(new GuiButton(0, x, y, 80, 20, "Antischrab + Amat"));
		this.addButton(new GuiButton(1, x + 88, y, 80, 20, "Deuterium + Tritium"));
		this.addButton(new GuiButton(2, x, y + 25, 80, 20, "O2 + H2"));
		this.addButton(new GuiButton(3, x + 88, y + 25, 80, 20, "Aschrab + Amat"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("fuelType", (byte) button.id);
		PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(tag, injector.getPos()));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);

		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 16, 16, 52, injector.tanks[0]);
		FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 16, 16, 52, injector.tanks[1]);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.injector.hasCustomInventoryName() ? this.injector.getInventoryName() : I18n.format(this.injector.getInventoryName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString("Fuel Selection:", 8, 58, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if (injector.lastGetCore != null) {
			TileEntityCore core = injector.lastGetCore;
			if (core.temperature >= 1500)
				drawTexturedModalRect(guiLeft+53,guiTop+15,176,87,70,70);
			else if (core.temperature >= 100)
				drawTexturedModalRect(guiLeft+53,guiTop+15,176,29,70,57);
			else if (core.tanks[0].getFluidAmount() > 0 && core.tanks[1].getFluidAmount() > 0)
				drawTexturedModalRect(guiLeft+72,guiTop+36,176,0,32,28);
		}

		FFUtils.drawLiquid(injector.tanks[0], guiLeft, guiTop, zLevel, 16, 52, 35, 97);
		FFUtils.drawLiquid(injector.tanks[1], guiLeft, guiTop, zLevel, 16, 52, 125, 97);
	}
}
