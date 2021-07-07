package me.whizvox.compoundores.util;

import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.Tags;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OreDistribution {

  public static final int
      MAX_CHUNK_RADIUS = 24;

  private final ResourceLocation dimension;
  private final List<ChunkPos> chunksScanned;
  private final List<ChunkPos> chunksSkipped;
  private final int totalScanned;
  private final int totalMatching;
  private final Map<ResourceLocation, Collection<BlockPos>> raw;
  private final Map<ResourceLocation, Map<Integer, Integer>> levelsDist;
  private final Map<ResourceLocation, Integer> genDist;

  public OreDistribution(ResourceLocation dimension, List<ChunkPos> chunksScanned, List<ChunkPos> chunksSkipped, int totalScanned, Map<ResourceLocation, Collection<BlockPos>> raw) {
    this.dimension = dimension;
    this.chunksScanned = Collections.unmodifiableList(chunksScanned);
    this.chunksSkipped = Collections.unmodifiableList(chunksSkipped);
    this.totalScanned = totalScanned;
    this.raw = Collections.unmodifiableMap(raw);

    Map<ResourceLocation, Map<Integer, Integer>> tempLevelsDist = new HashMap<>();
    Map<ResourceLocation, Integer> tempGenDist = new HashMap<>();
    AtomicInteger totalMatchingRef = new AtomicInteger(0);
    raw.forEach((blockName, positions) -> {
      Map<Integer, Integer> levelData = new HashMap<>();
      AtomicInteger count = new AtomicInteger(0);
      positions.forEach(pos -> {
        levelData.put(pos.getY(), levelData.getOrDefault(pos.getY(), 0) + 1);
        count.incrementAndGet();
        totalMatchingRef.incrementAndGet();
      });
      tempLevelsDist.put(blockName, Collections.unmodifiableMap(levelData));
      tempGenDist.put(blockName, count.get());
    });
    totalMatching = totalMatchingRef.get();
    levelsDist = Collections.unmodifiableMap(tempLevelsDist);
    genDist = Collections.unmodifiableMap(tempGenDist);
  }

  public ResourceLocation getDimension() {
    return dimension;
  }

  public List<ChunkPos> getChunksScanned() {
    return chunksScanned;
  }

  public List<ChunkPos> getChunksSkipped() {
    return chunksSkipped;
  }

  public int getTotalScanned() {
    return totalScanned;
  }

  public int getTotalMatching() {
    return totalMatching;
  }

  public int getTotalChunks() {
    return chunksScanned.size() + chunksSkipped.size();
  }

  public Map<ResourceLocation, Collection<BlockPos>> getRaw() {
    return raw;
  }

  public Map<ResourceLocation, Map<Integer, Integer>> getLevelsDistribution() {
    return levelsDist;
  }

  public Map<ResourceLocation, Integer> getGeneralDistribution() {
    return genDist;
  }

  public static OreDistribution create(World world, BlockPos center, int chunkRadius) {
    assert chunkRadius <= MAX_CHUNK_RADIUS;
    ChunkPos cCenter = new ChunkPos(center);
    Map<ResourceLocation, Collection<BlockPos>> matching = new HashMap<>();
    List<ChunkPos> scanned = new ArrayList<>();
    List<ChunkPos> skipped = new ArrayList<>();
    int totalScanned = 0;
    Random rand = new Random();

    for (int cxoff = -chunkRadius; cxoff <= chunkRadius; cxoff++) {
      for (int zxoff = -chunkRadius; zxoff <= chunkRadius; zxoff++) {
        ChunkPos cPos = new ChunkPos(cCenter.x + cxoff, cCenter.z + zxoff);
        IChunk chunk = world.getChunk(cPos.x, cPos.z, ChunkStatus.FULL, false);
        if (chunk != null) {
          scanned.add(cPos);
          for (int x = cPos.getMinBlockX(); x <= cPos.getMaxBlockX(); x++) {
            for (int z = cPos.getMinBlockZ(); z <= cPos.getMaxBlockZ(); z++) {
              for (int y = 0; y <= world.getMaxBuildHeight(); y++) {
                BlockPos bPos = new BlockPos(x, y, z);
                BlockState state = world.getBlockState(bPos);
                if (!state.is(Blocks.AIR)) {
                  totalScanned++;
                  if (Tags.Blocks.ORES.contains(state.getBlock()) || OreComponentRegistry.getInstance().getComponentFromBlock(state.getBlock(), rand) != OreComponent.EMPTY) {
                    matching.computeIfAbsent(state.getBlock().getRegistryName(), n -> new ArrayList<>()).add(bPos);
                  }
                }
              }
            }
          }
        } else {
          skipped.add(cPos);
        }
      }
    }
    return new OreDistribution(world.dimension().getRegistryName(), scanned, skipped, totalScanned, matching);
  }

}
