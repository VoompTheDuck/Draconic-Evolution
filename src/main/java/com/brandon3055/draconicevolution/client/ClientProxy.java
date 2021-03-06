package com.brandon3055.draconicevolution.client;

import codechicken.lib.math.MathHelper;
import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.texture.SpriteRegistryHelper;
import codechicken.lib.util.ResourceUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Vector3;
import com.brandon3055.draconicevolution.CommonProxy;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.client.gui.modular.itemconfig.GuiConfigurableItem;
import com.brandon3055.draconicevolution.client.gui.modular.GuiModularItem;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.api.energy.IENetEffectTile;
import com.brandon3055.draconicevolution.blocks.energynet.rendering.ENetFXHandler;
import com.brandon3055.draconicevolution.client.gui.GuiEnergyCore;
import com.brandon3055.draconicevolution.client.gui.GuiGenerator;
import com.brandon3055.draconicevolution.client.gui.GuiGrinder;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.brandon3055.draconicevolution.client.keybinding.KeyBindings;
import com.brandon3055.draconicevolution.client.keybinding.KeyInputHandler;
import com.brandon3055.draconicevolution.client.render.item.RenderItemChaosShard;
import com.brandon3055.draconicevolution.client.render.item.RenderItemEnergyCrystal;
import com.brandon3055.draconicevolution.client.render.item.RenderItemMobSoul;
import com.brandon3055.draconicevolution.client.render.tile.*;
import com.brandon3055.draconicevolution.utils.LogHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static com.brandon3055.draconicevolution.api.TechLevel.*;
import static com.brandon3055.draconicevolution.blocks.energynet.EnergyCrystal.CrystalType.*;

public class ClientProxy extends CommonProxy {

    public static SpriteRegistryHelper spriteHelper = new SpriteRegistryHelper();
    public static ModelRegistryHelper modelHelper = new ModelRegistryHelper();
    public static ModuleSpriteUploader moduleSpriteUploader;
//    public static LayerContributorPerkRenderer layerWings;


    @Override
    public void construct() {
        super.construct();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((ColorHandlerEvent.Block event) -> moduleSpriteUploader = new ModuleSpriteUploader());
    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event) {
        super.commonSetup(event);
    }


    @Override
    public void clientSetup(FMLClientSetupEvent event) {
        super.clientSetup(event);

        registerGuiFactories();
        registerItemRenderers();
        registerTileRenderers();
        registerTextures();
        setupRenderLayers();

        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        KeyBindings.init();


        spriteHelper.addIIconRegister(new DETextures());
//        spriteHelper.addIIconRegister(ModuleTextures.LOCATION_MODULE_TEXTURE, new ModuleTextures());

        ResourceUtils.registerReloadListener(new DETextures());
//        ResourceUtils.registerReloadListener(new ModuleTextures());


//        ModelResourceLocation modelLocation = new ModelResourceLocation(DEContent.stabilized_spawner.getRegistryName(), "inventory");
//        IBakedModel bakedModel = new RenderItemStabilizedSpawner();
//        modelHelper.register(modelLocation, bakedModel);
//
//        MinecraftForge.EVENT_BUS.addListener((EntityJoinWorldEvent e) -> LogHelper.dev(e.getEntity()));
        Minecraft.getInstance().getRenderManager().renderers.values().forEach(e -> {
            if (e instanceof LivingRenderer) {
                ((LivingRenderer) e).addLayer(new TestRenderLayer((LivingRenderer) e));
            }
        });


        RenderType modelType = RenderType.getEntitySolid(new ResourceLocation(DraconicEvolution.MODID, "textures/models/block/pylon_sphere_texture.png"));
        CCModel trackerModel;
        Map<String, CCModel> map = OBJParser.parseModels(new ResourceLocation(DraconicEvolution.MODID, "models/pylon_sphere.obj"), GL11.GL_QUADS, null);
        trackerModel = CCModel.combine(map.values()).backfacedCopy();
        trackerModel.apply(new Scale(1, 2, 1));
        trackerModel.computeNormals();


        MinecraftForge.EVENT_BUS.addListener((RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> e) -> {
            MatrixStack mStack = e.getMatrixStack();
            IRenderTypeBuffer getter = e.getBuffers();
            LivingEntity entity = e.getEntity();
            if (!entity.getPersistentData().contains("wr:trackers")) return;
            ListNBT trackers = entity.getPersistentData().getList("wr:trackers", 10);

            for (INBT inbt : trackers) {
                CompoundNBT nbt = (CompoundNBT) inbt;
                Vector3 vec = Vector3.fromNBT(nbt.getCompound("vec"));
                float rot = nbt.getFloat("rot");

                Matrix4 mat = new Matrix4(mStack);
                CCRenderState ccrs = CCRenderState.instance();
                ccrs.reset();
                ccrs.brightness = 240;
                ccrs.bind(modelType, getter);

                mat.apply(new Rotation((entity.renderYawOffset - rot) * -MathHelper.torad, Vector3.Y_POS));
                mat.translate(0, entity.getHeight() / 2, 0);
                mat.translate(vec);

                mat.scale(0.1);

                trackerModel.render(ccrs, mat);
            }
        });
    }

