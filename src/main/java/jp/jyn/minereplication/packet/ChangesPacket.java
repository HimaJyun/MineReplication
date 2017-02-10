package jp.jyn.minereplication.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ChangesPacket implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * パケットの番号、1から連番、最大値(127)に到達したら1に戻る
	 */
	public final byte packetNumber;
	public final int rrport;

	public final String world;
	public final int x;
	public final int y;
	public final int z;

	public final int type;
	public final byte data;

	/**
	 * パケットに欠損がないかを確認するパリティビット<br>
	 * 全データを足し合わせた数が偶数か奇数かで判断する<br>
	 * false=偶数、true=奇数<br>
	 * ただし二ヵ所以上欠損、及びこれ自体が欠損したら検出不可、諦メロン
	 */
	private final boolean parity;

	/**
	 * 設置時
	 * @param packetNumber 何番目のパケットか
	 * @param rrport エラー発生時に再送要求を送るポート
	 * @param block 置いたブロック
	 */
	@SuppressWarnings("deprecation")
	public ChangesPacket(byte packetNumber, int rrport, Block block) {
		this(packetNumber, rrport, block.getLocation(), block.getTypeId(), block.getData());
	}

	/**
	 * 設置時2
	 * @param packetNumber 何番目のパケットか
	 * @param rrport エラー発生時に再送要求を送るポート
	 * @param location 置いたブロック
	 * @param material 設置する物
	 */
	@SuppressWarnings("deprecation")
	public ChangesPacket(byte packetNumber, int rrport, Location location, Material material) {
		this(packetNumber, rrport, location, material.getId(), (byte) 0);
	}

	/**
	 * 破壊時
	 * @param packetNumber 何番目のパケットか
	 * @param rrport エラー発生時に再送要求を送るポート
	 * @param location 置いたブロック
	 */
	public ChangesPacket(byte packetNumber, int rrport, Location location) {
		// 両方を0(Air)にしてしまう事で同じ処理で設置と破壊の両方を表現する
		this(packetNumber, rrport, location, 0, (byte) 0);
	}

	private ChangesPacket(byte packetNumber, int rrport, Location location, int type, byte data) {
		this.packetNumber = packetNumber;
		this.rrport = rrport;

		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();

		// 両方を0(Air)にしてしまう事で同じ処理で設置と破壊の両方を表現する
		this.type = type;
		this.data = data;

		// パリティ計算
		this.parity = parityCalc();
	}

	private boolean parityCalc() {
		byte tmp = 0;

		// & 1 == %2
		tmp += (this.packetNumber & 1);
		tmp += (this.world.hashCode() & 1);
		tmp += (this.x & 1);
		tmp += (this.y & 1);
		tmp += (this.z & 1);
		tmp += (this.type & 1);
		tmp += (this.data & 1);

		return (tmp & 1) == 1;
	}

	/**
	 * パリティチェック
	 * @return 正常ならtrue
	 */
	public boolean parityCheck() {
		return (parity == parityCalc());
	}

	/**
	 * バイト配列に変換する
	 * @return 変換されたバイト配列
	 * @throws IOException ﾅﾝｶｴﾗｰﾀﾞｯﾃ
	 */
	public byte[] toBytes() {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
			objOut.writeObject(this);
			return byteOut.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte getPacketNumber() {
		return packetNumber;
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int getType() {
		return type;
	}

	public byte getData() {
		return data;
	}

}
