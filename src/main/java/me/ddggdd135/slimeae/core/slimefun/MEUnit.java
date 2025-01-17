package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.api.ItemRequest;
import me.ddggdd135.slimeae.api.ItemStorage;
import me.ddggdd135.slimeae.api.interfaces.IMEStorageObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * ME单元 相当于AE版本的原版大箱子
 * 测试使用
 */
public class MEUnit extends SlimefunItem implements IMEStorageObject, InventoryBlock {
    private static final int[] Slots = new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
        30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };

    public MEUnit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());

                if (blockMenu != null) {
                    blockMenu.dropItems(b.getLocation(), Slots);
                }
            }
        };
    }

    @Override
    @Nullable public IStorage getStorage(Block block) {
        return new IStorage() {
            @Override
            public void pushItem(@Nonnull ItemStack[] itemStacks) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return;
                for (ItemStack itemStack : itemStacks) {
                    ItemStack result = blockMenu.pushItem(itemStack, Slots);
                    if (result != null && !result.getType().isAir()) itemStack.setAmount(result.getAmount());
                    else itemStack.setAmount(0);
                }
                blockMenu.markDirty();
            }

            @Override
            public boolean contains(@Nonnull ItemRequest[] requests) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return false;
                return ItemUtils.contains(getStorage(), requests);
            }

            @Nonnull
            @Override
            public ItemStack[] tryTakeItem(@Nonnull ItemRequest[] requests) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return new ItemStack[0];
                Map<ItemStack, Integer> amounts = ItemUtils.getAmounts(ItemUtils.createItems(requests));
                ItemStorage found = new ItemStorage();

                for (ItemStack itemStack : amounts.keySet()) {
                    for (int slot : Slots) {
                        ItemStack item = blockMenu.getItemInSlot(slot);
                        if (item == null || item.getType().isAir()) continue;
                        if (SlimefunUtils.isItemSimilar(item, itemStack, true, false)) {
                            if (item.getAmount() > amounts.get(itemStack)) {
                                found.addItem(ItemUtils.createItems(itemStack, amounts.get(itemStack)));
                                int rest = item.getAmount() - amounts.get(itemStack);
                                item.setAmount(rest);
                                break;
                            } else {
                                found.addItem(ItemUtils.createItems(itemStack, item.getAmount()));
                                blockMenu.replaceExistingItem(slot, new ItemStack(Material.AIR));
                                int rest = amounts.get(itemStack) - item.getAmount();
                                if (rest != 0) amounts.put(itemStack, rest);
                                else break;
                            }
                        }
                    }
                }
                blockMenu.markDirty();
                return found.toItemStacks();
            }

            @Override
            public @Nonnull Map<ItemStack, Integer> getStorage() {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return new HashMap<>();
                return ItemUtils.getAmounts(blockMenu.getContents());
            }

            @Override
            public int getEmptySlots() {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return 0;
                int found = 0;
                for (int slot : Slots) {
                    ItemStack itemStack = blockMenu.getItemInSlot(slot);
                    if (itemStack == null || itemStack.getType().isAir()) found += 1;
                }
                return found;
            }

            @Override
            public boolean canHasEmptySlots() {
                return true;
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return Slots;
    }

    @Override
    public int[] getOutputSlots() {
        return Slots;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void init(@Nonnull BlockMenuPreset preset) {
        preset.setSize(6 * 9);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {}
}
