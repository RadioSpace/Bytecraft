package info.bytecraft.blockfill;

import info.bytecraft.Bytecraft;

import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import info.bytecraft.api.BytecraftPlayer;

public class Replacer extends AbstractFiller
{
    private History history;
    private BytecraftPlayer player;
    private MaterialData match;
    private MaterialData item;
    private SavedBlocks currentJob;

    public Replacer(Bytecraft plugin, History history, BytecraftPlayer player,
            Block block1, Block block2, MaterialData match, MaterialData item,
            int workSize)
    {
        super(plugin, block1, block2, workSize);

        this.history = history;
        this.player = player;
        this.match = match;
        this.item = item;
        this.currentJob = new SavedBlocks();
    }

    @Override
    public void changeBlock(Block block)
    {
        if (block.getTypeId() == match.getItemTypeId()
                && (match.getData() == 0 || block.getData() == match.getData())) {

            currentJob.addBlock(block.getState());

            block.setType(item.getItemType());
            block.setData(item.getData());
        }
    }

    @Override
    public void finished()
    {
        history.set(player, currentJob);
    }
}
