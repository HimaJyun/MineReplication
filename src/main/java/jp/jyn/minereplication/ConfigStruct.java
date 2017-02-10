package jp.jyn.minereplication;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigStruct {
	/**
	 * 設定
	 */
	private FileConfiguration conf = null;
	/**
	 * 使用されるプラグイン
	 */
	private final Plugin plg;
	private static final int DEFAULT_PORT = 10000;

	private int serverPort;
	private Set<InetSocketAddress> slave;

	/**
	 * 各種設定構造体を初期化します。
	 * @param plugin 対象のプラグイン
	 */
	public ConfigStruct(Plugin plugin) {
		// プラグイン
		plg = plugin;

		// 読み込み
		reloadConfig();
	}

	/**
	 * 設定をリロードします。
	 * @return 
	 */
	public ConfigStruct reloadConfig() {
		// デフォルトを保存
		plg.saveDefaultConfig();
		if (conf != null) { // confが非null
			plg.reloadConfig();
		}
		// 設定を取得
		conf = plg.getConfig();

		serverPort = conf.getInt("ServerPort");

		Set<InetSocketAddress> tmp = new HashSet<>();
		for (String value : conf.getStringList("Slave")) {
			try {

				URI uri = new URI("MineReplication://" + value);
				String host = uri.getHost();
				if (host != null) { // ホスト正常
					int port = uri.getPort();
					if (port == -1) { // ポート無し
						port = DEFAULT_PORT;
					}
					tmp.add(new InetSocketAddress(host, port));

					continue; // 抜ける
				}

			} catch (URISyntaxException ignore) { // 無視
			}
			// ここに来る == ﾅﾝｶｴﾗｰﾀﾞｯﾃ
			plg.getLogger().warning("Invalid hostname:" + value);
		}
		slave = Collections.unmodifiableSet(tmp);

		return this;
	}

	/**
	 * サーバポートを取得する
	 * @return サーバポート
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * スレーブのSetを取得
	 * @return スレーブの一覧が入った変更不能Set
	 */
	public Set<InetSocketAddress> getSlave() {
		return slave;
	}
}
