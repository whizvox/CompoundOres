package me.whizvox.compoundores.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.whizvox.compoundores.api.CompoundOresObjects;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import me.whizvox.compoundores.config.CompoundOresConfig;
import me.whizvox.compoundores.helper.NBTHelper;
import me.whizvox.compoundores.helper.WorldHelper;
import me.whizvox.compoundores.network.CompoundOresNetwork;
import me.whizvox.compoundores.network.ExportComponentsPacket;
import me.whizvox.compoundores.network.ExportOreDistributionPacket;
import me.whizvox.compoundores.obj.CompoundOreBlock;
import me.whizvox.compoundores.obj.CompoundOreBlockItem;
import me.whizvox.compoundores.util.COBlockSnapshot;
import me.whizvox.compoundores.util.OreDistribution;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.whizvox.compoundores.CompoundOres.LOGGER;
import static me.whizvox.compoundores.helper.Markers.SERVER;

public class CompoundOresCommand {

  private static final ITextComponent
    MSG_ITEMTAGS_NO_ITEM = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noItem"),
    MSG_ITEMTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.itemtags.noTags"),
    MSG_BLOCKTAGS_NO_BLOCK = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noBlock"),
    MSG_BLOCKTAGS_NO_TAGS = new TranslationTextComponent("message.compoundores.debugCommand.blocktags.noTags"),
    MSG_OREDIST_CLEAR_NONE = new TranslationTextComponent("message.compoundores.debugCommand.oredist.clear.none"),
    MSG_OREDIST_CLEAR_SINGLE = new TranslationTextComponent("message.compoundores.debugCommand.oredist.clear.success", new StringTextComponent("1").withStyle(TextFormatting.YELLOW)),
    MSG_OREDIST_VIEW_GENERAL_ALL = new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.general.header", new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.all").withStyle(TextFormatting.YELLOW)),
    MSG_OREDIST_VIEW_LEVELS_ALL = new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.levels.header", new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.all").withStyle(TextFormatting.YELLOW)),
    MSG_OREDIST_VIEW_NODIST = new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.noDist"),
    MSG_OREDIST_VIEW_NODATA = new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.noData");

  private static boolean shouldExecute(CommandSource src, int permissionLevel) {
    return src.getEntity() instanceof PlayerEntity && src.getEntity().hasPermissions(permissionLevel);
  }

  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    LiteralArgumentBuilder<CommandSource> builder = Commands.literal("compores")
      .then(Commands.literal("export")
        .requires(src -> shouldExecute(src, 2))
        .executes(ctx -> exportComponents(ctx, ExportComponentsPacket.Which.BOTH))
        .then(Commands.literal("both").executes(ctx -> exportComponents(ctx, ExportComponentsPacket.Which.BOTH)))
        .then(Commands.literal("components").executes(ctx -> exportComponents(ctx, ExportComponentsPacket.Which.COMPONENTS)))
        .then(Commands.literal("groups").executes(ctx -> exportComponents(ctx, ExportComponentsPacket.Which.GROUPS)))
      )
      .then(Commands.literal("give")
        .requires(src -> shouldExecute(src, 2))
        .then(Commands.argument("primary", OreComponentArgumentType.oreComponent())
          .then(Commands.argument("secondary", OreComponentArgumentType.oreComponent())
            .executes(ctx -> giveCompoundOre(ctx, 1))
            .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
              .executes(ctx -> giveCompoundOre(ctx, ctx.getArgument("count", Integer.class)))
            )
          )
        )
      );
    if (CompoundOresConfig.COMMON.registerDebugCommand.get()) {
      LOGGER.info(SERVER, "Registering CompoundOres debug subcommands");
      builder
        .then(Commands.literal("itemtags")
          .requires(src -> shouldExecute(src, 2))
          .executes(CompoundOresCommand::itemTags)
        )
        .then(Commands.literal("blocktags")
          .requires(src -> shouldExecute(src, 2))
          .executes(CompoundOresCommand::blockTags)
        )
        .then(Commands.literal("cleararea")
          .requires(src -> shouldExecute(src, 4))
          .executes(src -> clearArea(src, true))
          .then(Commands.literal("allores").executes(ctx -> clearArea(ctx, false)))
          .then(Commands.literal("restore").executes(CompoundOresCommand::undoClearedArea))
        )
        .then(Commands.literal("collage")
          .requires(src -> shouldExecute(src, 2))
          .executes(src -> createCollage(src, false))
          .then(Commands.literal("all").executes(ctx -> createCollage(ctx, true)))
        )
        .then(Commands.literal("oredist")
          .requires(src -> shouldExecute(src, 2))
          .then(Commands.literal("create")
            .executes(ctx -> createOreDistribution(ctx, 4))
            .then(Commands.argument("radius", IntegerArgumentType.integer(1, OreDistribution.MAX_CHUNK_RADIUS))
              .executes(ctx -> createOreDistribution(ctx, IntegerArgumentType.getInteger(ctx, "radius")))
            )
          )
          .then(Commands.literal("view")
            .executes(ctx -> viewOreDistribution(ctx, true, Blocks.AIR))
            .then(Commands.literal("general")
              .executes(ctx -> viewOreDistribution(ctx, true, Blocks.AIR))
              .then(Commands.argument("filter", BlockStateArgument.block())
                .executes(ctx -> viewOreDistribution(ctx, true, BlockStateArgument.getBlock(ctx, "filter").getState().getBlock()))
              )
            ).then(Commands.literal("levels")
              .executes(ctx -> viewOreDistribution(ctx, false, Blocks.AIR))
              .then(Commands.argument("filter", BlockStateArgument.block())
                .executes(ctx -> viewOreDistribution(ctx, false, BlockStateArgument.getBlock(ctx, "filter").getState().getBlock()))
              )
            )
          )
          .then(Commands.literal("export").executes(CompoundOresCommand::exportOreDistribution))
          .then(Commands.literal("clear").
            executes(ctx -> clearOreDistribution(ctx, false))
            .then(Commands.literal("all").executes(ctx -> clearOreDistribution(ctx, true)))
          )
        );
    }
    LOGGER.debug(SERVER, "Registering CompoundOres /compores command");
    dispatcher.register(builder);
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

  private static int exportComponents(CommandContext<CommandSource> ctx, ExportComponentsPacket.Which which) {
    CompoundOresNetwork.sendToPlayer(new ExportComponentsPacket(which), (ServerPlayerEntity) ctx.getSource().getEntity());
    return 1;
  }

  private static int giveCompoundOre(CommandContext<CommandSource> ctx, int count) {
    OreComponent primary = ctx.getArgument("primary", OreComponent.class);
    OreComponent secondary = ctx.getArgument("secondary", OreComponent.class);
    CompoundOreBlockItem blockItem = CompoundOresObjects.blockItems.get(primary.getRegistryName());
    ItemStack stack = new ItemStack(blockItem, count);
    NBTHelper.writeOreComponent(stack, CompoundOreBlockItem.TAG_SECONDARY, secondary);
    final ITextComponent stackDisplayName = stack.getDisplayName();
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    player.inventory.add(stack);
    player.playSound(SoundEvents.ITEM_PICKUP, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
    player.displayClientMessage(new TranslationTextComponent("commands.give.success.single", count, stackDisplayName, player.getDisplayName()), false);
    return 1;
  }

  private static int createCollage(CommandContext<CommandSource> ctx, boolean ignoreConfig) {
    ServerWorld world = ctx.getSource().getLevel();
    AtomicInteger zoff = new AtomicInteger(0);
    OreComponentRegistry.getInstance().getSortedValues().forEach(primary -> {
      BlockPos src = ctx.getSource().getEntity().blockPosition();
      AtomicInteger xoff = new AtomicInteger(0);
      Stream<OreComponent> stream;
      if (ignoreConfig) {
        stream = OreComponentRegistry.getInstance().getSortedValues().stream().filter(c -> !c.equals(primary));
      } else {
        stream = OreComponentRegistry.getInstance().getLootTable(primary).all().stream();
      }
      stream.forEach(secondary -> {
        WorldHelper.placeCompoundOreBlock(world, src.offset(xoff.getAndIncrement(), 0, zoff.get()), primary, secondary);
      });
      zoff.getAndIncrement();
    });
    return 1;
  }

  private static Map<UUID, OreDistribution> oreDistributions = new HashMap<>();

  private static int clearOreDistribution(CommandContext<CommandSource> ctx, boolean forAll) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    if (forAll) {
      if (oreDistributions.isEmpty()) {
        player.displayClientMessage(MSG_OREDIST_CLEAR_NONE, false);
      } else {
        int size = oreDistributions.size();
        oreDistributions.clear();
        if (size == 1) {
          player.displayClientMessage(MSG_OREDIST_CLEAR_SINGLE, false);
        } else {
          player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.oredist.clear.success",
            new StringTextComponent(Integer.toString(size)).withStyle(TextFormatting.YELLOW)), false);
        }
      }
    } else {
      if (oreDistributions.containsKey(player.getUUID())) {
        oreDistributions.remove(player.getUUID());
        player.displayClientMessage(MSG_OREDIST_CLEAR_SINGLE, false);
      } else {
        player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.oredist.clear.playerNone",
          player.getDisplayName()), false);
      }
    }
    return 1;
  }

  private static int createOreDistribution(CommandContext<CommandSource> ctx, int radius) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    World world = player.getCommandSenderWorld();
    OreDistribution oreDist = OreDistribution.create(world, player.blockPosition(), radius);
    oreDistributions.put(player.getUUID(), oreDist);
    player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.oredist.create.success",
      new StringTextComponent(Integer.toString(oreDist.getChunksScanned().size())).withStyle(TextFormatting.YELLOW),
      new StringTextComponent(Integer.toString(oreDist.getTotalMatching())).withStyle(TextFormatting.AQUA)), false);
    return 1;
  }

  private static int viewOreDistribution(CommandContext<CommandSource> ctx, boolean general, Block block) {
    PlayerEntity player = (PlayerEntity) ctx.getSource().getEntity();
    OreDistribution dist = oreDistributions.get(player.getUUID());
    if (dist == null) {
      throw new CommandException(MSG_OREDIST_VIEW_NODIST);
    }
    if (general) {
      if (block.is(Blocks.AIR)) {
        player.displayClientMessage(MSG_OREDIST_VIEW_GENERAL_ALL, false);
        dist.getGeneralDistribution().forEach((blockKey, count) -> displayGeneralOreDistData(player, blockKey, count));
      } else {
        player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.general.header",
          new StringTextComponent(block.getRegistryName().toString()).withStyle(TextFormatting.YELLOW)), false);
        displayGeneralOreDistData(player, block.getRegistryName(), dist.getGeneralDistribution().getOrDefault(block.getRegistryName(), 0));
      }
    } else {
      if (block.is(Blocks.AIR)) {
        player.displayClientMessage(MSG_OREDIST_VIEW_LEVELS_ALL, false);
        dist.getLevelsDistribution().forEach((blockKey, levelsData) -> displayLevelsOreDistData(player, blockKey, levelsData));
      } else {
        player.displayClientMessage(new TranslationTextComponent("message.compoundores.debugCommand.oredist.view.levels.header",
            new StringTextComponent(block.getRegistryName().toString()).withStyle(TextFormatting.YELLOW)), false);
        displayLevelsOreDistData(player, block.getRegistryName(), dist.getLevelsDistribution().getOrDefault(block.getRegistryName(), Collections.emptyMap()));
      }
    }
    return 1;
  }

  private static int exportOreDistribution(CommandContext<CommandSource> ctx) {
    OreDistribution dist = oreDistributions.get(ctx.getSource().getEntity().getUUID());
    if (dist == null) {
      throw new CommandException(MSG_OREDIST_VIEW_NODIST);
    }
    CompoundOresNetwork.sendToPlayer(new ExportOreDistributionPacket(dist), (ServerPlayerEntity) ctx.getSource().getEntity());
    return 1;
  }

  private static void displayGeneralOreDistData(PlayerEntity player, ResourceLocation blockKey, int count) {
    player.displayClientMessage(new StringTextComponent("- ")
        .append(new StringTextComponent(blockKey.toString()).withStyle(TextFormatting.YELLOW))
        .append(new StringTextComponent(": ").withStyle(TextFormatting.RESET))
        .append(new StringTextComponent(Integer.toString(count)).withStyle(TextFormatting.AQUA)),
      false
    );
  }

  private static void displayLevelsOreDistData(PlayerEntity player, ResourceLocation blockKey, Map<Integer, Integer> levelsData) {
    if (levelsData.isEmpty()) {
      player.displayClientMessage(new StringTextComponent("- ")
        .append(new StringTextComponent(blockKey.toString()).withStyle(TextFormatting.YELLOW))
        .append(new StringTextComponent(":").withStyle(TextFormatting.RESET))
        .append(MSG_OREDIST_VIEW_NODATA), false
      );
    }
    List<Pair<Integer, Integer>> sortedLevelsData = levelsData.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    sortedLevelsData.sort(Comparator.comparingInt(Pair::getLeft));
    player.displayClientMessage(new StringTextComponent("- ")
        .append(new StringTextComponent(blockKey.toString()).withStyle(TextFormatting.YELLOW))
        .append(new StringTextComponent(":").withStyle(TextFormatting.RESET)), false
    );
    sortedLevelsData.forEach(pair -> {
      player.displayClientMessage(new StringTextComponent("  - ")
          .append(new StringTextComponent(Integer.toString(pair.getLeft())).withStyle(TextFormatting.GRAY))
          .append(new StringTextComponent(": ").withStyle(TextFormatting.RESET))
          .append(new StringTextComponent(Integer.toString(pair.getRight())).withStyle(TextFormatting.AQUA)), false
      );
    });
  }

}
