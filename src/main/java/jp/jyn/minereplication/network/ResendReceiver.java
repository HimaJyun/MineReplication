package jp.jyn.minereplication.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.jyn.minereplication.Main;
import jp.jyn.minereplication.packet.PacketManager;

/**
 * 再送要求そのものが破損/欠損しちゃったら意味ないもんね、ここはTCPを使うよ
 * @author HimaJyun
 *
 */
public class ResendReceiver {

	private final ExecutorService pool = Executors.newCachedThreadPool();
	private final ServerSocket serverSocket;
	private volatile boolean running;

	private final PacketManager packetManager;

	public ResendReceiver(Main main) throws IOException {
		serverSocket = new ServerSocket(main.getConfigStruct().getServerPort());
		packetManager = main.getPacketManager();
	}

	public void start() {
		running = true;
		new Thread(() -> {
			try {
				while (running) {
					Socket socket = serverSocket.accept();
					pool.submit(new RRExecutor(socket));
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
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class RRExecutor implements Runnable {

		Socket socket;

		public RRExecutor(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try (Socket socket = this.socket;
					DataInputStream dataIn = new DataInputStream(socket.getInputStream());
					OutputStream out = socket.getOutputStream()) {
				byte[] packet = packetManager.getPacket(dataIn.readByte());
				out.write(packet, 0, packet.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
