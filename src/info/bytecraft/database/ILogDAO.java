package info.bytecraft.database;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.api.PaperLog;
import info.bytecraft.blockfill.Fill;
import info.bytecraft.blockfill.Fill.Action;

public interface ILogDAO
{
    public void insertChatMessage(BytecraftPlayer player, String channel, String message) throws DAOException;
    public void insertTransactionLog(BytecraftPlayer giver, BytecraftPlayer recepient, long amount) throws DAOException;
    public void insertFillLog(BytecraftPlayer filler, Fill fill, Material mat, Action action) throws DAOException;
    public void insertPaperLog(BytecraftPlayer player, Location loc, Material mat, String action) throws DAOException;
    public boolean isLegal(Block block) throws DAOException;
    public List<PaperLog> getLogs(Block block) throws DAOException; 
}