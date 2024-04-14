package me.ddggdd135.slimeae.core.listeners;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SCMenu;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {
    private static final int MACHINE_GUIDE_DISPLAY_SLOT = 16;
    private static final int MACHINE_RECIPE_DISPLAY_SLOT = 4;
    private static final int MENU_SIZE = 54;

    private static final int[] INPUT_BORDER = {18, 19, 20, 21, 27, 30, 36, 37, 38, 39};
    private static final int[] OUTPUT_BORDER = {23, 24, 25, 26, 32, 35, 41, 42, 43, 44};
    private static final int[] INPUT_SLOTS = {28, 29};
    private static final int[] OUTPUT_SLOTS = {33, 34};

    public static final NamespacedKey SF_KEY = new NamespacedKey(Slimefun.getPlugin(Slimefun.class), "slimefun_item");
    public static final NamespacedKey INDEX_KEY = new NamespacedKey(SlimeAEPlugin.getInstance(), "recipe_index");

    @EventHandler
    public void onDualRecipeClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        Inventory inventory = e.getClickedInventory();

        if (inventory == null) {
            return;
        }

        ItemStack backButton = inventory.getItem(0);

        if (clickedItem == null || backButton == null) {
            return;
        }

        PersistentDataContainer pdc = backButton.getItemMeta().getPersistentDataContainer();

        if (!pdc.has(SF_KEY, PersistentDataType.STRING)
                || !pdc.get(SF_KEY, PersistentDataType.STRING).equals("_UI_BACK")
                || !pdc.has(INDEX_KEY, PersistentDataType.INTEGER)) {
            return;
        }

        // At this point, it has been confirmed that the player clicked a dual input or output item and is in a sf guide
        Player p = (Player) e.getWhoClicked();
        SlimefunItem machine = SlimefunItem.getByItem(e.getClickedInventory().getItem(MACHINE_GUIDE_DISPLAY_SLOT));
        SCMenu menu = new SCMenu(Slimefun.getLocalization().getMessage(p, "guide" + ".title.main"));
        SurvivalSlimefunGuide guide = new SurvivalSlimefunGuide(false, false);
        if (!(machine instanceof AContainer)) {
            return;
        }

        List<MachineRecipe> recipes = ((AContainer) machine).getMachineRecipes();
        int index = pdc.get(INDEX_KEY, PersistentDataType.INTEGER);

        menu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));
        menu.setSize(MENU_SIZE);
        menu.addBackButton(guide, p, PlayerProfile.find(p).get());
        menu.replaceExistingItem(MACHINE_RECIPE_DISPLAY_SLOT, machine.getItem());
        for (int i : INPUT_BORDER) {
            menu.replaceExistingItem(i, ChestMenuUtils.getInputSlotTexture());
        }
        for (int i : OUTPUT_BORDER) {
            menu.replaceExistingItem(i, ChestMenuUtils.getOutputSlotTexture());
        }
        for (ItemStack item : recipes.get(index).getInput()) {
            menu.pushItem(item, INPUT_SLOTS);
        }
        for (ItemStack item : recipes.get(index).getOutput()) {
            menu.pushItem(item, OUTPUT_SLOTS);
        }

        menu.setBackgroundNonClickable(true);
        menu.setPlayerInventoryClickable(false);

        menu.open(p);
    }
}
