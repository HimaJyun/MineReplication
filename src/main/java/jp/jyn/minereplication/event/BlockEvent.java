package jp.jyn.minereplication.event;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import jp.jyn.minereplication.Main;
import jp.jyn.minereplication.packet.PacketManager;

public class BlockEvent implements Listener {

	private final PacketManager packetManager;

	public BlockEvent(Main main) {
		packetManager = main.getPacketManager();
		main.getServer().getPluginManager().registerEvents(this, main);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent e) {
		p(e.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent e) {
		b(e.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityExplode(EntityExplodeEvent e) {
		for (Block block : e.blockList()) {
			b(block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void tntIgnite(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (e.getClickedBlock().getType() == Material.TNT &&
				e.getMaterial() == Material.FLINT_AND_STEEL) {
			b(e.getClickedBlock());
		}
	}

	/*
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerBucketEmpty(PlayerBucketEmptyEvent e) {
		Block block = e.getBlockClicked();
		System.out.println(block);
		System.out.println(e.getBucket());
		if (block.getType() == Material.STATIONARY_LAVA &&
				e.getBucket() == Material.WATER_BUCKET) { // 溶岩->水
			m(block.getLocation(), Material.OBSIDIAN);
			return;
		}
	
		Material mat;
		switch (e.getBucket()) {
		case WATER_BUCKET:
			mat = Material.STATIONARY_WATER;
			break;
		case LAVA_BUCKET:
			mat = Material.STATIONARY_LAVA;
			break;
		default:
			return;
		}
	
		m(block.getRelative(e.getBlockFace()).getLocation(), mat);
	}
	*/

	private void p(Block block) {
		packetManager.sendPlace(block);
	}

	private void b(Block block) {
		packetManager.sendBreak(block.getLocation());
	}

	/*
	private void m(Location l, Material m) {
		packetManager.sendMaterial(l, m);
	}
	*/
}
