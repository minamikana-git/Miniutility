package org.hotamachisubaru.miniutility.GUI.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class GuiHolder implements InventoryHolder {
    private Inventory inv;
    private final GuiType type;
    private final UUID owner;   // オーナー不要なら null でもOK

    public GuiHolder(GuiType type, UUID owner) {
        this.type = type;
        this.owner = owner;
    }

    public GuiType getType() { return type; }
    public UUID getOwner()   { return owner; }

    public void bind(Inventory inv) { this.inv = inv; }

    @Override
    public Inventory getInventory() { return inv; }
}
