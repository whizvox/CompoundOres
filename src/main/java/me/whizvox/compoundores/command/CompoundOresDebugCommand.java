package me.whizvox.compoundores.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.whizvox.compoundores.helper.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Set;
import java.util.stream.Collectors;

public class CompoundOresDebugCommand {

  private static final ITextComponent
    MSG_ITEMTAGS_NO_ITEM = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noItem"),
    MSG_ITEMTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noTags"),
    MSG_BLOCKTAGS_NO_BLOCK = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noBlock"),
    MSG_BLOCKTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noTags");

  private static boolean shouldExecute(CommandSource src) {
    return src.getEntity() instanceof PlayerEntity && src.getEntity().hasPermissions(2);
  }

  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    dispatcher.register(
      Commands.literal("compores")
        .then(Commands.literal("itemtags")
          .requires(CompoundOresDebugCommand::shouldExecute)
          .executes(CompoundOresDebugCommand::itemTags)
        )
        .then(Commands.literal("blocktags")
          .requires(CompoundOresDebugCommand::shouldExecute)
          .executes(CompoundOresDebugCommand::blockTags)
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

}