    private void registerTextures() {
//        BCSprites.registerThemed(MODID, "<location>");
    }

    private void registerGuiFactories() {
        ScreenManager.registerFactory(DEContent.container_generator, GuiGenerator::new);
        ScreenManager.registerFactory(DEContent.container_grinder, GuiGrinder::new);
        ScreenManager.registerFactory(DEContent.container_energy_core, GuiEnergyCore::new);
        ScreenManager.registerFactory(DEContent.container_modular_item, GuiModularItem::new);
        ScreenManager.registerFactory(DEContent.container_configurable_item, GuiConfigurableItem::new);

//        ScreenManager.registerFactory(DEContent.container_celestial_manipulator, GuiCelestialManipulator::new);
//        ScreenManager.registerFactory(DEContent.container_dissenchanter, ::new);
//        ScreenManager.registerFactory(DEContent.container_draconium_chest, ContainerDraconiumChest::new);
//        ScreenManager.registerFactory(DEContent.container_energy_crystal, ContainerEnergyCrystal::new);
//        ScreenManager.registerFactory(DEContent.container_energy_infuser, ContainerEnergyInfuser::new);
//        ScreenManager.registerFactory(DEContent.container_fusion_crafting_core, ContainerFusionCraftingCore::new);
//        ScreenManager.registerFactory(DEContent.container_reactor, ContainerReactor::new);
    }

    private void registerTileRenderers() {
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_grinder, RenderTileGrinder::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_storage_core, RenderTileEnergyCore::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_energy_pylon, RenderTileEnergyPylon::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_core_stabilizer, RenderTileECStabilizer::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_stabilized_spawner, RenderTileStabilizedSpawner::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_generator, RenderTileGenerator::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_crystal_io, RenderTileEnergyCrystal::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_crystal_relay, RenderTileEnergyCrystal::new);
        ClientRegistry.bindTileEntityRenderer(DEContent.tile_crystal_wireless, RenderTileEnergyCrystal::new);
    }

    private void registerItemRenderers() {
        modelHelper.register(new ModelResourceLocation(DEContent.chaos_shard.getRegistryName(), "inventory"), new RenderItemChaosShard(DEContent.chaos_shard));
        modelHelper.register(new ModelResourceLocation(DEContent.chaos_frag_large.getRegistryName(), "inventory"), new RenderItemChaosShard(DEContent.chaos_frag_large));
        modelHelper.register(new ModelResourceLocation(DEContent.chaos_frag_medium.getRegistryName(), "inventory"), new RenderItemChaosShard(DEContent.chaos_frag_medium));
        modelHelper.register(new ModelResourceLocation(DEContent.chaos_frag_small.getRegistryName(), "inventory"), new RenderItemChaosShard(DEContent.chaos_frag_small));
        modelHelper.register(new ModelResourceLocation(DEContent.mob_soul.getRegistryName(), "inventory"), new RenderItemMobSoul());
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_io_basic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(CRYSTAL_IO, DRACONIUM));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_io_wyvern.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(CRYSTAL_IO, WYVERN));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_io_draconic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(CRYSTAL_IO, DRACONIC));
//        modelHelper.register(new ModelResourceLocation(DEContent.crystal_io_chaotic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(CRYSTAL_IO, CHAOTIC));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_relay_basic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(RELAY, DRACONIUM));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_relay_wyvern.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(RELAY, WYVERN));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_relay_draconic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(RELAY, DRACONIC));
//        modelHelper.register(new ModelResourceLocation(DEContent.crystal_relay_chaotic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(RELAY, CHAOTIC));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_wireless_basic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(WIRELESS, DRACONIUM));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_wireless_wyvern.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(WIRELESS, WYVERN));
        modelHelper.register(new ModelResourceLocation(DEContent.crystal_wireless_draconic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(WIRELESS, DRACONIC));
//        modelHelper.register(new ModelResourceLocation(DEContent.crystal_wireless_chaotic.getRegistryName(), "inventory"), new RenderItemEnergyCrystal(WIRELESS, CHAOTIC));
    }

    private void setupRenderLayers() {
        RenderTypeLookup.setRenderLayer(DEContent.grinder, RenderType.getCutoutMipped());
        RenderTypeLookup.setRenderLayer(DEContent.generator, RenderType.getCutoutMipped());
    }

    @Override
    public void serverSetup(FMLDedicatedServerSetupEvent event) {
        super.serverSetup(event);
    }


