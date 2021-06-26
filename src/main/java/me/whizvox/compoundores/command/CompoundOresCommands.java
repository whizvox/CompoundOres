package me.whizvox.compoundores.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

public class CompoundOresCommands {

  private CompoundOresCommands() {}

  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    CompoundOresCommand.register(dispatcher);
  }

}
