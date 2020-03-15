/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eqcoin.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * EQCType is an efficient binary serialization format for serializing and
 * deserializing the EQC blockchain. EQCType is based on the key-value pair, and
 * the EQCBits itself is embedded in the key-value pair.
 * <p>
 * There are 3 categories and 11 kinds of EQCType:
 * <p>
 * 1. BINxx: BINX, BIN8, BIN16, BIN24, BIN32.
 * 
 * BINxx stores a byte array whose length is up to (2^xx)-1 bytes. xx is the
 * maximum number of bits.
 * <p>
 * 2. EQCBits: EQCBits.
 * 
 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
 * the highest digit of which is a continuous label. If it is 1, it means that
 * the subsequent byte is still part of bytes. If it is 0, it means the current
 * byte is the last byte of bytes. The endian is little endian. 
 * | 1xxxxxxx | ... | 1xxxxxxx | 0xxxxxxx |
 * <p>
 * 3. ARRAY
 * 
 * ARRAYxx: ARRAYX, ARRAY8, ARRAY16, ARRAY24, ARRAY32.
 * 
 * ARRAY stores a byte array including xxx elements whose length up to (2^xx)-1
 * bytes. xx is the maximum number of bits. The length of elements use EQCBits
 * encoding.
 * 
 * @author Xun Wang
 * @date 9-21-2018
 * @email 10509759@qq.com
 */
public class EQCType {

	/**
	 * BINX stores a byte array whose length is from 9 up to 255 bytes. | xxxxxxxx |
	 * data | | XXXXXXXX | is a 8-bit unsigned integer which represents the length
	 * of data. The range is from 9 to 255 bytes.
	 * 
	 * ARRAYX stores a byte array including xxx elements whose length from 9 up to
	 * 255 bytes. The length of the ARRAY elements uses EQCBits encoding. | xxxxxxxx
	 * | EQCBits | data | | XXXXXXXX | is a 8-bit unsigned integer which represents
	 * the length of data. The range is from 9 to 255 bytes. | EQCBits | is a series
	 * of consecutive bytes which represents the length of ARRAY elements which use
	 * EQCBits encoding.
	 */
	public final static byte MIN_BINX_LEN = 9;
	public final static short MAX_BINX_LEN = 255;
	public final static int EOF = -1;

	/**
	 * For any BIN or ARRAY EQCType in case which represents the Object is null just
	 * save a NULL(0) in the bytes.
	 */
	public final static byte NULL = 0;
	
	public final static byte[] NULL_ARRAY = {NULL};
	
	/**
	 * BIN8 stores a byte array whose length is from 1 to 8 bytes. | 0x1 | XXXXXXXX
	 * | data | | XXXXXXXX | is a 8-bit unsigned integer which represents the length
	 * of data. The range is from 1 to 8 bytes.
	 */
	public final static byte BIN8 = 0x1;
	public final static int MAX_BIN8_LEN = 8;

	/**
	 * BIN16 stores a byte array whose length is up to (2^16)-1 bytes. | 0x2 |
	 * XXXXXXXX | XXXXXXXX | data | | XXXXXXXX | XXXXXXXX | is a 16-bit unsigned
	 * integer which represents the length of data.
	 */
	public final static byte BIN16 = 0x2;
	public final static int MAX_BIN16_LEN = 65535;

	/**
	 * BIN24 stores a byte array whose length is up to (2^24)-1 bytes. | 0x3 |
	 * XXXXXXXX | XXXXXXXX | XXXXXXXX| data | | XXXXXXXX | XXXXXXXX | XXXXXXXX | is
	 * a 24-bit unsigned integer which represents the length of data.
	 */
	public final static byte BIN24 = 0x3;
	public final static int MAX_BIN24_LEN = 16777215;

	/**
	 * BIN32 stores a byte array whose length is up to (2^32)-1 bytes. | 0x4 |
	 * XXXXXXXX | XXXXXXXX | XXXXXXXX | data | | XXXXXXXX| XXXXXXXX| XXXXXXXX |
	 * XXXXXXXX | is a 32-bit unsigned integer which represents the length of data.
	 */
	public final static byte BIN32 = 0x4;
	/*
	 * Due to Java only have signed int so here use long represent unsigned int.
	 */
	public final static long MAX_BIN32_LEN = 4294967295l;

