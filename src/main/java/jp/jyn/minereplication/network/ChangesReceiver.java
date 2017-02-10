package jp.jyn.minereplication.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import jp.jyn.minereplication.Main;
import jp.jyn.minereplication.packet.ChangesPacket;
import jp.jyn.minereplication.packet.PacketManager;

public class ChangesReceiver {

	private final static int BUFFER_SIZE = 1024;

	private final DatagramSocket socket;
	private volatile boolean running;

	private final PacketManager packetManager;
	private final Plugin plugin;

	private final ExecutorService pool = Executors.newCachedThreadPool();

	// 次に来るべきパケット番号
	private final Map<InetAddress, Byte> number = new ConcurrentHashMap<>();

	public ChangesReceiver(Main main) throws IOException {
		this.plugin = main;
		this.socket = new DatagramSocket(main.getConfigStruct().getServerPort());
		this.packetManager = main.getPacketManager();
	}

	public void start() {
		running = true;
		new Thread(() -> {
			try {
				while (running) {
					DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
					socket.receive(packet);
					pool.submit(new ReceiveExecutor(packet));
				}
			} catch (SocketException ignore) { // 無視(ソケットクローズ時の物なので)
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void stop() {
		running = false;
		pool.shutdown();
		socket.close();
	}

	private /*static*/ class ReceiveExecutor implements Runnable {

		private DatagramPacket packet;

		public ReceiveExecutor(DatagramPacket packet) {
			this.packet = packet;
		}

		@Override
		public void run() {
			ChangesPacket data;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
					ObjectInputStream objIn = new ObjectInputStream(byteIn)) {
				Object obj = objIn.readObject();
				if (!(obj instanceof ChangesPacket)) { // 変更パケットじゃない(多分ノイズとかブロードキャストで飛んできたﾅﾆｶ)
					return;
				}
				data = (ChangesPacket) obj;
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}

			// パリティチェック不一致
			InetAddress client = packet.getAddress();
			if (!data.parityCheck()) {
				data = packetManager.sendResendRequest(data.packetNumber, new InetSocketAddress(client, data.rrport));
			}

			// TODO:パケットの順番狂いと欠損対策

			// FIXME: 多分ここ連続でパケット来たらおかしくなる
			number.put(client, PacketManager.nextNumber(data.packetNumber));
			execChange(data);
		}
	}

	@SuppressWarnings("deprecation")
	private void execChange(ChangesPacket data) {
		World world = Bukkit.getWorld(data.world);
		if (world == null) {
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			Block block = world.getBlockAt(data.x, data.y, data.z);
			block.setTypeIdAndData(data.type, data.data, true);
		});
	}

}
