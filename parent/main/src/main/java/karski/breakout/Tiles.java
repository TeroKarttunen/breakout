package karski.breakout;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Tiles is a matrix of 6 rows of 20 tiles (binary numbers). Tiles can be constructed from a 30-byte hex String (values '0'-'f'),
 * consisting of 120 bites of data. The bits of data correspond to matrix values as follows.
 * 000 001 002 003 024 025 026 027 048 049 050 051 072 073 074 075 096 097 098 099
 * 004 005 006 007 028 029 030 031 052 053 054 055 076 077 078 079 100 101 102 103
 * 008 009 010 011 032 033 034 035 056 057 058 059 080 081 082 083 104 105 106 107
 * 012 013 014 015 036 037 038 039 060 061 062 063 084 085 086 087 108 109 110 111
 * 016 017 018 019 040 041 042 043 064 065 066 067 088 089 090 091 112 113 114 115
 * 020 021 022 023 044 045 046 047 068 069 070 071 092 093 094 095 116 117 118 119
 * @author tero
 *
 */
public class Tiles {

	private final static Logger LOGGER = Logger.getLogger(Tiles.class.getName());

	// six rows times twenty columns
	public boolean[][] tileMatrix = new boolean[6][20];

	/**
	 * This constructor is used to convert an emulator memory dump of tiles data to a tiles matrix object
	 * @param byteResponse C-64 emulator memory dump of tiles memory area
	 */
	public Tiles(byte[] byteResponse) {
		for (int j=0; j<6; j++) {
			for (int i=0; i<20; i++) {
				int pos = ((20 * j) + i) * 2;
				tileMatrix[j][i] = (convertSignedToUnsigned(byteResponse[pos]) == 1);
			}
		}
		
	}

