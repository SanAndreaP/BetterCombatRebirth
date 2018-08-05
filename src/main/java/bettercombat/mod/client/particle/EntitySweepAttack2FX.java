package bettercombat.mod.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySweepAttack2FX
        extends Particle
{
    private static final ResourceLocation SWEEP_TEXTURE = new ResourceLocation("textures/entity/sweep.png");
    private static final VertexFormat VERTEX_FORMAT = new VertexFormat().addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB)
                                                                        .addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
    private int life;
    private int lifeTime;
    private TextureManager textureManager;
    private float size;

    public EntitySweepAttack2FX(TextureManager textureManager, World world, double x, double y, double z, double scale) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.textureManager = textureManager;
        this.lifeTime = 4;
        this.particleRed = (this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.6F + 0.4F);
        this.size = (1.0F - (float) scale * 0.5F);
    }

    @Override
    public void renderParticle(BufferBuilder buf, Entity entityIn, float partialTicks, float rotationX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
        int tInd = (int) ((this.life + partialTicks) * 3.0F / this.lifeTime);
        if( tInd <= 7 ) {
            this.textureManager.bindTexture(SWEEP_TEXTURE);
            float u1 = tInd % 4 / 4.0F;
            float u2 = u1 - 0.24975F;
            float v1 = tInd / 2.0F / 2.0F;
            float v2 = v1 - 0.4995F;
            float scale = -1.0F * this.size;
            float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
            float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
            float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            buf.begin(7, VERTEX_FORMAT);
            buf.pos(x - rotationX * scale - rotXY * scale, y - rotZ * scale * 0.5F, z - rotYZ * scale - rotXZ * scale).tex(u2, v2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buf.pos(x - rotationX * scale + rotXY * scale, y + rotZ * scale * 0.5F, z - rotYZ * scale + rotXZ * scale).tex(u2, v1).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buf.pos(x + rotationX * scale + rotXY * scale, y + rotZ * scale * 0.5F, z + rotYZ * scale + rotXZ * scale).tex(u1, v1).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            buf.pos(x + rotationX * scale - rotXY * scale, y - rotZ * scale * 0.5F, z + rotYZ * scale - rotXZ * scale).tex(u1, v2).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.enableLighting();
        }
    }

    @Override
    public int getBrightnessForRender(float partTicks) {
        return 0xF0F0;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.life += 1;
        if( this.life == this.lifeTime ) {
            setExpired();
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }
}