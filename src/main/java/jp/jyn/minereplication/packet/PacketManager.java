package jp.jyn.minereplication.packet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import jp.jyn.minereplication.Main;
import jp.jyn.minereplication.network.ChangesSender;

public class PacketManager {

	private final ChangesSender sender;

	// Byte.MAX_VALUE(127)より小さく、2の倍数
	private final static int OLD_SIZE = 126;
	private byte[][] oldPacket = new byte[OLD_SIZE][];
	private byte counter = 0;

	private final int rrport;

	public PacketManager(ChangesSender sender, Main main) {
		this.sender = sender;
		this.rrport = main.getConfigStruct().getServerPort();
	}

	/**
	 * ブロック設置パケットを送信します
	 * @param block 設置したブロック
	 */
	public void sendPlace(Block block) {
		send(new ChangesPacket(counter, rrport, block));
	}

	/**
	 * ブロック破壊パケットを送信します
	 * @param location 破壊したブロックの座標
	 */
	public void sendBreak(Location location) {
		send(new ChangesPacket(counter, rrport, location));
	}

	/**
	 * 特定のブロックを送信します。
	 * @param location ブロックの座標
	 * @param material 設置する物
	 */
	public void sendMaterial(Location location, Material material) {
		send(new ChangesPacket(counter, rrport, location, material));
	}

	private void send(ChangesPacket packet) {
		// 過去パケット保存
		byte[] tmp = packet.toBytes();
		oldPacket[counter] = tmp;
		counter = nextNumber(counter);

		// 送信
		sender.sendPacket(tmp);
	}

	/**
	 * 再送要求を送信しますお
	 * @param packetNumber エラーが起こったパケットの番号
	 * @param target エラーを起こしやがったマスタ鯖
	 */
	public ChangesPacket sendResendRequest(byte packetNumber, InetSocketAddress target) {
		try (Socket socket = new Socket(target.getAddress(), target.getPort());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream())) {
			out.writeByte(packetNumber);
			out.flush();

			Object obj = objIn.readObject();
			return (ChangesPacket) obj;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getPacket(byte packetNumber) {
		return oldPacket[(packetNumber & (OLD_SIZE - 1))];
	}

	/**
	 * 次に来るパケットのの番号
	 * @param current 今の番号
	 * @return (今の番号+1)%OLD_SIZE
	 */
	public static byte nextNumber(byte current) {
		current += 1;
		current &= (OLD_SIZE - 1); // MOD
		return current;
	}
}
