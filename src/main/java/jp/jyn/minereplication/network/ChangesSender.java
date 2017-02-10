package jp.jyn.minereplication.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.jyn.minereplication.Main;

public class ChangesSender {

	private final DatagramSocket socket;
	private volatile boolean isAlive;

	private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final Set<InetSocketAddress> slave;

	public ChangesSender(Main main) throws IOException {
		this.socket = new DatagramSocket();
		slave = main.getConfigStruct().getSlave();
	}

	public void start() {
	}

	public void stop() {
		executor.shutdown();
	}

	private class SlaveSender implements Runnable {
		@Override
		public void run() {
			isAlive = true;
			try {
				for (byte[] packet : queue) {
					for (InetSocketAddress target : slave) {
						send(packet, target);
					}
					// 一瞬ずらさないと悲惨
					Thread.sleep(1);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				isAlive = false;
			}
		}

		private void send(byte[] packet, InetSocketAddress target) throws IOException {
			socket.send(new DatagramPacket(packet, packet.length, target.getAddress(), target.getPort()));
		}
	}

	/**
	 * パケットをキューに追加します。<br>
	 * 多分近いうちに送信されるけど、即座に送信する訳ではないんだよ、おぼえておいてね(はぁと
	 * @param packet 送信するパケット
	 */
	public void sendPacket(byte[] packet) {
		queue.add(packet);
		if (!isAlive) {
			executor.submit(new SlaveSender());
		}
	}

}