	/**
	 * This constructor is used to convert an hex-encoded representation of tiles data to tiles matrix.
	 * Databank primary key contains data in this format. The reverse of this operation is contained in GameStateWithTiles code.
	 * @param hexencodedtiles
	 */
	public Tiles(String hexencodedtiles) {
		int byte1 = Integer.valueOf(hexencodedtiles.substring(0, 1), 16);
		int byte2 = Integer.valueOf(hexencodedtiles.substring(1, 2), 16);
		int byte3 = Integer.valueOf(hexencodedtiles.substring(2, 3), 16);
		int byte4 = Integer.valueOf(hexencodedtiles.substring(3, 4), 16);
		int byte5 = Integer.valueOf(hexencodedtiles.substring(4, 5), 16);
		int byte6 = Integer.valueOf(hexencodedtiles.substring(5, 6), 16);
		int byte7 = Integer.valueOf(hexencodedtiles.substring(6, 7), 16);
		int byte8 = Integer.valueOf(hexencodedtiles.substring(7, 8), 16);
		int byte9 = Integer.valueOf(hexencodedtiles.substring(8, 9), 16);
		int byte10 = Integer.valueOf(hexencodedtiles.substring(9, 10), 16);
		int byte11 = Integer.valueOf(hexencodedtiles.substring(10, 11), 16);
		int byte12 = Integer.valueOf(hexencodedtiles.substring(11, 12), 16);
		int byte13 = Integer.valueOf(hexencodedtiles.substring(12, 13), 16);
		int byte14 = Integer.valueOf(hexencodedtiles.substring(13, 14), 16);
		int byte15 = Integer.valueOf(hexencodedtiles.substring(14, 15), 16);
		int byte16 = Integer.valueOf(hexencodedtiles.substring(15, 16), 16);
		int byte17 = Integer.valueOf(hexencodedtiles.substring(16, 17), 16);
		int byte18 = Integer.valueOf(hexencodedtiles.substring(17, 18), 16);
		int byte19 = Integer.valueOf(hexencodedtiles.substring(18, 19), 16);
		int byte20 = Integer.valueOf(hexencodedtiles.substring(19, 20), 16);
		int byte21 = Integer.valueOf(hexencodedtiles.substring(20, 21), 16);
		int byte22 = Integer.valueOf(hexencodedtiles.substring(21, 22), 16);
		int byte23 = Integer.valueOf(hexencodedtiles.substring(22, 23), 16);
		int byte24 = Integer.valueOf(hexencodedtiles.substring(23, 24), 16);
		int byte25 = Integer.valueOf(hexencodedtiles.substring(24, 25), 16);
		int byte26 = Integer.valueOf(hexencodedtiles.substring(25, 26), 16);
		int byte27 = Integer.valueOf(hexencodedtiles.substring(26, 27), 16);
		int byte28 = Integer.valueOf(hexencodedtiles.substring(27, 28), 16);
		int byte29 = Integer.valueOf(hexencodedtiles.substring(28, 29), 16);
		int byte30 = Integer.valueOf(hexencodedtiles.substring(29, 30), 16);
		putValues(0, 0, byte1);
		putValues(1, 0, byte2);
		putValues(2, 0, byte3);
		putValues(3, 0, byte4);
		putValues(4, 0, byte5);
		putValues(5, 0, byte6);
		putValues(0, 4, byte7);
		putValues(1, 4, byte8);
		putValues(2, 4, byte9);
		putValues(3, 4, byte10);
		putValues(4, 4, byte11);
		putValues(5, 4, byte12);
		putValues(0, 8, byte13);
		putValues(1, 8, byte14);
		putValues(2, 8, byte15);
		putValues(3, 8, byte16);
		putValues(4, 8, byte17);
		putValues(5, 8, byte18);
		putValues(0, 12, byte19);
		putValues(1, 12, byte20);
		putValues(2, 12, byte21);
		putValues(3, 12, byte22);
		putValues(4, 12, byte23);
		putValues(5, 12, byte24);
		putValues(0, 16, byte25);
		putValues(1, 16, byte26);
		putValues(2, 16, byte27);
		putValues(3, 16, byte28);
		putValues(4, 16, byte29);
		putValues(5, 16, byte30);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tiles) {
			Tiles t = (Tiles) obj;
			if (!Arrays.equals(t.tileMatrix[0], tileMatrix[0])) return false;
			if (!Arrays.equals(t.tileMatrix[1], tileMatrix[1])) return false;
			if (!Arrays.equals(t.tileMatrix[2], tileMatrix[2])) return false;
			if (!Arrays.equals(t.tileMatrix[3], tileMatrix[3])) return false;
			if (!Arrays.equals(t.tileMatrix[4], tileMatrix[4])) return false;
			if (!Arrays.equals(t.tileMatrix[5], tileMatrix[5])) return false;
			return true;
		} else return false;
	}
	
	private int convertSignedToUnsigned(byte b) {
		if (b < 0) return b + 256;
		return b;
	}	
	
	/**
	 * Converts the tile matrix to human-viewable form for debugging purposes.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int j=0; j<6; j++) {
			for (int i=0; i<20; i++) {
				if (tileMatrix[j][i]) { 
					sb.append('x');
				} else {
					sb.append('.');
				}
			}
			sb.append('\n');
		}
		// TODO Auto-generated method stub
		return sb.toString();
	}
	
	public void putValues(int j, int i, int byt) {
		boolean a = false;
		boolean b = false;
		boolean c = false; 
		boolean d = false;
		if (byt >= 8) {
			a = true;
			byt = byt - 8;
		}
		if (byt >= 4) {
			b = true;
			byt = byt - 4;
		}
		if (byt >= 2) {
			c = true;
			byt = byt - 2;
		}
		if (byt >= 1) {
			d = true;
		}
	
		tileMatrix[j][i] = a;
		tileMatrix[j][i+1] = b;
		tileMatrix[j][i+2] = c;
		tileMatrix[j][i+3] = d;
	}
	
}
