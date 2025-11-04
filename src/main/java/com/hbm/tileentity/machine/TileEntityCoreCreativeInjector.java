package com.hbm.tileentity.machine;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.leafia.contents.machines.powercores.dfc.DFCBaseTE;
import com.leafia.dev.container_utility.LeafiaPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCoreCreativeInjector extends DFCBaseTE implements ITickable, IFluidHandler, ITankPacketAcceptor, IControlReceiver {

    public FluidTank[] tanks;
    public static final int range = 50;
    public int beam;

    public TileEntityCoreCreativeInjector() {
        super(0);
        tanks = new FluidTank[2];
        // Infinite capacity tanks pre-filled with premium fluids
        tanks[0] = new FluidTank(Integer.MAX_VALUE);
        tanks[1] = new FluidTank(Integer.MAX_VALUE);
        // Pre-fill with Antischrabbidium and Antimatter
        tanks[0].setFluid(new FluidStack(ModForgeFluids.ASCHRAB, Integer.MAX_VALUE));
        tanks[1].setFluid(new FluidStack(ModForgeFluids.AMAT, Integer.MAX_VALUE));
    }

    @Override
    public void update() {
        TileEntityCore core = getCore(range);
        if (!world.isRemote) {
            LeafiaPacket._start(this).__write(31,targetPosition).__sendToAffectedClients();

            if (core != null)
                fillDFC(core);

            this.markDirty();

            PacketDispatcher.wrapper.sendToAllTracking(new FluidTankPacket(pos, tanks), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 250));
        }
    }

    public void fillDFC(TileEntityCore core) {
        Fluid tank0 = null;
        Fluid tank1 = null;
        Fluid dfcTank0 = null;
        Fluid dfcTank1 = null;

        if (tanks[0].getFluid() != null)
            tank0 = tanks[0].getFluid().getFluid();
        if (tanks[1].getFluid() != null)
            tank1 = tanks[1].getFluid().getFluid();

        if (tank0 == null && tank1 == null) return;

        if (core.tanks[0].getFluid() != null)
            dfcTank0 = core.tanks[0].getFluid().getFluid();
        if (core.tanks[1].getFluid() != null)
            dfcTank1 = core.tanks[1].getFluid().getFluid();

        // Creative mode: always provide unlimited fluid
        if ((tank0 == dfcTank0 || dfcTank0 == null) && tank0 != dfcTank1) {
            FluidStack toFill = new FluidStack(tank0, 1000000);
            core.tanks[0].fill(toFill, true);
            dfcTank0 = tank0;
            core.markDirty();
        }

        if ((tank1 == dfcTank1 || dfcTank1 == null) && tank1 != dfcTank0) {
            FluidStack toFill = new FluidStack(tank1, 1000000);
            core.tanks[1].fill(toFill, true);
            dfcTank1 = tank1;
            core.markDirty();
        }

        if ((tank0 == dfcTank1 || dfcTank1 == null) && tank0 != dfcTank0) {
            FluidStack toFill = new FluidStack(tank0, 1000000);
            core.tanks[1].fill(toFill, true);
            core.markDirty();
        }

        if ((tank1 == dfcTank0 || dfcTank0 == null) && tank1 != dfcTank1) {
            FluidStack toFill = new FluidStack(tank1, 1000000);
            core.tanks[0].fill(toFill, true);
            core.markDirty();
        }
    }

    @Override
    public String getName() {
        return "container.dfcCreativeInjector";
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0]};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null)
            return 0;
        // Creative mode: always accept and provide unlimited fluid
        return 1000000;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public void recievePacket(NBTTagCompound[] tags) {
        if (tags.length == 2) {
            tanks[0].readFromNBT(tags[0]);
            tanks[1].readFromNBT(tags[1]);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("tanks"))
            FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanks);
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("tanks", FFUtils.serializeTankArray(tanks));
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String getPacketIdentifier() {
        return "dfc_creative_injector";
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return true; // Creative injector - anyone can use
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("fuelType")) {
            byte fuelType = data.getByte("fuelType");
            setFuel(fuelType);
        }
    }

    private void setFuel(int fuelType) {
        switch(fuelType) {
            case 0: // Antischrabbidium + Antimatter
                tanks[0].setFluid(new FluidStack(ModForgeFluids.ASCHRAB, Integer.MAX_VALUE));
                tanks[1].setFluid(new FluidStack(ModForgeFluids.AMAT, Integer.MAX_VALUE));
                break;
            case 1: // Deuterium + Tritium
                tanks[0].setFluid(new FluidStack(ModForgeFluids.DEUTERIUM, Integer.MAX_VALUE));
                tanks[1].setFluid(new FluidStack(ModForgeFluids.TRITIUM, Integer.MAX_VALUE));
                break;
            case 2: // Oxygen + Hydrogen
                tanks[0].setFluid(new FluidStack(ModForgeFluids.OXYGEN, Integer.MAX_VALUE));
                tanks[1].setFluid(new FluidStack(ModForgeFluids.HYDROGEN, Integer.MAX_VALUE));
                break;
            case 3: // Antischrabbidium + Antimatter (duplicate)
                tanks[0].setFluid(new FluidStack(ModForgeFluids.ASCHRAB, Integer.MAX_VALUE));
                tanks[1].setFluid(new FluidStack(ModForgeFluids.AMAT, Integer.MAX_VALUE));
                break;
        }
        this.markDirty();
    }
}
