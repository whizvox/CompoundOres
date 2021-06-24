package me.whizvox.compoundores.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.whizvox.compoundores.api.component.OreComponent;
import me.whizvox.compoundores.api.component.OreComponentRegistry;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.concurrent.CompletableFuture;

public class OreComponentArgumentType implements ArgumentType<OreComponent> {

  private OreComponentArgumentType() {
  }

  public static OreComponentArgumentType oreComponent() {
    return new OreComponentArgumentType();
  }

  @Override
  public OreComponent parse(StringReader reader) throws CommandSyntaxException {
    OreComponent oreComp = OreComponentRegistry.getInstance().getNonEmptyRegistry().get(ResourceLocation.read(reader));
    if (oreComp.isEmpty()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
    }
    return oreComp;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    return ISuggestionProvider.suggestResource(
      OreComponentRegistry.getInstance().getSortedValues().stream().map(ForgeRegistryEntry::getRegistryName),
      builder
    );
  }

}