//    @Override
//    public void preInit(FMLPreInitializationEvent event) {
//        super.preInit(event);
//
//        OBJLoader.INSTANCE.addDomain(DraconicEvolution.MODID);
//        TextureUtils.addIconRegister(new DETextures());
//        ResourceUtils.registerReloadListener(new DETextures());
//
//        DEImageHandler.init(event);
//
//        TextureUtils.addIconRegister(new ArmorModelHelper());
//        TextureUtils.addIconRegister(new DETextureCache());
//
//        registerRendering();
//    }
//
//    @Override
//    public void init(FMLInitializationEvent event) {
//
//        super.init(event);

    //        CCRenderEventHandler.init();
//    }
//
//    @Override
//    public void postInit(FMLPostInitializationEvent event) {
//        super.postInit(event);
//
//        for (RenderPlayer renderPlayer : Minecraft.getInstance().getRenderManager().getSkinMap().values()) {
//            renderPlayer.addLayer(layerWings = new LayerContributorPerkRenderer(renderPlayer));
//        }
//    }
//
//    @Override
//    public void initializeNetwork() {
//        super.initializeNetwork();
//        PacketCustom.assignHandler("DEPCChannel", new ClientPacketHandler());
//    }
//
//    public void registerRendering() {
//
//        //Entities
//        RenderingRegistry.registerEntityRenderingHandler(EntityChaosGuardian.class, RenderChaosGuardian::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityDragonHeart.class, RenderDragonHeart::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityGuardianProjectile.class, RenderGuardianProjectile::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityGuardianCrystal.class, RenderGuardianCrystal::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityChaosImplosion.class, RenderEntityChaosVortex::new);
//
//        if (DEConfig.disableCustomArrowModel) {
//            RenderingRegistry.registerEntityRenderingHandler(EntityCustomArrow.class, manager -> new RenderArrow<EntityCustomArrow>(manager) {
//                @Override
//                protected ResourceLocation getEntityTexture(EntityCustomArrow entity) {
//                    return RenderTippedArrow.RES_ARROW;
//                }
//            });
//        }
//        else {
//            RenderingRegistry.registerEntityRenderingHandler(EntityCustomArrow.class, RenderCustomArrow::new);
//        }
//
//        RenderingRegistry.registerEntityRenderingHandler(EntityLootCore.class, RenderLootCore::new);
//        RenderingRegistry.registerEntityRenderingHandler(EntityEnderEnergyManipulator.class, RenderEntityEnderEnergyManipulator::new);
//    }
//
//    @Override
//    public void registerParticles() {
//        DEParticles.registerClient();
//    }
//
//    public boolean isOp(String paramString) {
//        return Minecraft.getInstance().world.getWorldInfo().getGameType().isCreative();
//    }
//
    @Override
    public ENetFXHandler createENetFXHandler(IENetEffectTile tile) {
        if (EffectiveSide.get().isServer()) {
            return super.createENetFXHandler(tile);
        }
        return tile.createClientFXHandler();
    }
//
//    @Override
//    public ReactorEffectHandler createReactorFXHandler(TileReactorCore tile) {
//        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
//            return super.createReactorFXHandler(tile);
//        }
//        return new ReactorEffectHandler(tile);
//    }
//
//    @Override
//    public ISound playISound(ISound sound) {
//        FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
//        return sound;
//    }
}
