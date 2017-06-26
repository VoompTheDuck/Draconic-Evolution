package com.brandon3055.draconicevolution.client.render.item;

import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.brandon3055.draconicevolution.DEFeatures;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

/**
 * Created by brandon3055 on 18/04/2017.
 */
public class RenderItemMobSoul implements IItemRenderer, IPerspectiveAwareModel {

    private ItemCameraTransforms.TransformType transformType;

    public RenderItemMobSoul() {
    }

    //region Unused

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    //endregion

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        transformType = cameraTransformType;
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, TransformUtils.DEFAULT_ITEM.getTransforms(), cameraTransformType);
    }

    //Remember GuiInventory.drawEntityOnScreen
    @Override
    public void renderItem(ItemStack item, ItemCameraTransforms.TransformType transformType) {
        Entity mob = DEFeatures.mobSoul.getRenderEntity(item);

        GlStateManager.pushMatrix();
        float scale = 0.6F / Math.max(mob.width, mob.height);
        GlStateManager.translate(0.5, 0.175, 0.5);
        GlStateManager.scale(scale, scale, scale);

        if (transformType != ItemCameraTransforms.TransformType.GROUND && transformType != ItemCameraTransforms.TransformType.FIXED) {
            GlStateManager.rotate((float) Math.sin((ClientEventHandler.elapsedTicks + Minecraft.getMinecraft().getRenderPartialTicks()) / 50F) * 15F, 1, 0, -0.5F);
            GlStateManager.rotate((ClientEventHandler.elapsedTicks + Minecraft.getMinecraft().getRenderPartialTicks()) * 3, 0, 1, 0);
        }

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.doRenderEntity(mob, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);

        if (transformType != ItemCameraTransforms.TransformType.GROUND && transformType != ItemCameraTransforms.TransformType.FIXED) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.disableLighting();
        }

        //Some entities like the ender dragon modify the blend state which if not corrected like this breaks inventory rendering.
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.popMatrix();

    }
}