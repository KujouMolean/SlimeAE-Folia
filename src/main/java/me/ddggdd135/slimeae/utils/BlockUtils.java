package me.ddggdd135.slimeae.utils;

import com.molean.Folia;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockUtils {
    public static void breakBlock(@Nonnull Block block, @Nonnull Player player) {
        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        Folia.getPluginManager().ce(breakEvent);
        if (!breakEvent.isCancelled()) {
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(block.getLocation());
            block.setType(Material.AIR);
        }
    }
}
