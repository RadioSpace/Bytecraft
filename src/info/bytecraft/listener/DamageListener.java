package info.bytecraft.listener;

import info.bytecraft.Bytecraft;
import info.bytecraft.api.BytecraftPlayer;

import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener
{
    private Bytecraft plugin;

    public DamageListener(Bytecraft instance)
    {
        plugin = instance;
    }

    private Random random = new Random();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getDamager() instanceof Player))
            return;
        
        BytecraftPlayer player = plugin.getPlayer((Player) event.getDamager());

        ItemStack item = player.getItemInHand();
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                String s = "";
                String critChance = "";
                for (String string : lore) {
                    if (string.contains("Damage")) {
                        s = ChatColor.stripColor(string);
                    }
                    if (string.contains("Critical")) {
                        critChance = ChatColor.stripColor(string);
                    }
                }

                String[] args = s.split(": ");
                String[] crit = null;
                if (critChance != "") {
                    crit = critChance.split(": ");
                }
                if (s.equalsIgnoreCase(""))
                    return;
                int damage = 0;
                int critical = 0;
                try {
                    damage = Integer.parseInt(args[1]);
                    critical = Integer.parseInt(crit[1]);
                } catch (NumberFormatException e) {
                    damage = 0;
                    critical = 0;
                } catch (ArrayIndexOutOfBoundsException e) {
                    damage = 0;
                    critical = 0;
                } catch(NullPointerException e){
                    critical = 0;
                }

                double oldDamage = event.getDamage();
                double newDamage = (oldDamage + (damage / 10.0D));
                double criticalDamage = newDamage + ((damage / 10.0D) / 2.0D);
                if (critical != 0) {
                    int i = random.nextInt(99) + 1;
                    if(i <= critical){
                        event.setDamage(criticalDamage);
                    }
                    else {
                        event.setDamage(newDamage);
                    }
                }
                else {
                    event.setDamage(newDamage);
                }
            }
        }
    }

    @EventHandler
    public void onTakeDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        
        
        BytecraftPlayer player = plugin.getPlayer((Player) event.getEntity());

        ItemStack[] armor = player.getEquipment().getArmorContents();
        int armorBonus = 0;
        for (ItemStack item : armor) {
            if (!item.hasItemMeta()) {
                continue;
            }
            if (!item.getItemMeta().hasLore()) {
                continue;
            }
            List<String> lore = item.getItemMeta().getLore();
            String string = "";
            for (String s : lore) {
                if (s.contains("Armor")) {
                    string = ChatColor.stripColor(s);
                }
            }
            if (string.equalsIgnoreCase("")) {
                return;
            }
            String[] args = string.split(": ");
            int i = 0;
            try {
                i = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                i = 0;
            }
            armorBonus += i;
            if (armorBonus == 0)  {
                return;
            }
            
            double oldDamage = event.getDamage();
            //orignal damage from entity
            double newDamage = oldDamage - (armorBonus / 10.0D);
            
            if (newDamage < 0) {
                newDamage = 0;
            }
            event.setDamage(newDamage);
        }
    }

}
