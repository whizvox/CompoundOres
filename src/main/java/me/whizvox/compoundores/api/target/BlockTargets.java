package me.whizvox.compoundores.api.target;

import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.*;
import java.util.stream.Collectors;

public class BlockTargets {

  /**
   * Create a new block target based on a varargs array of objects.
   * @param targets The target candidates to add to the final target. These targets must be instances of one of the
   *                following:
   *                <ul>
   *                  <li>
   *                    {@link CharSequence}: must either be a resource location that resolves to a block (i.e.
   *                    <code>minecraft:coal_ore</code>) or a tag (i.e. <code>#forge:ores/gold</code>). Tag definitions
   *                    must start with a hash (#).
   *                  </li>
   *                  <li>{@link Block}</li>
   *                  <li>{@link ITag}&lt;{@link Block}&gt;</li>
   *                </ul>
   * @return The final block target that consists of all targets specified by the argument
   * @throws IllegalArgumentException In case any of the passed targets does not conform to the listed specifications
   * @throws ResourceLocationException If any of the {@link CharSequence}-based arguments cannot be parsed as
   *                                   {@link ResourceLocation}s
   * @throws NullPointerException If any of the targets are null
   */
  public static IBlockTarget create(Object... targets) {
    if (targets == null || targets.length == 0) {
      throw new IllegalArgumentException("Target candidates array must be nonnull and not empty");
    }
    Set<ResourceLocation> blockNames = new HashSet<>();
    List<ITag<Block>> tags = new ArrayList<>();
    Arrays.stream(targets).forEach(target -> {
      if (target instanceof CharSequence) {
        String targetStr = ((CharSequence) target).toString();
        if (!targetStr.isEmpty() && targetStr.charAt(0) == '#') {
          tags.add(BlockTags.createOptional(new ResourceLocation(targetStr.substring(1))));
        } else {
          blockNames.add(new ResourceLocation(targetStr));
        }
      } else if (target instanceof Block) {
        blockNames.add(((Block) target).getRegistryName());
      } else if (target instanceof ITag) {
        try {
          ITag<Block> tag = (ITag<Block>) target;
          tags.add(tag);
        } catch (ClassCastException e) {
          throw new IllegalArgumentException(String.format("Could not cast IITag target to ITag<Block>: [%s] %s", target.getClass(), target));
        }
      } else if (target != null) {
        throw new IllegalArgumentException(String.format("Illegal block target candidate type: [%s] %s", target.getClass(), target));
      } else {
        throw new NullPointerException("No block target candidates can be null");
      }
    });
    IBlockTarget target;
    if (blockNames.isEmpty()) {
      if (tags.isEmpty()) {
        throw new IllegalArgumentException(String.format("Could not resolve any targets to anything usable: [%s]", Arrays.stream(targets).map(String::valueOf).collect(Collectors.joining(", "))));
      } else if (tags.size() == 1) {
        target = new TagTarget(tags.get(0));
      } else {
        target = new MultiTagTarget(tags);
      }
    } else if (tags.isEmpty()) {
      if (blockNames.size() == 1) {
        target = new NameTarget(blockNames.stream().findAny().get());
      } else {
        target = new MultiNameTarget(blockNames);
      }
    } else {
      target = new CompositeBlockTarget(blockNames, tags);
    }
    return new CachedTargetWrapper(target);
  }

  /**
   * Does the same thing as the non-safe {@link #create(Object...)}, but guaranteed to return a non-null value without
   * throwing an exception.
   * @return Either the successfully-resolved {@link IBlockTarget} instance if no exceptions were thrown, otherwise
   *         will return {@link IBlockTarget#NONE} instead
   * @see #create(Object...)
   */
  public static IBlockTarget tryCreate(Object... targets) {
    try {
      return create(targets);
    } catch (IllegalArgumentException | ResourceLocationException | NullPointerException ignored) {}
    return IBlockTarget.NONE;
  }

}
