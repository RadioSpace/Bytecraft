package info.bytecraft.database;

import org.bukkit.inventory.ItemStack;

public interface IItemDAO
{
    public int getValue(ItemStack mat) throws DAOException;
}
