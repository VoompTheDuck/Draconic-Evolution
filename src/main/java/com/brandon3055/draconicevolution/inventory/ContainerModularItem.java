package com.brandon3055.draconicevolution.inventory;

import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout;
import com.brandon3055.brandonscore.inventory.PlayerSlot;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleContext;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleGrid;
import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.init.ModCapabilities;
import com.google.common.collect.Streams;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.brandon3055.draconicevolution.api.capability.DECapabilities.MODULE_HOST_CAPABILITY;

/**
 * Created by brandon3055 on 19/4/20.
 */
public class ContainerModularItem extends ContainerModuleHost<TileBCore> {

    private PlayerSlot slot;
    public ItemStack hostStack;
    private ModuleGrid moduleGrid;
    private ModuleHost moduleHost;

    public ContainerModularItem(int windowId, PlayerInventory player, PacketBuffer extraData, ContainerSlotLayout.LayoutFactory<TileBCore> factory) {
        super(DEContent.container_modular_item, windowId, player, extraData, factory);
        this.slot = PlayerSlot.fromBuff(extraData);
        this.onContainerOpen();
        this.moduleGrid = new ModuleGrid(this, player);
    }

    public ContainerModularItem(int windowId, PlayerInventory player, PlayerSlot itemSlot, ContainerSlotLayout.LayoutFactory<TileBCore> factory) {
        super(DEContent.container_modular_item, windowId, player, factory);
        this.slot = itemSlot;
        this.onContainerOpen();
        this.moduleGrid = new ModuleGrid(this, player);
    }

    private static Stream<ItemStack> getPlayerInventory(PlayerInventory player) {
        return Streams.concat(player.mainInventory.stream(), player.armorInventory.stream(), player.offHandInventory.stream()).filter(e -> !e.isEmpty());
    }

    public static void tryOpenGui(ServerPlayerEntity sender) {
        ItemStack stack = sender.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getCapability(MODULE_HOST_CAPABILITY).isPresent()) {
            PlayerSlot slot = new PlayerSlot(sender, Hand.MAIN_HAND);
            NetworkHooks.openGui(sender, new ContainerModularItem.Provider(stack, slot), slot::toBuff);
            return;
        } else {
            PlayerSlot slot = PlayerSlot.findStack(sender.inventory, e -> e.getCapability(MODULE_HOST_CAPABILITY).isPresent());
            if (slot != null) {
                NetworkHooks.openGui(sender, new ContainerModularItem.Provider(slot.getStackInSlot(sender), slot), slot::toBuff);
                return;
            }
        }

        sender.sendMessage(new TranslationTextComponent("msg.draconicevolution.modular_item.no_module_hosts").applyTextStyle(TextFormatting.RED));
    }

    @Override
    public ModuleHost getModuleHost() {
        if (moduleHost == null || EffectiveSide.get().isClient()) {
            LazyOptional<ModuleHost> optional = hostStack.getCapability(MODULE_HOST_CAPABILITY);
            if (optional.isPresent()) {
                moduleHost = optional.orElseThrow(RuntimeException::new);
            }
        }
        return moduleHost;
    }

    @Override
    public ModuleContext getModuleContext() {
        return new StackModuleContext(getModuleHost(), hostStack, player);
    }

    private void onContainerOpen() {
        hostStack = slot.getStackInSlot(player);
        getModuleHost();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        if (moduleHost == null || hostStack != slot.getStackInSlot(player)) {
            return false;
        }
        if (moduleHost != hostStack.getCapability(MODULE_HOST_CAPABILITY).orElse(null)) {
            return false; //I dont think this is actually possible... But just in case.
        }
        return true;//moduleHost != null && hostStack == slot.getStackInSlot(player);
    }

    @Override
    public ModuleGrid getGrid() {
        return moduleGrid;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        ItemStack stack = slot.getStackInSlot(player);
        if (stack != hostStack && !stack.isEmpty() && stack.getCapability(MODULE_HOST_CAPABILITY).isPresent()) {
            hostStack = stack; //Because the client side stack is invalidated every time the server sends an update.
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int button, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 0 && slotId < inventorySlots.size()) {
            Slot slot = this.inventorySlots.get(slotId);
            if (slot != null && !slot.getStack().isEmpty()) {
                if (slot.getStack() == hostStack) {
                    return ItemStack.EMPTY;
                } else if (clickTypeIn == ClickType.PICKUP && button == 0 && player.inventory.getItemStack().isEmpty()) {
                    if (slot.getStack().getCapability(MODULE_HOST_CAPABILITY).isPresent()) {
                        if (player instanceof ServerPlayerEntity) {
                            PlayerSlot playerSlot = new PlayerSlot(slotId, PlayerSlot.EnumInvCategory.MAIN);
                            NetworkHooks.openGui((ServerPlayerEntity) player, new Provider(slot.getStack(), playerSlot), playerSlot::toBuff);
                        } else {
                            GuiButton.playGenericClick();
                        }
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return super.slotClick(slotId, button, clickTypeIn, player);
    }

    public static class Provider implements INamedContainerProvider {
        private ItemStack stack;
        private PlayerSlot slot;

        public Provider(ItemStack stack, PlayerSlot slot) {
            this.stack = stack;
            this.slot = slot;
        }

        @Override
        public ITextComponent getDisplayName() {
            return stack.getDisplayName();
        }

        @Nullable
        @Override
        public Container createMenu(int menuID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerModularItem(menuID, playerInventory, slot, GuiLayoutFactories.MODULAR_ITEM_LAYOUT);
        }
    }
}
