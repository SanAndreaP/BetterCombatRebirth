/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package bettercombat.mod.util;

import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.capability.CapabilityOffhandCooldown;
import com.google.common.collect.Multimap;
import jline.internal.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Helpers
{
    private Helpers() {}

    public static <T> void execNullable(@Nullable T obj, Consumer<T> onNonNull) {
        if( obj != null ) {
            onNonNull.accept(obj);
        }
    }

    public static <T, R> R execNullable(@Nullable T obj, Function<T, R> onNonNull, R orElse) {
        if( obj != null ) {
            return onNonNull.apply(obj);
        }

        return orElse;
    }

    public static int getOffhandCooldown(EntityPlayer player) {
        Multimap<String, AttributeModifier> modifiers = player.getHeldItemOffhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        double speed = 4.0D;
        for( Map.Entry<String, AttributeModifier> modifier : modifiers.entries()) {
            if( modifier.getKey().contains("attackSpeed") ) {
                speed = modifier.getValue().getAmount();
            }
        }
        return (int) (20.0F / (4.0F + speed));
    }

    public static float getOffhandDamage(EntityPlayer player) {
        Multimap<String, AttributeModifier> modifiers = player.getHeldItemOffhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        float attack = 4.0F;
        for( Map.Entry<String, AttributeModifier> modifier : modifiers.entries()) {
            if( modifier.getKey().contains("attackDamage") ) {
                attack = (float) modifier.getValue().getAmount();
            }
        }
        return (1.0F + attack) * (ConfigurationHandler.weakerOffhand ? ConfigurationHandler.offHandEfficiency : 1.0F);
    }

    public static int getOffhandFireAspect(EntityPlayer player) {
        NBTTagList tagList = player.getHeldItemOffhand().getEnchantmentTagList();

        for( int i = 0; i < tagList.tagCount(); i++ ) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            if( tag.getInteger("id") == Enchantment.getEnchantmentID(Enchantments.FIRE_ASPECT) ) {
                return tag.getInteger("lvl");
            }
        }

        return 0;
    }

    public static int getOffhandKnockback(EntityPlayer player) {
        NBTTagList tagList = player.getHeldItemOffhand().getEnchantmentTagList();

        for( int i = 0; i < tagList.tagCount(); i++ ) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            if( tag.getInteger("id") == Enchantment.getEnchantmentID(Enchantments.KNOCKBACK) ) {
                return tag.getInteger("lvl");
            }
        }

        return 0;
    }

    public static void attackTargetEntityItem(EntityPlayer player, Entity targetEntity, boolean offhand) {
        if( !ForgeHooks.onPlayerAttackTarget(player, targetEntity) ) {
            return;
        }

        if( targetEntity.canBeAttackedWithItem() ) {
            if( !targetEntity.hitByEntity(player) ) {
                float damage = offhand ? getOffhandDamage(player) : (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float cMod;
                if( targetEntity instanceof EntityLivingBase ) {
                    cMod = EnchantmentHelper.getModifierForCreature(offhand ? player.getHeldItemOffhand() : player.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                } else {
                    cMod = EnchantmentHelper.getModifierForCreature(offhand ? player.getHeldItemOffhand() : player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }

                int cooldown = 0;
                float cooledStr;
                if( offhand ) {
                    cooldown = getOffhandCooldown(player);
                    cooledStr = 1.0F - Helpers.execNullable(player.getCapability(EventHandlers.TUTO_CAP, null), CapabilityOffhandCooldown::getOffhandCooldown, 0) / (float) cooldown;
                } else {
                    cooledStr = player.getCooledAttackStrength(0.5F);
                }

                damage *= (0.2F + cooledStr * cooledStr * 0.8F);
                cMod *= cooledStr;
                if( offhand ) {
                    EventHandlers.INSTANCE.offhandCooldown = cooldown;
                } else {
                    player.resetCooldown();
                }

                if( damage > 0.0F || cMod > 0.0F ) {
                    boolean isStrong = cooledStr > 0.9F;
                    boolean knockback = false;
                    boolean isCrit = ConfigurationHandler.randomCrits && player.getRNG().nextFloat() < ConfigurationHandler.critChance && !player.isSprinting();
                    boolean isSword = false;
                    int knockbackMod = offhand ? getOffhandKnockback(player) : EnchantmentHelper.getKnockbackModifier(player);
                    int fireAspect = offhand ? getOffhandFireAspect(player) : EnchantmentHelper.getFireAspectModifier(player);

                    if( player.isSprinting() && isStrong ) {
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        knockbackMod++;
                        knockback = true;
                    }

                    if( isCrit ) {
                        damage *= 1.5F;
                    }
                    damage += cMod;

                    double tgtDistDelta = player.distanceWalkedModified - player.prevDistanceWalkedModified;
                    if( isStrong && !isCrit && !knockback && player.onGround && tgtDistDelta < player.getAIMoveSpeed() ) {
                        ItemStack ohItem = player.getHeldItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                        if( ohItem.getItem() instanceof ItemSword ) {
                            isSword = true;
                        }
                    }

                    float tgtHealth = 0.0F;
                    boolean burnInflicted = false;
                    if( targetEntity instanceof EntityLivingBase ) {
                        tgtHealth = ((EntityLivingBase) targetEntity).getHealth();
                        if( fireAspect > 0 && !targetEntity.isBurning() ) {
                            targetEntity.setFire(1);
                            burnInflicted = true;
                        }
                    }

                    double tgtMotionX = targetEntity.motionX;
                    double tgtMotionY = targetEntity.motionY;
                    double tgtMotionZ = targetEntity.motionZ;
                    boolean attacked;

                    if( offhand ) {
                        final float attackDmgFinal = damage;
                        attacked = execNullable(targetEntity.getCapability(EventHandlers.SECONDHURTTIMER_CAP, null),
                                                sht -> sht.attackEntityFromOffhand(targetEntity, DamageSource.causePlayerDamage(player), attackDmgFinal), false);
                    } else {
                        attacked = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
                    }
                    if( attacked ) {
                        if( knockbackMod > 0 ) {
                            if( targetEntity instanceof EntityLivingBase ) {
                                ((EntityLivingBase) targetEntity).knockBack(player, knockbackMod * 0.5F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                            } else {
                                targetEntity.addVelocity(-MathHelper.sin(player.rotationYaw * 0.017453292F) * knockbackMod * 0.5F, 0.1D, MathHelper.cos(player.rotationYaw * 0.017453292F) * knockbackMod * 0.5F);
                            }
                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            if( !ConfigurationHandler.moreSprint ) {
                                player.setSprinting(false);
                            }
                        }

                        if( isSword ) {
                            for( EntityLivingBase living : player.world.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D)) ) {
                                if( living != player && living != targetEntity && !player.isOnSameTeam(living) && player.getDistanceSq(living) < 9.0D ) {
                                    living.knockBack(player, 0.4F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                                    if( offhand ) {
                                        execNullable(targetEntity.getCapability(EventHandlers.SECONDHURTTIMER_CAP, null),
                                                     sht -> sht.attackEntityFromOffhand(living, DamageSource.causePlayerDamage(player), 1.0F));
                                    } else {
                                        living.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0F);
                                    }
                                }
                            }
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }

                        if( targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged ) {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = tgtMotionX;
                            targetEntity.motionY = tgtMotionY;
                            targetEntity.motionZ = tgtMotionZ;
                        }

                        if( isCrit ) {
                            if( offhand ) {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                            player.onCriticalHit(targetEntity);
                        }

                        boolean playSound = true;
                        ItemStack heldItem = offhand ? player.getHeldItemOffhand() : player.getHeldItemMainhand();
                        if( !heldItem.isEmpty() ) {
                            if( heldItem.getItem() instanceof ItemSpade ) {
                                playSound = false;
                            }
                            if( playSound ) {
                                if( ConfigurationHandler.hitSound && (!ConfigurationHandler.critSound || !isCrit) ) {
                                    player.world.playSound(null, player.posX, player.posY, player.posZ, Sounds.SWORD_SLASH, player.getSoundCategory(), 1.0F, 1.0F);
                                }
                                if( ConfigurationHandler.critSound && isCrit ) {
                                    player.world.playSound(null, player.posX, player.posY, player.posZ, Sounds.CRITICAL_STRIKE, player.getSoundCategory(), 1.0F, 1.0F);
                                }
                            }
                        }

                        if( !isCrit && !isSword ) {
                            if( isStrong ) {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if( cMod > 0.0F ) {
                            player.onEnchantmentCritical(targetEntity);
                        }

                        if( !player.world.isRemote && targetEntity instanceof EntityPlayer ) {
                            EntityPlayer entityplayer = (EntityPlayer) targetEntity;
                            ItemStack activeItem = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;
                            if( heldItem.getItem() instanceof ItemAxe && activeItem.getItem() instanceof ItemShield ) {
                                float efficiency = 0.25F + EnchantmentHelper.getEfficiencyModifier(player) * 0.05F;
                                if( knockback ) {
                                    efficiency += 0.75F;
                                }

                                if( player.getRNG().nextFloat() < efficiency ) {
                                    entityplayer.getCooldownTracker().setCooldown(activeItem.getItem(), 100);
                                    player.world.setEntityState(entityplayer, (byte) 30);
                                }
                            }
                        }

                        player.setLastAttackedEntity(targetEntity);

                        if( targetEntity instanceof EntityLivingBase ) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                        Entity entity = targetEntity;

                        if( targetEntity instanceof MultiPartEntityPart ) {
                            IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;
                            if( ientitymultipart instanceof EntityLivingBase ) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if( !heldItem.isEmpty() && entity instanceof EntityLivingBase ) {
                            ItemStack beforeHitCopy = heldItem.copy();
                            heldItem.hitEntity((EntityLivingBase) entity, player);
                            if( heldItem.isEmpty() ) {
                                player.setHeldItem(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, ItemStack.EMPTY);
                                ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                            }
                        }

                        if( targetEntity instanceof EntityLivingBase ) {
                            float healthDelta = tgtHealth - ((EntityLivingBase) targetEntity).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(healthDelta * 10.0F));

                            if( fireAspect > 0 ) {
                                targetEntity.setFire(fireAspect * 4);
                            }

                            if( player.world instanceof WorldServer && healthDelta > 2.0F ) {
                                int k = (int) (healthDelta * 0.5D);
                                ((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + targetEntity.height * 0.5F, targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.3F);
                    } else {
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

                        if( burnInflicted ) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }

    /**
     * This returns a null value for those final variables that have their values injected during runtime.
     * Prevents IDEs from warning the user of potential NullPointerExceptions on code using those variables.
     * @param <T> any type
     * @return null
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T getNull() {
        return null;
    }
}