	/**
	 * ARRAY8 stores a byte array including xxx elements whose length is from 1 to 8
	 * bytes. | 0x5 | EQCBits | XXXXXXXX | data | | EQCBits | is a series of
	 * consecutive bytes which represents the length of ARRAY elements which use
	 * EQCBits encoding. | XXXXXXXX | is a 8-bit unsigned integer which represents
	 * the length of data. The range is from 1 to 8 bytes.
	 */
	public final static byte ARRAY8 = 0x5;
	public final static int MAX_ARRAY8_LEN = MAX_BIN8_LEN;

	/**
	 * ARRAY16 stores a byte array including xxx elements whose length is up to
	 * (2^16)-1 bytes. | 0x6 | EQCBits | XXXXXXXX | XXXXXXXX | data | | EQCBits | is
	 * a series of consecutive bytes which represents the length of ARRAY elements
	 * which use EQCBits encoding. | XXXXXXXX | XXXXXXXX | is a 16-bit unsigned
	 * integer which represents the length of data.
	 */
	public final static byte ARRAY16 = 0x6;
	public final static int MAX_ARRAY16_LEN = MAX_BIN16_LEN;

	/**
	 * ARRAY24 stores a byte array including xxx elements whose length is up to
	 * (2^24)-1 bytes. | 0x7 | EQCBits | XXXXXXXX | XXXXXXXX | XXXXXXXX | data | |
	 * EQCBits | is a series of consecutive bytes which represents the length of
	 * ARRAY elements which use EQCBits encoding. | XXXXXXXX | XXXXXXXX | XXXXXXXX |
	 * is a 24-bit unsigned integer which represents the length of data.
	 */
	public final static byte ARRAY24 = 0x7;
	public final static int MAX_ARRAY24_LEN = MAX_BIN24_LEN;

	/**
	 * ARRAY32 stores a byte array including xxx elements whose length is up to
	 * (2^32)-1 bytes. | 0x8 | EQCBits | XXXXXXXX | XXXXXXXX| XXXXXXXX| XXXXXXXX |
	 * data | | EQCBits | is a series of consecutive bytes which represents the
	 * length of ARRAY elements which use EQCBits encoding. | XXXXXXXX| XXXXXXXX|
	 * XXXXXXXX | XXXXXXXX | is a 32-bit unsigned integer which represents the
	 * length of data.
	 */
	public final static byte ARRAY32 = 0x8;
	public final static long MAX_ARRAY32_LEN = MAX_BIN32_LEN;

	/**
	 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
	 * the highest digit of which is a continuous label. If it is 1, it means that
	 * the subsequent byte is still part of bytes. If it is 0, it means the current
	 * byte is the last byte of bytes. The endian is little endian. | 1xxxxxxx | ...
	 * | 1xxxxxxx | 0xxxxxxx |
	 */
	public final static byte EQCBITS = (byte) 128;
	public final static byte EQCBITS_BUFFER_LEN = 11;
	
	public static final NoSuchFieldException ZERO_EXCEPTION = new NoSuchFieldException("The ID shouldn't be zero.");
	
	public static final NoSuchFieldException NULL_OBJECT_EXCEPTION = new NoSuchFieldException("The Object shouldn't be null.");
	
	public static final IllegalStateException REDUNDANT_DATA_EXCEPTION = new IllegalStateException("Exists redundant data in current Object.");

	public static final IllegalStateException ARRAY_LENGTH_EXCEPTION = new IllegalStateException("ARRAY's length doesn't equal to it's actual length.");

	public static final IllegalStateException EOF_EXCEPTION = new IllegalStateException("The ByteArrayInputStream shouldn't end but read EOF.");

//	/**
//	 * Convert String to BINX using StandardCharsets.US_ASCII charset
//	 * 
//	 * @param foo The String which will be convert to BINX
//	 * @return String's bytes in BINX format
//	 */
//	public static byte[] stringToBINX(final String foo) {
//		return bytesToBINX(foo.getBytes(StandardCharsets.US_ASCII));
//	}

