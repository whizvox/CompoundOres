package me.whizvox.compoundores.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.whizvox.compoundores.helper.WorldHelper;
import me.whizvox.compoundores.network.CompoundOresNetwork;
import me.whizvox.compoundores.network.GenerateComponentsPacket;
import me.whizvox.compoundores.obj.CompoundOreBlock;
import me.whizvox.compoundores.util.COBlockSnapshot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import java.util.*;
import java.util.stream.Collectors;

public class CompoundOresDebugCommand {

  private static final ITextComponent
    MSG_ITEMTAGS_NO_ITEM = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noItem"),
    MSG_ITEMTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noTags"),
    MSG_BLOCKTAGS_NO_BLOCK = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noBlock"),
    MSG_BLOCKTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noTags");

  private static boolean shouldExecute(CommandSource src, int permissionLevel) {
    return src.getEntity() instanceof PlayerEntity && src.getEntity().hasPermissions(permissionLevel);
  }

  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    dispatcher.register(
      Commands.literal("compores")
        .then(Commands.literal("itemtags")
          .requires(src -> shouldExecute(src, 2))
          .executes(CompoundOresDebugCommand::itemTags)
        )
        .then(Commands.literal("blocktags")
          .requires(src -> shouldExecute(src, 2))
          .executes(CompoundOresDebugCommand::blockTags)
        )
        .then(Commands.literal("cleararea")
          .requires(src -> shouldExecute(src, 4))
          .executes(ctx -> clearArea(ctx, true))
          .then(Commands.literal("allores").executes(ctx -> clearArea(ctx, false)))
          .then(Commands.literal("restore").executes(CompoundOresDebugCommand::undoClearedArea))
        )
        .then(Commands.literal("generate")
          .requires(src -> shouldExecute(src, 2))
          .executes(CompoundOresDebugCommand::generateCompounds)
        )
    );
  }

  private static int itemTags(CommandContext<CommandSource> ctx) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    ItemStack stack = player.inventory.getSelected();
    if (stack.isEmpty()) {
      throw new CommandException(MSG_ITEMTAGS_NO_ITEM);
    }
    Set<ResourceLocation> tags = stack.getItem().getTags();
    if (tags.isEmpty()) {
      player.displayClientMessage(MSG_ITEMTAGS_NO_TAGS, false);
    } else {
      player.displayClientMessage(
        new TranslationTextComponent(
          "message.compoundores.debugCommand.tagList",
          new StringTextComponent(tags.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")))
            .withStyle(TextFormatting.GREEN)
        ), false
      );
    }
    return 1;
  }

  private static int blockTags(CommandContext<CommandSource> ctx) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    BlockRayTraceResult lookingAt = WorldHelper.getLookingAt(player);
    if (lookingAt.getType() == RayTraceResult.Type.MISS) {
      throw new CommandException(MSG_BLOCKTAGS_NO_BLOCK);
    }
    Block block = player.getCommandSenderWorld().getBlockState(lookingAt.getBlockPos()).getBlock();
    Set<ResourceLocation> tags = block.getTags();
    if (tags.isEmpty()) {
      player.displayClientMessage(MSG_BLOCKTAGS_NO_TAGS, false);
    } else {
      player.displayClientMessage(
        new TranslationTextComponent(
          "message.compoundores.debugCommand.tagList",
          new StringTextComponent(tags.stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")))
            .withStyle(TextFormatting.GREEN)
        ), false
      );
    }
    return 1;
  }

  private static Map<UUID, List<COBlockSnapshot>> deletedBlocks = new HashMap<>();

  private static int undoClearedArea(CommandContext<CommandSource> ctx) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    UUID entityId = player.getUUID();
    List<COBlockSnapshot> blocks = deletedBlocks.get(entityId);
    int total = 0;
    if (blocks != null) {
      blocks.forEach(COBlockSnapshot::restore);
      total += blocks.size();
      deletedBlocks.remove(entityId);
    }
    player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.cleararea.restored", total), false);
    return 1;
  }

  private static int clearArea(CommandContext<CommandSource> ctx, boolean onlyCompoundOres) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    World world = player.getCommandSenderWorld();
    List<COBlockSnapshot> blocks = new ArrayList<>();
    final int WIDTH = 25;
    for (int x = -WIDTH; x <= WIDTH; x++) {
      for (int z = -WIDTH; z <= WIDTH; z++) {
        for (int y = 0; y < player.blockPosition().getY(); y++) {
          BlockPos pos = player.blockPosition().offset(x, -y, z);
          Block block = world.getBlockState(pos).getBlock();
          if (onlyCompoundOres ? !(block instanceof CompoundOreBlock) : !Tags.Blocks.ORES.contains(block)) {
            blocks.add(COBlockSnapshot.create(world, pos));
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
          }
        }
      }
    }
    deletedBlocks.put(player.getUUID(), blocks);
    player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.cleararea.cleared", blocks.size()), false);
    return 1;
  }

  private static int generateCompounds(CommandContext<CommandSource> ctx) {
    CompoundOresNetwork.sendToPlayer(new GenerateComponentsPacket(), (ServerPlayerEntity) ctx.getSource().getEntity());
    return 1;
  }

}
