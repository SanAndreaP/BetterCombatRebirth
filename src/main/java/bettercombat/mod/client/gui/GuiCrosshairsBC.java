package bettercombat.mod.client.gui;

import bettercombat.mod.capability.CapabilityOffhandCooldown;
import bettercombat.mod.combat.IOffHandAttack;
import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.util.BetterCombatMod;
import bettercombat.mod.util.Helpers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCrosshairsBC
        extends Gui
{
    public static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    public void renderAttackIndicator(float partTicks, ScaledResolution scaledRes) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(ICONS);
        GlStateManager.enableBlend();
        GameSettings gamesettings = mc.gameSettings;
        if( gamesettings.thirdPersonView == 0 ) {
            if( mc.playerController.isSpectator() && mc.pointedEntity == null ) {
                RayTraceResult rtRes = mc.objectMouseOver;
                if( rtRes == null || rtRes.typeOfHit != net.minecraft.util.math.RayTraceResult.Type.BLOCK ) {
                    return;
                }

                BlockPos blockpos = rtRes.getBlockPos();
                IBlockState state = mc.world.getBlockState(blockpos);
                if( !state.getBlock().hasTileEntity(state) || !(mc.world.getTileEntity(blockpos) instanceof IInventory) ) {
                    return;
                }
            }

            int sw = scaledRes.getScaledWidth();
            int sh = scaledRes.getScaledHeight();
            if( gamesettings.showDebugInfo && !gamesettings.hideGUI && !mc.player.hasReducedDebug() && !gamesettings.reducedDebugInfo ) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(sw / 2.0F, sh / 2.0F, this.zLevel);
                Entity entity = mc.getRenderViewEntity();
                if( entity == null ) {
                    return;
                }
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partTicks, -1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partTicks, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(-1.0F, -1.0F, -1.0F);
                net.minecraft.client.renderer.OpenGlHelper.renderDirections(10);
                GlStateManager.popMatrix();
            } else {
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.enableAlpha();
                drawTexturedModalRect(sw / 2 - 7, sh / 2 - 7, 0, 0, 16, 16);
                if( mc.gameSettings.attackIndicator == 1 ) {
                    float cooledStr = mc.player.getCooledAttackStrength(0.0F);
                    ItemStack heldItemOH = mc.player.getHeldItemOffhand();
                    if( heldItemOH.getItem() instanceof ItemHoe || heldItemOH.getItem() instanceof ItemTool || heldItemOH.getItem() instanceof ItemSword ) {
                        int cooldown = Helpers.getOffhandCooldown(mc.player);

                        float ohCooldown = Helpers.execNullable(mc.player.getCapability(EventHandlers.TUTO_CAP, null), CapabilityOffhandCooldown::getOffhandCooldown, 0) / (float) cooldown;
                        ohCooldown = Math.abs(1.0F - ohCooldown);
                        if( cooledStr < 1.0F ) {
                            int i = sh / 2 - 7 + 16;
                            int j = sw / 2 - 7;
                            int k = (int) (cooledStr * 17.0F);
                            drawTexturedModalRect(j + 15, i, 36, 94, 16, 4);
                            drawTexturedModalRect(j + 15, i, 52, 94, k, 4);
                        }

                        if( ohCooldown < 1.0F ) {
                            int i = sh / 2 - 7 + 16;
                            int j = sw / 2 - 7;
                            int k = (int) (ohCooldown * 17.0F);
                            drawTexturedModalRect(j - 15, i, 36, 94, 16, 4);
                            drawTexturedModalRect(j - 15, i, 52, 94, k, 4);
                        }
                    } else if( cooledStr < 1.0F ) {
                        int i = sh / 2 - 7 + 16;
                        int j = sw / 2 - 7;
                        int k = (int) (cooledStr * 17.0F);
                        drawTexturedModalRect(j, i, 36, 94, 16, 4);
                        drawTexturedModalRect(j, i, 52, 94, k, 4);
                    }
                }
            }
        }

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }
}