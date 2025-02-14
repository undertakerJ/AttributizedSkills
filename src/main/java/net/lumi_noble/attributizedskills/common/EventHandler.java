package net.lumi_noble.attributizedskills.common;

import net.lumi_noble.attributizedskills.common.capabilities.SkillCapability;
import net.lumi_noble.attributizedskills.common.capabilities.SkillModel;
import net.lumi_noble.attributizedskills.common.capabilities.SkillProvider;
import net.lumi_noble.attributizedskills.common.config.Config;
import net.lumi_noble.attributizedskills.AttributizedSkills;
import net.lumi_noble.attributizedskills.common.effects.AttributizedSkillsEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class EventHandler {

  public static final EquipmentSlot[] ALL_SLOTS = {
    EquipmentSlot.MAINHAND,
    EquipmentSlot.OFFHAND,
    EquipmentSlot.CHEST,
    EquipmentSlot.FEET,
    EquipmentSlot.HEAD,
    EquipmentSlot.LEGS
  };
  public static final EquipmentSlot[] ARMOR_SLOTS = {
    EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.LEGS
  };

  private static final MobEffect ATROPHY = AttributizedSkillsEffects.ATROPHY.get();
  private static final MobEffect ENCUMBERED = AttributizedSkillsEffects.ENCUMBERED.get();

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onLivingTick(PlayerTickEvent event) {

    if (event.side == LogicalSide.SERVER
        && !event.player.isCreative()
        && !event.player.isDeadOrDying()
        && event.player != null) {

      Player player = event.player;
      SkillModel model = SkillModel.get(player);

      // if effect detriment is true, apply negative effects while wielding items with unmet
      // requirements.
      if (Config.getWhetherEffectDetriment()) {
        boolean hasRestrictedItem = false;

        for (EquipmentSlot slot : ALL_SLOTS) {

          ItemStack item = player.getItemBySlot(slot);

          if (item != null && model != null && !model.canUseItemInSlot(player, item, slot)) {

            hasRestrictedItem = true;
            MobEffectInstance instance = null;

            if (slot.getType() == EquipmentSlot.Type.HAND && !player.hasEffect(ATROPHY)) {
              instance = new MobEffectInstance(ATROPHY, SkillModel.EFFECT_DURATION);
            } else if (!player.hasEffect(ENCUMBERED) && slot.getType() != EquipmentSlot.Type.HAND) {
              instance = new MobEffectInstance(ENCUMBERED, SkillModel.EFFECT_DURATION);
            }

            if (instance != null) {
              player.addEffect(instance);
            }
          }
        }

        // if no longer equipping restricted item, remove effect.
        if (!hasRestrictedItem && player.hasEffect(ATROPHY)) {
          player.removeEffect(ATROPHY);
        }
        if (!hasRestrictedItem && player.hasEffect(ENCUMBERED)) {
          player.removeEffect(ENCUMBERED);
        }
      }
    }
  }

  // Left Click Block
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    Player player = event.getEntity();
    ItemStack item = event.getItemStack();
    Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
    SkillModel model = SkillModel.get(player);
    boolean canUseItem = true;

    if (event.getHand() == InteractionHand.MAIN_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.MAINHAND);
    } else if (event.getHand() == InteractionHand.OFF_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.OFFHAND);
    }

    if (!player.isCreative() && !canUseItem || !model.canUseBlock(player, block)) {
      event.setCanceled(true);
    }
  }

  // Right Click Block
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    Player player = event.getEntity();
    ItemStack item = event.getItemStack();
    Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
    SkillModel model = SkillModel.get(player);
    boolean canUseItem = true;

    if (event.getHand() == InteractionHand.MAIN_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.MAINHAND);
    } else if (event.getHand() == InteractionHand.OFF_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.OFFHAND);
    }

    if (!player.isCreative() && !canUseItem || !model.canUseBlock(player, block)) {
      event.setCanceled(true);
    }
  }

  // Right Click Item
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
    Player player = event.getEntity();
    ItemStack item = event.getItemStack();
    SkillModel model = SkillModel.get(player);
    boolean canUseItem = true;

    if (event.getHand() == InteractionHand.MAIN_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.MAINHAND);
    } else if (event.getHand() == InteractionHand.OFF_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.OFFHAND);
    }

    if (!player.isCreative() && !canUseItem) {
      event.setCanceled(true);
    }
  }

  // Right Click Entity
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
    Player player = event.getEntity();
    Entity entity = event.getTarget();
    ItemStack item = event.getItemStack();
    SkillModel model = SkillModel.get(player);
    boolean canUseItem = true;

    if (event.getHand() == InteractionHand.MAIN_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.MAINHAND);
    } else if (event.getHand() == InteractionHand.OFF_HAND) {
      canUseItem = model.canUseItemInSlot(player, item, EquipmentSlot.OFFHAND);
    }

    if (!player.isCreative()) {
      if (!SkillModel.get(player).canUseEntity(player, entity) || !canUseItem) {
        event.setCanceled(true);
      }
    }
  }

  // if effect detriment is false, cancel the following interaction events:

  // Attack Entity
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onAttackEntity(AttackEntityEvent event) {
    if (!Config.getWhetherEffectDetriment()) {
      Player player = event.getEntity();

      if (player != null) {
        ItemStack item = player.getMainHandItem();

        if (!player.isCreative()
            && !SkillModel.get(player).canUseItemInSlot(player, item, EquipmentSlot.MAINHAND)) {
          event.setCanceled(true);
        }
      }
    }
  }

  // Change Equipment
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChangeEquipment(LivingEquipmentChangeEvent event) {
    if (!Config.getWhetherEffectDetriment()) {
      if (event.getEntity() instanceof Player) {
        Player player = (Player) event.getEntity();

        if (!player.isCreative() && event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
          ItemStack item = event.getTo();
          SkillModel model = SkillModel.get(player);

          for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!model.canUseItemInSlot(player, item, slot)) {
              player.drop(item.copy(), false);
              item.setCount(0);
            }
          }
        }
      }
    }
  }

  @SubscribeEvent
  public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(new ResourceLocation("rpgskillable","cap_skills"), new SkillProvider(new SkillModel()));
    }
  }

  @SubscribeEvent
  public void onPlayerDeath(LivingDeathEvent event) {
    if (Config.getDeathReset() && event.getEntity() instanceof ServerPlayer) {
      ServerPlayer player = (ServerPlayer) event.getEntity();
      SkillModel.get(player).resetSkills(player);
    }
  }

  @SubscribeEvent
  public static void onPlayerClone(PlayerEvent.Clone event) {
    if (event.isWasDeath()) {
      event.getOriginal().reviveCaps();
      event.getEntity().getCapability(SkillCapability.SKILL_MODEL).ifPresent(newStore -> {
        event.getOriginal().getCapability(SkillCapability.SKILL_MODEL).ifPresent(oldStore -> {
          newStore.copyForRespawn(oldStore);
        });
      });
      event.getOriginal().invalidateCaps();
    }
  }

  @SubscribeEvent
  public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    Player player = event.getEntity();
    if (player instanceof ServerPlayer serverPlayer) {
      SkillModel model = SkillModel.get(player);
    }
  }

  @SubscribeEvent
  public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    Player player = event.getEntity();
    if (player instanceof ServerPlayer serverPlayer) {
      SkillModel model = SkillModel.get(player);
    }
  }

  @SubscribeEvent
  public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    Player player = event.getEntity();
    if (player instanceof ServerPlayer serverPlayer) {
      SkillModel model = SkillModel.get(player);
    }
  }
}
