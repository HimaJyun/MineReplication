package jp.jyn.minereplication;

import java.io.IOException;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import jp.jyn.minereplication.event.BlockEvent;
import jp.jyn.minereplication.network.ChangesReceiver;
import jp.jyn.minereplication.network.ChangesSender;
import jp.jyn.minereplication.network.ResendReceiver;
import jp.jyn.minereplication.packet.PacketManager;

/**
 * セキュリティなんかガバガバだよ～ん
 * @author HimaJyun
 *
 */
public class Main extends JavaPlugin {

	private ConfigStruct config;

	private PacketManager packetManager;

	private ChangesReceiver changesReceiver;
	private ChangesSender changesSender;
	private ResendReceiver resendReceiver;

	@Override
	public void onEnable() {
		// どれかがnullでなければリロード、もしくは起動失敗
		if (config != null) {
			// 無効化
			onDisable();
			// リロード
			config.reloadConfig();
		} else { // 初回起動
			config = new ConfigStruct(this);
		}

		try {
			changesSender = new ChangesSender(this);
			packetManager = new PacketManager(changesSender, this);
			changesReceiver = new ChangesReceiver(this);
			resendReceiver = new ResendReceiver(this);

			changesReceiver.start();
			changesSender.start();
			resendReceiver.start();

			new BlockEvent(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onDisable() {
		// イベント無効化
		HandlerList.unregisterAll(this);

		resendReceiver.stop();
		changesSender.stop();
		changesReceiver.stop();
	}

	public ConfigStruct getConfigStruct() {
		return config;
	}

	public PacketManager getPacketManager() {
		return packetManager;
	}
}
