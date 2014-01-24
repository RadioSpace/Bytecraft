package info.bytecraft.listener;

import java.util.HashMap;

import info.bytecraft.Bytecraft;
import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.api.ChestLog;
import info.bytecraft.api.PaperLog;
import info.bytecraft.api.PlayerBannedException;
import info.bytecraft.api.Rank;
import info.bytecraft.api.BytecraftPlayer.Flag;
import info.bytecraft.database.DAOException;
import info.bytecraft.database.IContext;
import info.bytecraft.database.ILogDAO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

public class BytecraftPlayerListener implements Listener
{
    private Bytecraft plugin;

    public BytecraftPlayerListener(Bytecraft bytecraft)
    {
        this.plugin = bytecraft;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        event.setJoinMessage(null);
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        if (player.getRank() == Rank.ELDER && player.hasFlag(Flag.INVISIBLE)) {
            for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                if (other.getRank() != Rank.ELDER) {
                    other.hidePlayer(player.getDelegate());
                }
                else {
                    other.sendMessage(player.getDisplayName() + ChatColor.RED
                            + " has joined invisible");
                }
            }
        }
        else {
            Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Welcome "
                    + player.getDisplayName() + ChatColor.DARK_AQUA
                    + " to bytecraft!");
            if (!player.hasPlayedBefore()) {
                player.teleport(plugin.getWorldSpawn("world"));
            }
            if (player.getRank() == Rank.NEWCOMER) {
                for (BytecraftPlayer other : plugin.getOnlinePlayers()) {
                    if (other.isMentor()) {
                        other.sendMessage(other.getDisplayName()
                                + ChatColor.AQUA
                                + " has joined as a newcomer, you should help them out!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        try {
            plugin.addPlayer(event.getPlayer());
        } catch (PlayerBannedException e) {
            event.disallow(Result.KICK_BANNED, e.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        if (player.hasFlag(Flag.INVISIBLE)) {
            event.setQuitMessage(null);
            plugin.removePlayer(player);
            return;
        }
        event.setQuitMessage(ChatColor.GRAY + "-QUIT- "
                + plugin.getPlayer(event.getPlayer()).getDisplayName()
                + ChatColor.AQUA + " has left the game");
        plugin.removePlayer(player);
        return;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof Player))
            return;
        BytecraftPlayer player = plugin.getPlayer((Player) event.getEntity());
        event.setCancelled(player.isAdmin());
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player) {
            if (event.getEntity() instanceof Player) {
                BytecraftPlayer player =
                        plugin.getPlayer((Player) event.getEntity());
                if (player.getCurrentZone() == null
                        || !player.getCurrentZone().isPvp()) {
                    event.setCancelled(true);
                    ((Player) event.getDamager()).sendMessage(ChatColor.RED
                            + "You are not in a pvp zone.");
                }
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event)
    {
        event.setLeaveMessage(null);
    }

    private HashMap<Item, BytecraftPlayer> droppedItems = Maps.newHashMap();

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        droppedItems.put(event.getItemDrop(), player);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event)
    {
        if (!droppedItems.containsKey(event.getItem()))
            return;
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        BytecraftPlayer from = droppedItems.get(event.getItem());
        ItemStack stack = event.getItem().getItemStack();

        if (from != null && (from != player)) {
            player.sendMessage(ChatColor.YELLOW + "You got " + ChatColor.GOLD
                    + stack.getAmount() + " "
                    + stack.getType().toString().toLowerCase()
                    + ChatColor.YELLOW + " from " + from.getDisplayName() + ".");
            from.sendMessage(ChatColor.YELLOW + "You gave "
                    + player.getDisplayName() + ChatColor.GOLD + " "
                    + stack.getAmount() + " "
                    + stack.getType().name().toLowerCase().replace("_", " "));
            droppedItems.remove(event.getItem());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event)
    {
        event.setRespawnLocation(plugin.getWorldSpawn(event.getPlayer().getWorld().getName()));
    }

    @EventHandler
    public void onCheck(PlayerInteractEvent event)
    {
        BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
        if (player.getItemInHand().getType() != Material.PAPER)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block block = event.getClickedBlock();
        try (IContext ctx = plugin.createContext()) {
            ILogDAO dao = ctx.getLogDAO();
            for (PaperLog log : dao.getLogs(block)) {
                player.sendMessage(ChatColor.GREEN + log.getPlayerName() + " "
                        + ChatColor.AQUA + log.getAction() + " "
                        + log.getMaterial() + ChatColor.GREEN + " at "
                        + log.getDate());
            }
            event.setCancelled(true);
            return;
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player))
            return;
        Player player = (Player) event.getPlayer();// For some reason its human
                                                   // entity
        if(!(event.getInventory().getHolder() instanceof Chest))return;
        
        Chest chest = (Chest)event.getInventory().getHolder();
        
        try(IContext ctx = plugin.createContext()){
            ILogDAO dao = ctx.getLogDAO();
            ChestLog log = new ChestLog(player.getName(), chest.getLocation(), ChestLog.Action.OPEN);
            
            dao.insertChestLog(log);
        }catch(DAOException e){
            throw new RuntimeException(e);
        }
    }
}