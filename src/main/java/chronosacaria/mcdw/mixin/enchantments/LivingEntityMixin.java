package chronosacaria.mcdw.mixin.enchantments;

import chronosacaria.mcdw.api.util.AOECloudHelper;
import chronosacaria.mcdw.api.util.AOEHelper;
import chronosacaria.mcdw.api.util.AbilityHelper;
import chronosacaria.mcdw.api.util.ProjectileEffectHelper;
import chronosacaria.mcdw.bases.McdwBow;
import chronosacaria.mcdw.configs.McdwEnchantsConfig;
import chronosacaria.mcdw.enchants.EnchantsRegistry;
import chronosacaria.mcdw.sounds.McdwSoundEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Unique
	private void applyChains(DamageSource source, float amount, LivingEntity user) {
		if (source.getSource() instanceof ArrowEntity) {
			return;
		}

		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity && !source.isProjectile()) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("chains")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.CHAINS, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.CHAINS, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.2f) {
							AOEHelper.chainNearbyEntities(
								user,
								target,
								1.5F * level,
								level);
						}
					}
				}
			}
		}
	}

	@Unique
	private void applyCharge(LivingEntity user) {
		if ((EnchantmentHelper.getLevel(EnchantsRegistry.CHARGE, user.getMainHandStack()) >= 1)) {
			int level = EnchantmentHelper.getLevel(EnchantsRegistry.CHARGE, user.getMainHandStack());
			float chargeRand = user.getRandom().nextFloat();
			if (chargeRand <= 0.1F) {
				StatusEffectInstance charge = new StatusEffectInstance(StatusEffects.SPEED, level * 20, 4);
				user.addStatusEffect(charge);
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;
		LivingEntity user = (LivingEntity) source.getAttacker();

		if (McdwEnchantsConfig.getValue("chains")) {
			applyChains(source, amount, user);
		} else if (McdwEnchantsConfig.getValue("charge")) {
			applyCharge(user);
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyCommittedEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.isProjectile()) return;
		if (source.getSource() instanceof ArrowEntity) return;

		if (source.getSource() instanceof PlayerEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}

				if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.COMMITTED, mainHandStack) >= 1)) {
					int level = EnchantmentHelper.getLevel(EnchantsRegistry.COMMITTED, mainHandStack);


					float getTargetHealth = target.getHealth();
					float getTargetMaxHealth = target.getMaxHealth();
					float getTargetRemainingHealth = getTargetHealth / getTargetMaxHealth;
					float getOriginalDamage = (float) user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
					float extraDamageMultiplier = 0.1F + level * 0.1F;
					float getExtraDamage = (getOriginalDamage * (1 - getTargetRemainingHealth) * extraDamageMultiplier);

					float chance = user.getRandom().nextFloat();
					if (chance <= 0.2) {
						if ((Math.abs(getTargetHealth)) < (Math.abs(getTargetMaxHealth))) {
							target.setHealth(getTargetHealth - (amount * getExtraDamage));
							target.world.playSound(
								null,
								target.getX(),
								target.getY(),
								target.getZ(),
								SoundEvents.ENTITY_GENERIC_EXPLODE,
								SoundCategory.PLAYERS,
								0.5F,
								1.0F);
						}
					}
				}
			}
		}
	}


	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyCriticalHitEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag = false;
				if (McdwEnchantsConfig.getValue("critical_hit")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.CRITICAL_HIT, mainHandStack) >= 1 )) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.CRITICAL_HIT, mainHandStack);

						float criticalHitChance;
						criticalHitChance = 0.5f + level * 0.05F;
						float criticalHitRand = user.getRandom().nextFloat();
						float attackDamage = (float) user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
						float extraDamageMultiplier = 1.5F;
						float getExtraDamage = (attackDamage * (extraDamageMultiplier));
						float h = target.getHealth();

						if (criticalHitRand <= criticalHitChance) {
							target.setHealth(h - (amount * extraDamageMultiplier));
							target.world.playSound(
								null,
								target.getX(),
								target.getY(),
								target.getZ(),
								SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
								SoundCategory.PLAYERS,
								0.5F,
								1.0F);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyEchoEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("echo")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.ECHO, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.ECHO, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.1 + level * 0.15) {
							AOEHelper.causeEchoAttack(user,
								target,
								3.0f,
								level,
								amount);
							user.world.playSound(
								null,
								user.getX(),
								user.getY(),
								user.getZ(),
								McdwSoundEvents.ECHO_SOUND_EVENT,
								SoundCategory.PLAYERS,
								0.5F,
								1.0F);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyFreezingEnchantment(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag = false;
				if (McdwEnchantsConfig.getValue("freezing")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.FREEZING, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.FREEZING, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.3 + (level * 0.1)) {
							AbilityHelper.causeFreesing(target, 100);
						}
					}
				}
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "applyDamage", cancellable = true)

	private void applyFuseShotEnchantment(DamageSource source, float amount, CallbackInfo info)  {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;
		ItemStack mainHandStack = null;
		if (user != null) {
			mainHandStack = user.getMainHandStack();
		}
		boolean uniqueWeaponFlag =
			false;
		if (McdwEnchantsConfig.getValue("fuse_shot")) {
			if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.FUSE_SHOT, mainHandStack) >= 1)) {
				int level = EnchantmentHelper.getLevel(EnchantsRegistry.FUSE_SHOT, mainHandStack);
				float chance = user.getRandom().nextFloat();
				if (chance <= (0.2 + level * 0.15)) {
					AbilityHelper.causeFuseShot(user, target, level);
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyGravityEnchantment(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity && !source.isProjectile()) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("gravity")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.GRAVITY, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.GRAVITY, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.3) {
							AOEHelper.pullInNearbyEntities(
								user,
								target,
								(level + 1) * 3);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyJunglesPoisonEnchantment(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag = false;
				if (McdwEnchantsConfig.getValue("jungle_poison")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.JUNGLE_POISON, mainHandStack) >= 1 )) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.JUNGLE_POISON, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.3) {
							StatusEffectInstance poison = new StatusEffectInstance(StatusEffects.POISON, 60, level - 1);
							target.addStatusEffect(poison);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyPoisonCloudEnchantment(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("poison_cloud")) {
					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.POISON_CLOUD, mainHandStack) >= 1 )) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.POISON_CLOUD, mainHandStack);

						float chance = user.getRandom().nextFloat();
						//Spawn Poison Cloud @ 30% chance
						if (target instanceof LivingEntity) {
							if (chance <= 0.3) {
								AOECloudHelper.spawnPoisonCloud(
									user,
									target,
									level - 1);
							}
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyRadianceEnchantmentCloud(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("radiance")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.RADIANCE, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.RADIANCE, mainHandStack);

						float chance = user.getRandom().nextFloat();
						//Spawn Regen Cloud @ 20% chance
						if (target instanceof LivingEntity) {
							if (chance <= 0.2) {
								AOECloudHelper.spawnRegenCloud(
									user,
									level - 1);
							}
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyReplenishEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		//LivingEntity target = (LivingEntity) (Object) this;

		if (source.isProjectile()) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("replenish")) {
					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.REPLENISH,
						mainHandStack) >= 1 )) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.REPLENISH, mainHandStack);
						if (user instanceof PlayerEntity) {
							if (level >= 1) {
								float replenishRand = user.getRandom().nextFloat();
								float replenishChance = 0;
								if (level == 1) replenishChance = 0.10f;
								if (level == 2) replenishChance = 0.17f;
								if (level == 3) replenishChance = 0.24f;
								if (replenishRand <= replenishChance) {
									ItemEntity arrowDrop = new ItemEntity(user.world, user.getX(), user.getY(),
										user.getZ(),
										new ItemStack(Items.ARROW));
									user.world.spawnEntity(arrowDrop);
								}
							}
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyRicochet(DamageSource source, float amount, CallbackInfo info) {
		if (!(source.getAttacker() instanceof PlayerEntity)) return;

		PlayerEntity attacker = (PlayerEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (McdwEnchantsConfig.getValue("ricochet"))  {
			int level = EnchantmentHelper.getLevel(EnchantsRegistry.RICOCHET, attacker.getMainHandStack());
			if (level >= 1) {
				float damageMultiplier = 0.1F + ((level - 1) * 0.07F);
				float arrowVelocity = McdwBow.maxBowRange;
				if (arrowVelocity > 0.1F) {
					ProjectileEffectHelper.riochetArrowTowardsOtherEntity(target, 10, damageMultiplier, arrowVelocity);
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyShockwaveEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {

		if(!(source.getAttacker() instanceof PlayerEntity)) return;


		PlayerEntity user = (PlayerEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof PlayerEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("shockwave")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.SHOCKWAVE, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.SHOCKWAVE, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.1 + (level * 0.15)) {
							AOEHelper.causeShockwaveAttack(
								user,
								target,
								3.0f,
								amount);

							target.world.playSound(
								null,
								target.getX(),
								target.getY(),
								target.getZ(),
								SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
								SoundCategory.WEATHER,
								0.5F,
								1.0F);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applySmitingEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if(target instanceof PlayerEntity) return;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("smiting")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.SMITING, mainHandStack) >= 1 && !(EnchantmentHelper.getLevel(Enchantments.SMITE, mainHandStack) >= 1))) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.SMITING, mainHandStack);
						if (target.isUndead()) {
							AOEHelper.causeSmitingAttack(
								(PlayerEntity) user,
								target,
								3.0f * level,
								amount);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyStunningEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("stunning")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.STUNNING, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.STUNNING, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.2 + level * 0.15) {
							target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 10));
							target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 1));
							//this.world.sendEntityStatus(this,(byte)35);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applySwirlingEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("swirling")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.SWIRLING, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.SWIRLING, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.1 + level * 0.15) {
							AOEHelper.causeSwirlingAttack(
								(PlayerEntity) user,
								target,
								1.5f,
								amount);

							target.world.playSound(
								null,
								target.getX(),
								target.getY(),
								target.getZ(),
								SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
								SoundCategory.PLAYERS,
								0.5F,
								1.0F);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyTempoTheftEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.isProjectile()) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("tempo_theft")) {
					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.TEMPO_THEFT, mainHandStack) >= 1 || uniqueWeaponFlag)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.TEMPO_THEFT, mainHandStack);
						if (target instanceof LivingEntity) {
							AbilityHelper.stealSpeedFromTarget(user, target, level);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyThunderingEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				if (McdwEnchantsConfig.getValue("thundering")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.THUNDERING, mainHandStack) >= 1 )) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.THUNDERING, mainHandStack);

						float chance = user.getRandom().nextFloat();
						if (chance <= 0.2F) {
							AOEHelper.electrocuteNearbyEnemies(
								user,
								5 * level,
								amount,
								Integer.MAX_VALUE);
						}
					}
				}
			}
		}
	}

	@Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"))
	public void applyWeakeningEnchantmentDamage(DamageSource source, float amount, CallbackInfo info) {
		if(!(source.getAttacker() instanceof PlayerEntity)) return;

		LivingEntity user = (LivingEntity) source.getAttacker();
		LivingEntity target = (LivingEntity) (Object) this;

		if (source.getSource() instanceof LivingEntity) {
			if (amount != 0.0F) {
				ItemStack mainHandStack = null;
				if (user != null) {
					mainHandStack = user.getMainHandStack();
				}
				boolean uniqueWeaponFlag =
					false;
				if (McdwEnchantsConfig.getValue("weakening")) {

					if (mainHandStack != null && (EnchantmentHelper.getLevel(EnchantsRegistry.WEAKENING, mainHandStack) >= 1)) {
						int level = EnchantmentHelper.getLevel(EnchantsRegistry.WEAKENING, mainHandStack);

						float chance = user.getRandom().nextFloat();
						//Spawn Weakening Cloud @ 30% chance
						if (target instanceof LivingEntity) {
							if (chance <= 0.3) {
								AOECloudHelper.spawnWeakeningCloud(
									user,
									target,
									level - 1);
							}
						}
					}
				}
			}
		}
	}
}
