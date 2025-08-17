
package org.hotamachisubaru.miniutility.GUI.holder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {

    private Inventory inv;
    public void bind(Inventory inv) {
        this.inv = inv;
    }
    @Override
    public Inventory getInventory(){
        return inv;
    }
}
// （必要に応じて NicknameHolder / TrashHolder / TrashConfirmHolder も同様）