	public static byte[] bytesToBINX(final byte[] bytes) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (bytes == null) {
				os.write(NULL);
			} else {
				if (bytes.length < MIN_BINX_LEN || bytes.length > MAX_BINX_LEN) {
					throw new IllegalArgumentException(
							"Byte array's length shouldn't less than 9 or exceed 255. Len: " + bytes.length);
				}
//				os.write((byte) (BIN7 | (bytes.length & 0xFF)));
				os.write(bytes.length & 0xFF);
//				Log.info("bytesToBIN7's len: " +((byte) (BIN7 | (bytes.length & 0xFF))) );
//				Log.info(Util.dumpBytesLittleEndianBinary(new byte[] { (byte) (BIN7 | (bytes.length & 0xFF)) }));
				os.write(bytes);
//				Log.info(Util.dumpBytesLittleEndianBinary(bytes));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * Convert String to BIN using StandardCharsets.US_ASCII charset
	 * 
	 * @param foo The String which will be convert to BIN
	 * @return
	 */
	public static byte[] stringToBIN(final String foo) {
		if(foo == null) {
			return NULL_ARRAY;
		}
		return bytesToBIN(foo.getBytes(StandardCharsets.US_ASCII));
	}

	public static boolean isEQCBits(final int type) {
		return (((byte) type & EQCBITS) == EQCBITS);
	}

	public static boolean isBINX(final int type) {
		return type >= MIN_BINX_LEN && type <= MAX_BINX_LEN;
	}

	public static boolean isBINX(final byte[] bytes) {
		return (bytes.length == 1) && (bytes[0] >= MIN_BINX_LEN);
	}

	/**
	 * In EQCType serialization when BIN or ARRAY EQCType represent's Object is null
	 * will save a NULL
	 * 
	 * @param bytes
	 * @return boolean If current Object is null
	 */
	public static boolean isNULL(final byte[] bytes) {
		return (bytes != null) && (bytes.length == 1) && (bytes[0] == NULL);
	}

	public static boolean isBIN(final int type) {
		byte foo = (byte) type;
		return isBINX(type) || ((foo == BIN16) || (foo == BIN24) || (foo == BIN32) || (foo == BIN8));
	}

	public static boolean isARRAY(final int type) {
		byte foo = (byte) type;
		return (foo == ARRAY16) || (foo == ARRAY8) || (foo == ARRAY24) || (foo == ARRAY32) || isBINX(type);
	}

	public static int getBINTypeLen(final int type) {
		byte foo = (byte) type;
		int len = 0;
		if (foo == BIN16) {
			len = 2;
		} else if (foo == BIN24) {
			len = 3;
		} else if (foo == BIN32) {
			len = 4;
		} else if (foo == BIN8) {
			len = 1;
		}
		return len;
	}

	public static int getBINDataLen(final int type, byte[] bytes) {
		byte foo = (byte) type;
		int len = 0;
		if (foo == BIN16) {
			len = Util.bytesToInt(bytes);
		} else if (foo == BIN24) {
			len = Util.bytesToInt(bytes);
		} else if (foo == BIN32) {
			len = Util.bytesToInt(bytes);
		} else if (foo == BIN8) {
			len = Util.bytesToInt(bytes);
		}
		return len;
	}

	public static int getARRAYTypeLen(final int type) {
		byte foo = (byte) type;
		int len = 0;
		if (foo == ARRAY16) {
			len = 2;
		} else if (foo == ARRAY24) {
			len = 3;
		} else if (foo == ARRAY32) {
			len = 4;
		} else if (foo == ARRAY8) {
			len = 1;
		}
		return len;
	}

	public static int getARRAYDataLen(final int type, byte[] bytes) {
		byte foo = (byte) type;
		int len = 0;
		if (foo == ARRAY16) {
			len = Util.bytesToInt(bytes);
		} else if (foo == ARRAY24) {
			len = Util.bytesToInt(bytes);
		} else if (foo == ARRAY32) {
			len = Util.bytesToInt(bytes);
		} else if (foo == ARRAY8) {
			len = Util.bytesToInt(bytes);
		}
		return len;
	}

	/**
	 * Convert element's bytes to ARRAY in this ARRAY include the relevant
	 * element's bin this function will convert the element's bytes to bin in it
	 * @param vector
	 * @return
	 */
	public static byte[] bytesArrayToARRAY(Vector<byte[]> vector) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] bytes = null;
		try {
			// Stores a NULL placeholder for parsing data when there is corresponding Object
			// is null.
			if ((vector == null) || vector.size() == 0) {
				os.write(NULL);
			} else {
				bytes = bytesArrayToBytes(vector);
				if (bytes.length <= MAX_BIN8_LEN) {
					os.write(ARRAY8);
					os.write(longToEQCBits(vector.size()));
					os.write(Util.intToByte(bytes.length));
					os.write(bytes);
				} else if (bytes.length <= MAX_BINX_LEN) {
					os.write(bytes.length & 0xFF);
					os.write(longToEQCBits(vector.size()));
					os.write(bytes);
				} else if (bytes.length <= MAX_BIN16_LEN) {
					os.write(ARRAY16);
					os.write(longToEQCBits(vector.size()));
					os.write(Util.intTo2Bytes(bytes.length));
					os.write(bytes);
				} else if (bytes.length <= MAX_BIN24_LEN) {
					os.write(ARRAY24);
					os.write(longToEQCBits(vector.size()));
					os.write(Util.intTo3Bytes(bytes.length));
					os.write(bytes);
				} else if (bytes.length <= MAX_BIN32_LEN) {
					os.write(ARRAY32);
					os.write(longToEQCBits(vector.size()));
					os.write(Util.intToBytes(bytes.length));
					os.write(bytes);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public static byte[] bytesArrayToBytes(Vector<byte[]> vec) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (vec == null || vec.size() == 0) {
			os.write(NULL);
		} else {
			for (byte[] bytes : vec) { 
				try {
					os.write(bytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
		return os.toByteArray();
	}

	public static byte[] bytesToBIN(byte[] bytes) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Stores a NULL. placeholder for parsing data when there is no corresponding
			// data item.
			if (bytes == null) {
				os.write(NULL);
			} else if (bytes.length <= MAX_BIN8_LEN) {
				os.write(BIN8);
				os.write(Util.intToByte(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BINX_LEN) {
				os.write(bytesToBINX(bytes));
			} else if (bytes.length <= MAX_BIN16_LEN) {
				os.write(BIN16);
				os.write(Util.intTo2Bytes(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BIN24_LEN) {
				os.write(BIN24);
				os.write(Util.intTo3Bytes(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BIN32_LEN) {
				os.write(BIN32);
				os.write(Util.intToBytes(bytes.length));
				os.write(bytes);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public static byte[] parseBIN(ByteArrayInputStream is) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] data = null;
		byte[] bytes = null;
		byte[] len = null;
		int iLen = 0;

		assertNotTerminate(is);
		
		// Parse type
		type = is.read();
		assertNotEOF(type);
//		Log.info(Util.dumpBytesBigEndianBinary(new byte[] {(byte) type}));
		if (isNULL(type)) {
			bytes = new byte[] { NULL };
		} else if (isBINX(type)) {
//					Log.info("Bin7 len: " + type);
			data = new byte[type];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBINX Get BIN data's len  error occur record len != real len.");
			}
			bytes = data;
		} else if (isBIN(type)) {
			// Get BIN type len
			iLen = getBINTypeLen(type);
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBIN Get BIN type len error occur record len != real len.");
			}
			// Get BIN data's len
			iLen = getBINDataLen(type, data);
			// Read the content
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBIN Get BIN data's len error occur record len != real len.");
			}
			bytes = data;
		}
		return bytes;
	}

	public static ARRAY parseARRAY(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		ARRAY array = null;
		array = parseARRAY(is);
		return array;
	}

	public static ARRAY parseARRAY(ByteArrayInputStream is) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] data = null;
		byte[] bytes = null;
		byte[] elementLen = null;
		int iLen = 0;
		ARRAY array = new ARRAY();
		
		assertNotTerminate(is);
		// Parse type
		type = is.read();
		assertNotEOF(type);
		if(isNULL(type)) {
		} else if (isBINX(type)) {
			// Get element's length
			elementLen = parseEQCBits(is);
			// Get BIN data's len
			data = new byte[type];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseARRAY Get BIN data's len error occur record len != real len.");
			}
			array.size = eqcBitsToLong(elementLen);
			array.elements = data;
		} else if (isARRAY(type)) {
			// Get element's length
			elementLen = parseEQCBits(is);
			// Get ARRAY type len
			iLen = getARRAYTypeLen(type);
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseARRAY Get BIN type len error occur record len != real len.");
			}
			// Get ARRAY data's len
			iLen = getARRAYDataLen(type, data);
			// Read the content
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseARRAY Get BIN data's len error occur record len != real len.");
			}
			bytes = data;
			array.size = eqcBitsToLong(elementLen);
			array.elements = data;
		}
		return array;
	}

	public static byte[] parseEQCBits(ByteArrayInputStream is) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] bytes = null;
		
		assertNotTerminate(is);
		// Parse EQCBits
		ByteBuffer buff = ByteBuffer.allocate(EQCBITS_BUFFER_LEN);
		while ((((type = is.read()) != EOF) && ((byte) type & EQCBITS) != 0)) {
			buff.put((byte) type);
		}
		if (type != EOF) {
			buff.put((byte) type);
			buff.flip();
			bytes = buff.array();
		}
		else {
			throw EOF_EXCEPTION;
		}
		if(bytes.length > 1) {
			if((bytes[0] == 128) && (bytes[1] < 192) ) {
				throw new IllegalStateException("Bad format the highest byte can't be zero");
			}
		}
		return bytes;
	}
	
	public static ID parseID(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		return new ID(parseEQCBits(is));
	}

	public static boolean isInputStreamEnd(ByteArrayInputStream is) {
		return is.available() == 0;
	}
	
	public static boolean isNULL(ByteArrayInputStream is) {
		boolean boolisNULL = false;
		int type;
		byte[] data = null;
		byte[] bytes = null;
		int iLen = 0;
		try {
			// Parse type
			is.mark(0);
			type = is.read();
			is.reset();
			boolisNULL = isNULL(type);
//			if (isBIN7(type)) {
//				// Check if current data is null
//				if (parseBIN7Len(type) == 0) {
//					boolisNULL = true;
//				}
//			} 
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return boolisNULL;
	}

	public static boolean isNULL(int type) {
		return type == NULL;
	}

	public static class ARRAY {
		/**
		 * Due to the unsigned integer in java doesn't support very good and
		 * Vector&Array's size in java is integer type. But in EQCType Array's size type
		 * is unsigned integer So here use long to present unsigned integer.
		 */
		public long size;
		public byte[] elements;

		public ARRAY() {
		}

		public boolean isNULL() {
			return ((size == 0) && (elements == null));
		}

	}

	public static Vector<byte[]> parseVector(byte[] bytes) throws NoSuchFieldException, IOException {
		if (bytes == null) {
			return null;
		}
		Vector<byte[]> vector = new Vector<>();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		while ((data = parseBIN(is)) != null) {
			vector.add(data);
		}
		return vector;
	}

	public static int getEQCTypeOverhead(int rawdataLength) {
		int overHead = 0;
		if (rawdataLength <= MAX_BIN8_LEN) {
			overHead = 2;
		} else if (rawdataLength >= MIN_BINX_LEN && rawdataLength <= MAX_BINX_LEN) {
			overHead = 1;
		} else if (rawdataLength <= MAX_BIN16_LEN) {
			overHead = 3;
		} else if (rawdataLength <= MAX_BIN24_LEN) {
			overHead = 4;
		} else if (rawdataLength <= MAX_BIN32_LEN) {
			overHead = 5;
		}
		return overHead;
	}

	public static byte[] intToEQCBits(final int value) {
		return bigIntegerToEQCBits(BigInteger.valueOf(value));
	}

	public static int eqcBitsToInt(final byte[] bytes) {
		return eqcBitsToBigInteger(bytes).intValue();
	}

	public static byte[] longToEQCBits(final long value) {
		return bigIntegerToEQCBits(BigInteger.valueOf(value));
	}

	public static long eqcBitsToLong(final byte[] bytes) {
		return eqcBitsToBigInteger(bytes).longValue();
	}

	/**
	 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
	 * the highest digit of which is a continuous label. If it is 1, it means that
	 * the subsequent byte is still part of bytes. If it is 0, it means the current
	 * byte is the last byte of bytes. The endian is little endian. Due to
	 * ByteArrayOutputStream write byte array in little endian so here reverse the
	 * byte array
	 * <p>
	 * 
	 * @param value the original value of relevant number
	 * @return byte[] the original number's EQCBits
	 */
	public static byte[] bigIntegerToEQCBits(final BigInteger value) {
		EQCType.assertNotNegative(value);
		
		// Get the original binary sequence with the high digits on the left.
		String strFoo = null;
		strFoo = value.toString(2);
		StringBuilder sb = new StringBuilder();
		sb.append(strFoo);
		int len = strFoo.length();
		// Insert a 1 every 7 digits from the low position.
		for (int i = 1; i < len; ++i) {
			if (i % 7 == 0) {
				sb.insert(len - i, '1');
			}
		}
		return Util.reverseBytes(new BigInteger(sb.toString(), 2).toByteArray());
	}

	public static BigInteger eqcBitsToBigInteger(final byte[] bytes) {
		BigInteger foo = new BigInteger(1, Util.reverseBytes(bytes));
		String strFoo = foo.toString(2);
		StringBuilder sb = new StringBuilder().append(strFoo);
		int len = strFoo.length();
		for (int i = 1; i < strFoo.length(); ++i) {
			if (i % 8 == 0) {
				sb.deleteCharAt(len - i);
			}
		}
		return new BigInteger(sb.toString(), 2);
	}
	
	public static ID eqcBitsToID(final byte[] bytes) {
		BigInteger foo = new BigInteger(1, Util.reverseBytes(bytes));
		String strFoo = foo.toString(2);
		StringBuilder sb = new StringBuilder().append(strFoo);
		int len = strFoo.length();
		for (int i = 1; i < strFoo.length(); ++i) {
			if (i % 8 == 0) {
				sb.deleteCharAt(len - i);
			}
		}
		return new ID(new BigInteger(sb.toString(), 2));
	}

	public static byte[] stringToASCIIBytes(String foo) {
		if(foo == null) {
			return NULL_ARRAY;
		}
		return foo.getBytes(StandardCharsets.US_ASCII);
	}

	public static String bytesToASCIISting(byte[] bytes) {
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public static byte[] booleanToEQCBits(boolean isTrue) {
		byte[] bytes = null;
		if(isTrue) {
			bytes = intToEQCBits(1);
		}
		else {
			bytes = intToEQCBits(0);
		}
		return bytes;
	}
	
	public static boolean eqcBitsToBoolean(byte[] bytes) {
		int value = eqcBitsToInt(bytes);
		if(value == 0) {
			return false;
		}
		return true;
	}
	
	public static void assertNoRedundantData(ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(!isInputStreamEnd(is)) {
			throw new IllegalStateException("Exists redundant data in current Object.");
		}
	}
	
	public static void assertNotTerminate(ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(isInputStreamEnd(is)) {
			throw new IllegalStateException("Current Object's input stream unexpected terminated.");
		}
	}
	
	public static void assertNotNull(ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(isNULL(is)) {
			throw new IllegalStateException("The Object shouldn't be null.");
		}
	}
	
	public static void assertNotNull(byte[] bytes) throws IllegalStateException {
		if(isNULL(bytes)) {
			throw new IllegalStateException("The Object shouldn't be null.");
		}
	}
	
	public static void assertNotZero(ID id) throws IllegalStateException {
		Objects.requireNonNull(id);
		if(id.compareTo(ID.ZERO) == 0) {
			throw new IllegalStateException("The ID shouldn't be zero.");
		}
	}
	
	public static void assertNotNegative(ID id) throws IllegalStateException {
		Objects.requireNonNull(id);
		if(id.compareTo(ID.ZERO) < 0) {
			throw new IllegalStateException("The ID shouldn't be negative.");
		}
	}
	
	public static void assertNotNegative(BigInteger bigInteger) throws IllegalStateException {
		Objects.requireNonNull(bigInteger);
		if(bigInteger.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalStateException("The BigInteger shouldn't be negative.");
		}
	}
	
	public static void assertNotNegative(long value) throws IllegalStateException {
		if(value < 0) {
			throw new IllegalStateException("The long Value shouldn't be negative.");
		}
	}
	
	public static void assertNotZero(long value) throws IllegalStateException {
		if(value == 0) {
			throw new IllegalStateException("The value shouldn't be zero.");
		}
	}
	
	public static void assertNotEOF(int type) throws IllegalStateException {
		if(type == EOF) {
			throw new IllegalStateException("The ByteArrayInputStream shouldn't end but read EOF.");
		}
	}

	public static void assertNotBigger(ID amount0, ID amount1) throws IllegalStateException {
		Objects.requireNonNull(amount0);
		Objects.requireNonNull(amount1);
		if(amount0.compareTo(amount1) > 0) {
			throw new IllegalStateException(amount0 + " shouldn't bigger than " + amount1);
		}
	}
	
	public static void assertEqual(long value, long value1) throws IllegalStateException {
		if(value != value1) {
			throw new IllegalStateException("The value " + value + " and " + value1 + " should equal.");
		}
	}
	
	public static void assertEqual(byte[] original, byte[] saved) throws IllegalStateException {
		Objects.requireNonNull(original);
		Objects.requireNonNull(saved);
		if(!Arrays.equals(original, saved)) {
			throw new IllegalStateException("Original data:\n" + Util.dumpBytes(original, 16) + " doesn't equal to saved data:\n" + Util.dumpBytes(saved, 16));
		}
	}
	
}
