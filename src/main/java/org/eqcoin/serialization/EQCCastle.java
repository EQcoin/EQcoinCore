/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation retains all current and future right, title and interest
 * in all of Wandering Earth Corporation’s intellectual property, including, without
 * limitation, inventions, ideas, concepts, code, discoveries, processes, marks,
 * methods, software, compositions, formulae, techniques, information and data,
 * whether or not patentable, copyrightable or protectable in trademark, and
 * any trademarks, copyright or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
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
package org.eqcoin.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

import org.eqcoin.avro.O;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * EQCCastle is an efficient serialization and deserialization language for
 * EQcoin. EQCCastle is based on Object-oriented serialization. The continuous
 * fixed-length bytes or object array of a specific object is serialized and
 * deserialized by the object itself. For example, a T1 type Lock whose
 * signature's r, s length is 32 bytes is obtained directly use parseNBytes(32)
 * according to the properties of the T1 type Lock object.
 * <p>
 * There are 3 categories of EQCCastle:
 * <p>
 * 1. EQCBits
 * <p>
 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
 * the highest digit of which is a continuous label. If it is 0, it means that
 * the subsequent byte is still part of bytes. If it is 1, it means the current
 * byte is the last byte of bytes. The endian is big endian.
 * <p>
 * | 0XXXXXXX | 0XXXXXXX | 0XXXXXXX | ... | 1XXXXXXX |
 * <p>
 * 2. EQCBytes
 * <p>
 * EQCBytes stores a byte array whose length is from 1 to 2^32 - 1.
 * <p>
 * | len | bytes |
 * <p>
 * len is the byte array's length which is EQCBits.
 * <p>
 * bytes is the byte array.
 * <p>
 * 3. EQCArray
 * <p>
 * EQCArray stores a object array whose length is from 1 to 2^32 - 1.
 * <p>
 * | len | array |
 * <p>
 * len is the object array's length which is EQCBits.
 * <p>
 * array is the object array.
 * <p>
 * 4. EQCLight
 * <p>
 * EQCLight is a series of consecutive bytes which length is from 2 to 9 bytes.
 * EQCLight can store positive integers divisible by 1000, so that the lowest 3
 * bits of its lowest byte can be used as status bits. The lowest 3 bits of the
 * lowest byte of the current byte sequence are the status bits used to indicate
 * how many bytes it contains. The endian is big endian. EQC uses EQCLight
 * to store the transfer value in TransferTxOut. For the most efficient use of
 * bytes, the transfer value in TransferTxOut must divisible by 1000.
 * <p>
 * | XXXXXSSS | XXXXXXXX | XXXXXXXX | XXXXXXXX | XXXXXXXX | ... | XXXXXXXX |
 * <p>
 * 5. EQCBitsX
 * <p>
 * EQCBitsX is a series of consecutive bytes consisting of several atomic byte
 * units. An atomic byte unit consists of n bytes, where n is a positive integer.
 * The highest bit of each atomic byte unit is a continuous symbol. If it is 0, it
 * means that there are other atomic units behind the current atomic unit. If
 * it is 1, it means that the end of the current sequence has been reached. The
 * last atomic unit of each byte series follows its continuous symbol is m-bit
 * state symbol, where m^2=n. Limited by space and efficiency, only the cases
 * of m=2, 3, and 4 are implemented here. The endian is big endian.
 * <p>
 * | 0XX...XX | 0XX...XX | 0XX...XX | ... | 1S...SXX...XX |
 * <p>
 * 6. EQCAtom
 * <p>
 * EQCAtom is a series of consecutive bytes which length is from 1 to 2
 * bytes. The lowest 1 bit of the lowest byte of the current byte sequence are
 * the status bits used to indicate how many bytes it contains. If the lowest
 * bit of the lowest byte of the current byte sequence is 0, then its highest bit
 * can be used as status bit, and the position of this byte will be inverted to the
 * position of the lowest byte. On the contrary, a new byte is added whose
 * highest bit is the status bit and the rest of the bits are zero, followed by the
 * byte sequence it includes. The endian is big endian.
 * <p>
 * | XXXXXXXS | ... | XXXXXXXX | or | 0000000S | XXXXXXXX | ... |XXXXXXXX |
 * <p>
 * 7. EQCQuantum
 * <p>
 * EQCQuantum is a series of consecutive bytes which length is from 1 to 4
 * bytes. EQCQuantum can store positive integers divisible by 4, so that the
 * lowest 2 bits of its lowest byte can be used as status bits. The lowest 2 bits
 * of the lowest byte of the current byte sequence are the status bits used to
 * indicate how many bytes it contains. The endian is big endian.
 * <p>
 * | XXXXXXSS | ... | XXXXXXXX |
 * <p>
 * 8. EQCTrinity
 * <p>
 * EQCTrinity is a series of consecutive bytes which length is from 2 to 9
 * bytes. The lowest 3 bits of the lowest byte of the current byte sequence are
 * the status bits used to indicate how many bytes it contains. If the highest
 * 2 bits of the highest byte of the current byte sequence is 0, then its highest
 * 2 bits can be used as status bit, and the position of this byte will be inverted
 * to the position of the lowest byte. On the contrary, a new byte is added whose
 * highest 3 bits is the status bit and the rest of the bits are zero, followed by
 * the byte sequence it includes. The endian is big endian.
 * <p>
 * | SSSXXXXX | ... | XXXXXXXX | or | SSS00000 | XXXXXXXX | ... |XXXXXXXX |
 * <p>
 * 9. EQCHelix
 * <p>
 * EQCHelix is a series of consecutive bytes which length is from 2 to 9 bytes.
 * EQCHelix can store positive integers divisible by 100000, so that the lowest
 * 5 bits of its lowest byte can be used as status bits. EQC uses EQCHelix to
 * store the transfer value and relevant Passport's bytes' length in TransferTxOut.
 * For the most efficient use of bytes, the transfer value in TransferTxOut must
 * divisible by 100000. The 3 bits from the 0th to the second bit of the lowest
 * byte of the current byte sequence are the status bits used to indicate how
 * many bytes it contains and the 2 bits from the 3rd to the 4th bit of the lowest
 * byte of the current byte sequence are the status bits used to indicate how
 * many bytes the relevant Passport contains. The endian is big endian.
 * <p>
 * | XXXQQSSS | XXXXXXXX | XXXXXXXX | XXXXXXXX | XXXXXXXX | ... | XXXXXXXX |
 * <p>
 *
 * @author Xun Wang
 * @date 9-21-2018
 * @email 10509759@qq.com
 */
public class EQCCastle {

	/**
	 * BINX stores a byte array whose length is from 1 to 247 bytes.
	 * <p>
	 * | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | is a 8-bit unsigned integer which represents the length of data.
	 * <p>
	 * <p>
	 * ArrayX stores a byte array including xxx elements whose length from 1 to 247
	 * bytes.
	 * <p>
	 * | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | is a 8-bit unsigned integer which represents the length of
	 * elements.
	 */
	public final static long MIN_BINX_LEN = 1;
	public final static long MAX_BINX_LEN = 247;
	public final static int EOF = -1;

	/**
	 * For EQCBytes or EQCArray in case the Object which represents is null just
	 * save a NULL(0) in relevant position.
	 */
	public final static byte NULL = 0;

	public final static byte[] NULL_ARRAY = {NULL};

	public final static int MAX_BIN_LEN = Integer.MAX_VALUE;

	public final static int MAX_ARRAY_LEN = Integer.MAX_VALUE;

	/**
	 * BIN8 stores a byte array whose length is from 1 to 8 bytes.
	 * <p>
	 * | 0xF8 | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | is a 8-bit unsigned integer which represents the length of data.
	 */
	public final static int BIN8 = 0xF8;
	/*
	 * Due to Java only have signed int so here use long represent unsigned int.
	 */
	public final static long MIN_BIN8_LEN = 248;
	public final static long MAX_BIN8_LEN = 255;

	/**
	 * BIN16 stores a byte array whose length is from 2^8 to 2^16 - 1 bytes.
	 * <p>
	 * | 0xF9 | XXXXXXXX | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | XXXXXXXX | is a 16-bit unsigned integer which represents the
	 * length of data.
	 */
	public final static int BIN16 = 0xF9;
	public final static long MIN_BIN16_LEN = (int) (Math.pow(2, 8));
	public final static long MAX_BIN16_LEN = (int) (Math.pow(2, 16) - 1);

	/**
	 * BIN24 stores a byte array whose length is from 2^16 to 2^24 - 1 bytes.
	 * <p>
	 * | 0xFA | XXXXXXXX | XXXXXXXX | XXXXXXXX| data |
	 * <p>
	 * | XXXXXXXX | XXXXXXXX | XXXXXXXX | is a 24-bit unsigned integer which
	 * represents the length of data.
	 */
	public final static int BIN24 = 0xFA;
	public final static long MIN_BIN24_LEN = (int) (Math.pow(2, 16));
	public final static long MAX_BIN24_LEN = (int) (Math.pow(2, 24) - 1);

	/**
	 * BIN32 stores a byte array whose length is from 2^24 to 2^32 - 1 bytes.
	 * <P>
	 * | 0xFB | XXXXXXXX | XXXXXXXX | XXXXXXXX | data |
	 * <P>
	 * | XXXXXXXX| XXXXXXXX| XXXXXXXX | XXXXXXXX | is a 32-bit unsigned integer
	 * which represents the length of data.
	 */
	public final static int BIN32 = 0xFB;
	public final static long MIN_BIN32_LEN = (int) (Math.pow(2, 24));
	public final static long MAX_BIN32_LEN = (int) (Math.pow(2, 32) - 1);

	/**
	 * Array8 stores a byte array including xxx elements whose length is from 1 to 8
	 * bytes.
	 * <p>
	 * | 0xFC | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | is a 8-bit unsigned integer which represents the length of array's element.
	 */
	public final static int ARRAY8 = 0xFC;
	public final static long MIN_ARRAY8_LEN = MIN_BIN8_LEN;
	public final static long MAX_ARRAY8_LEN = MAX_BIN8_LEN;

	/**
	 * Array16 stores a byte array including xxx elements whose length is from 2^8
	 * to 2^16 - 1 bytes.
	 * <p>
	 * | 0xFD | XXXXXXXX | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | XXXXXXXX | is a 16-bit unsigned integer which represents the
	 * length of array's element.
	 */
	public final static int ARRAY16 = 0xFD;
	public final static long MIN_ARRAY16_LEN = MIN_BIN16_LEN;
	public final static long MAX_ARRAY16_LEN = MAX_BIN16_LEN;

	/**
	 * Array24 stores a byte array including xxx elements whose length is from 2^16
	 * to 2^24 - 1 bytes.
	 * <p>
	 * | 0xFE | EQCBits | XXXXXXXX | XXXXXXXX | XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX | XXXXXXXX | XXXXXXXX | is a 24-bit unsigned integer which
	 * represents the length of array's element.
	 */
	public final static int ARRAY24 = 0xFE;
	public final static long MIN_ARRAY24_LEN = MIN_BIN24_LEN;
	public final static long MAX_ARRAY24_LEN = MAX_BIN24_LEN;

	/**
	 * Array32 stores a byte array including xxx elements whose length is from 2^24
	 * to 2^32 - 1 bytes.
	 * <p>
	 * | 0xFF | EQCBits | XXXXXXXX | XXXXXXXX| XXXXXXXX| XXXXXXXX | data |
	 * <p>
	 * | XXXXXXXX| XXXXXXXX| XXXXXXXX | XXXXXXXX | is a 32-bit unsigned integer
	 * which represents the length of array's element.
	 */
	public final static int ARRAY32 = 0xFF;
	public final static long MIN_ARRAY32_LEN = MIN_BIN32_LEN;
	public final static long MAX_ARRAY32_LEN = MAX_BIN32_LEN;

	/**
	 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
	 * the highest digit of which is a continuous label. If it is 1, it means that
	 * the subsequent byte is still part of bytes. If it is 0, it means the current
	 * byte is the last byte of bytes. The endian is little endian.
	 * <p>
	 * | 1XXXXXXX | ... | 1XXXXXXX | 0XXXXXXX |
	 */
	public final static int EQCBITS = 0x80;
	public final static int EQCBITS_MASK = 0x7F;
	public final static byte EQCBITS_BUFFER_LEN = 9;
	public final static BigInteger BASE128 = BigInteger.valueOf(0x80);

	/**
	 * EQCLight
	 * <p>
	 * EQCLight is a series of consecutive bytes which length is from 2 to 9 bytes.
	 * EQCLight can store positive integers divisible by 1000, so that the lowest 3
	 * bits of its lowest byte can be used as status bits. The lowest 3 bits of the
	 * lowest byte of the current byte sequence are the status bits used to indicate
	 * how many bytes it contains. The endian is big endian. EQC uses EQCLight
	 * to store the transfer value in TransferTxOut. For the most efficient use of
	 * bytes, the transfer value in TransferTxOut must divisible by 1000.
	 * <p>
	 * | XXXXXSSS | XXXXXXXX | XXXXXXXX | XXXXXXXX | XXXXXXXX | ... | XXXXXXXX |
	 * <p>
	 */
	public final static int EQCLIGHT_MASK = 0xF8;

	public final static byte EQCLIGHT_MIN_LEN = 2;

	public final static byte EQCLIGHT_MAX_LEN = 9;

	public final static BigInteger EQCLIGHT_MIN_VALUE = BigInteger.valueOf(1000);

	/**
	 * EQCQuantum
	 * <p>
	 * EQCQuantum is a series of consecutive bytes which length is from 1 to 4
	 * bytes. EQCQuantum can store positive integers divisible by 4, so that the
	 * lowest 2 bits of its lowest byte can be used as status bits. The lowest 2 bits
	 * of the lowest byte of the current byte sequence are the status bits used to
	 * indicate how many bytes it contains. The endian is big endian.
	 * <p>
	 * | XXXXXXSS | ... | XXXXXXXX |
	 * <p>
	 */
	public final static int EQCQUANTUM_MASK = 0xFC;

	public final static byte EQCQUANTUM_MIN_LEN = 1;

	public final static byte EQCQUANTUM_MAX_LEN = 4;

	public final static BigInteger EQCQUANTUM_MIN_VALUE = BigInteger.valueOf(4);

	public final static int EQCHELIX_PASSPORT_LEN_MASK = 0xE7;

	public final static int EQCHELIX_VALUE_MASK = 0xE7;

	public final static NoSuchFieldException ZERO_EXCEPTION = new NoSuchFieldException("The ID shouldn't be zero");

	public final static NoSuchFieldException NULL_OBJECT_EXCEPTION = new NoSuchFieldException("The Object shouldn't be null");

	public final static IllegalStateException REDUNDANT_DATA_EXCEPTION = new IllegalStateException("Exists redundant data in current Object");

	public final static IllegalStateException ARRAY_LENGTH_EXCEPTION = new IllegalStateException("ARRAY's length doesn't equal to it's actual length");

	public final static IllegalStateException EOF_EXCEPTION = new IllegalStateException("The ByteArrayInputStream shouldn't end but read EOF");

	public final static void assertEqual(final byte[] original, final byte[] saved) throws IllegalStateException {
		Objects.requireNonNull(original);
		Objects.requireNonNull(saved);
		if(!Arrays.equals(original, saved)) {
			throw new IllegalStateException("Original data:\n" + Util.dumpBytes(original, 16) + " doesn't equal to saved data:\n" + Util.dumpBytes(saved, 16));
		}
	}

	public final static void assertEqual(final ID value, final ID value1) throws IllegalStateException {
		Objects.requireNonNull(value);
		Objects.requireNonNull(value1);
		if(!value.equals(value1)) {
			throw new IllegalStateException("The value " + value + " and " + value1 + " should equal");
		}
	}

	public final static void assertEqual(final int value, final int value1) throws IllegalStateException {
		if(value != value1) {
			throw new IllegalStateException("The value " + value + " and " + value1 + " should equal");
		}
	}

	public final static void assertEqual(final long value, final long value1) throws IllegalStateException {
		if(value != value1) {
			throw new IllegalStateException("The value " + value + " and " + value1 + " should equal");
		}
	}

	public final static void assertNoRedundantData(final ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(!isInputStreamEnd(is)) {
			throw new IllegalStateException("Exists redundant data in current Object");
		}
	}

	public final static void assertNotBigger(final BigInteger amount0, final BigInteger amount1) throws IllegalStateException {
		Objects.requireNonNull(amount0);
		Objects.requireNonNull(amount1);
		if(amount0.compareTo(amount1) > 0) {
			throw new IllegalStateException(amount0 + " shouldn't bigger than " + amount1);
		}
	}

	public final static void assertNotBigger(final int amount0, final int amount1) throws IllegalStateException {
		if(amount0 > amount1) {
			throw new IllegalStateException(amount0 + " shouldn't bigger than " + amount1);
		}
	}

	public final static void assertNotEOF(final int type) throws IllegalStateException {
		if(type == EOF) {
			throw new IllegalStateException("The ByteArrayInputStream shouldn't end but read EOF");
		}
	}

	public final static void assertNotLess(final BigInteger amount0, final BigInteger amount1) throws IllegalStateException {
		Objects.requireNonNull(amount0);
		Objects.requireNonNull(amount1);
		if(amount0.compareTo(amount1) < 0) {
			throw new IllegalStateException(amount0 + " shouldn't less than " + amount1);
		}
	}

	public final static void assertNotLess(final int amount0, final int amount1) throws IllegalStateException {
		if(amount0 < amount1) {
			throw new IllegalStateException(amount0 + " shouldn't less than " + amount1);
		}
	}

	public final static void assertNotNegative(final BigInteger bigInteger) throws IllegalStateException {
		Objects.requireNonNull(bigInteger);
		if(bigInteger.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalStateException("The BigInteger shouldn't be negative");
		}
	}

	public final static void assertNotNegative(final ID id) throws IllegalStateException {
		Objects.requireNonNull(id);
		if(id.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalStateException("The ID shouldn't be negative");
		}
	}

	public final static void assertNotNegative(final long value) throws IllegalStateException {
		if(value < 0) {
			throw new IllegalStateException("The long Value shouldn't be negative");
		}
	}

	public final static void assertNotNull(final byte[] bytes) throws IllegalStateException {
		if(isNULL(bytes)) {
			throw new IllegalStateException("The Object shouldn't be null");
		}
	}

	public final static void assertNotNull(final ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(isNULL(is)) {
			throw new IllegalStateException("The Object shouldn't be null");
		}
	}

	public final static void assertNotTerminate(final ByteArrayInputStream is) throws IllegalStateException {
		Objects.requireNonNull(is);
		if(isInputStreamEnd(is)) {
			throw new IllegalStateException("Current Object's input stream unexpected terminated");
		}
	}

	public final static void assertNotZero(final ID id) throws IllegalStateException {
		Objects.requireNonNull(id);
		if(id.compareTo(BigInteger.ZERO) == 0) {
			throw new IllegalStateException("The ID shouldn't be zero");
		}
	}

	public final static void assertNotZero(final long value) throws IllegalStateException {
		if(value == 0) {
			throw new IllegalStateException("The value shouldn't be zero");
		}
	}

	public final static void assertPositive(final BigInteger bigInteger) throws IllegalStateException {
		Objects.requireNonNull(bigInteger);
		if(bigInteger.compareTo(BigInteger.ZERO) <= 0) {
			throw new IllegalStateException("The BigInteger shouldn't be negative or zero");
		}
	}

	public final static void assertPositive(final long value) throws IllegalStateException {
		if(value <= 0) {
			throw new IllegalStateException("The long Value shouldn't be negative or zero");
		}
	}

	/**
	 * EQCBits is a series of consecutive bytes. Each byte has 7 significant digits,
	 * the highest digit of which is a continuous label. If it is 0, it means that
	 * the subsequent byte is still part of bytes. If it is 1, it means the current
	 * byte is the last byte of bytes. The endian is big endian.
	 * <p>
	 *
	 * @param value the original value of relevant number
	 * @return byte[] the original number's EQCBits
	 */
	public static byte[] bigIntegerToEQCBits(BigInteger value) {
		EQCCastle.assertNotNegative(value);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		BigInteger remainder = null;
		byte[] bytes = null;
		while (value.compareTo(BASE128) >= 0) {
			remainder = value.mod(BASE128);
			bytes = remainder.toByteArray();
			os.write(bytes.length == 1?bytes[0]:bytes[1]);
			value = value.subtract(remainder).divide(BASE128);
		}
		bytes = value.toByteArray();
		os.write((bytes.length == 1?bytes[0]:bytes[1]) | EQCBITS);
		return os.toByteArray();
	}

	public final static byte[] booleanToEQCBits(final boolean isTrue) {
		byte[] bytes = null;
		if(isTrue) {
			bytes = intToEQCBits(1);
		}
		else {
			bytes = intToEQCBits(0);
		}
		return bytes;
	}

	public final static String bytesToASCIISting(final byte[] bytes) {
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	public final static byte[] bytesToBIN(final byte[] bytes) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Stores a NULL. placeholder for parsing data when there is no corresponding
			// data item.
			if (bytes == null) {
				os.write(NULL);
			} else if (bytes.length <= MAX_BINX_LEN) {
				os.write(bytesToBINX(bytes));
			} else if (bytes.length <= MAX_BIN8_LEN) {
				os.write(BIN8);
				os.write(Util.longToBytes(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BIN16_LEN) {
				os.write(BIN16);
				os.write(Util.longToBytes(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BIN24_LEN) {
				os.write(BIN24);
				os.write(Util.longToBytes(bytes.length));
				os.write(bytes);
			} else if (bytes.length <= MAX_BIN32_LEN) {
				os.write(BIN32);
				os.write(Util.longToBytes(bytes.length));
				os.write(bytes);
			}
		} catch (final IOException e) {
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	private final static byte[] bytesToBINX(final byte[] bytes) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (bytes == null) {
				os.write(NULL);
			} else {
				if (bytes.length < MIN_BINX_LEN || bytes.length > MAX_BINX_LEN) {
					throw new IllegalStateException(
							"Byte array's length shouldn't less than 1 or exceed 247. Len: " + bytes.length);
				}
				//				os.write((byte) (BIN7 | (bytes.length & 0xFF)));
				os.write(bytes.length & 0xFF);
				//				Log.info("bytesToBIN7's len: " +((byte) (BIN7 | (bytes.length & 0xFF))) );
				//				Log.info(Util.dumpBytesLittleEndianBinary(new byte[] { (byte) (BIN7 | (bytes.length & 0xFF)) }));
				os.write(bytes);
				//				Log.info(Util.dumpBytesLittleEndianBinary(bytes));
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public static BigInteger eqcBitsToBigInteger(final byte[] bytes) {
		BigInteger foo = BigInteger.ZERO;
		for(int i=0; i<bytes.length-1; ++i) {
			foo = foo.add(BigInteger.valueOf(bytes[i]).multiply(BASE128.pow(i)));
		}
		foo = foo.add(BigInteger.valueOf(bytes[bytes.length - 1] & EQCBITS_MASK).multiply(BASE128.pow(bytes.length - 1)));
		return foo;
	}

	public final static boolean eqcBitsToBoolean(final byte[] bytes) {
		final int value = eqcBitsToInt(bytes);
		if(value == 0) {
			return false;
		}
		return true;
	}

	public final static ID eqcBitsToID(final byte[] bytes) {
		return new ID(eqcBitsToBigInteger(bytes));
	}

	public final static int eqcBitsToInt(final byte[] bytes) {
		return eqcBitsToBigInteger(bytes).intValue();
	}

	public final static long eqcBitsToLong(final byte[] bytes) {
		return eqcBitsToBigInteger(bytes).longValue();
	}

	public final static <T extends EQCObject> byte[] eqcSerializableListToArray(final Vector<T> eqcSerializableList)
			throws Exception {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] bytes = null;
		// Stores a NULL placeholder for parsing data when there is corresponding Object
		// is null.
		if ((eqcSerializableList == null) || eqcSerializableList.size() == 0) {
			os.write(NULL);
		} else {
			bytes = eqcSerializableListToBytes(eqcSerializableList);
			if (eqcSerializableList.size() <= MAX_BINX_LEN) {
				os.write(eqcSerializableList.size());
				os.write(bytes);
			} else if (eqcSerializableList.size() <= MAX_BIN8_LEN) {
				os.write(ARRAY8);
				os.write(Util.longToBytes(eqcSerializableList.size()));
				os.write(bytes);
			} else if (eqcSerializableList.size() <= MAX_BIN16_LEN) {
				os.write(ARRAY16);
				os.write(Util.longToBytes(eqcSerializableList.size()));
				os.write(bytes);
			} else if (eqcSerializableList.size() <= MAX_BIN24_LEN) {
				os.write(ARRAY24);
				os.write(Util.longToBytes(eqcSerializableList.size()));
				os.write(bytes);
			} else if (eqcSerializableList.size() <= MAX_BIN32_LEN) {
				os.write(ARRAY32);
				os.write(Util.longToBytes(eqcSerializableList.size()));
				os.write(bytes);
			}
		}
		return os.toByteArray();
	}

	//	public static byte[] bigIntegerToEQCBits(final BigInteger value) {
	//		byte[] bytes = null;
	//		EQCType.assertNotNegative(value);
	//		// Get the original binary sequence with the high digits on the left.
	//		String strFoo = null;
	//		strFoo = value.toString(2);
	//		StringBuilder sb = new StringBuilder();
	//		sb.append(strFoo);
	//		int len = strFoo.length();
	//		// Insert a 1 every 7 digits from the low position.
	//		for (int i = 1; i < len; ++i) {
	//			if (i % 7 == 0) {
	//				sb.insert(len - i, '1');
	//			}
	//		}
	//		bytes = new BigInteger(sb.toString(), 2).toByteArray();
	//		if(bytes.length > 1 && bytes[0] == 0) {
	//			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
	//		}
	//		return Util.reverseBytes(bytes);
	//	}

	private final static <T extends EQCObject> byte[] eqcSerializableListToBytes(final Vector<T> eqcSerializableList)
			throws Exception {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		if (eqcSerializableList == null || eqcSerializableList.size() == 0) {
			os.write(NULL);
		} else {
			for (final T eqcSerializable : eqcSerializableList) {
				try {
					os.write(eqcSerializable.getBytes());
				} catch (final IOException e) {
					Log.Error(e.getMessage());
				}
			}
		}
		return os.toByteArray();
	}

	//	public static BigInteger eqcBitsToBigInteger(final byte[] bytes) {
	//		BigInteger foo = new BigInteger(1, Util.reverseBytes(bytes));
	//		String strFoo = foo.toString(2);
	//		StringBuilder sb = new StringBuilder().append(strFoo);
	//		int len = strFoo.length();
	//		for (int i = 1; i < strFoo.length(); ++i) {
	//			if (i % 8 == 0) {
	//				sb.deleteCharAt(len - i);
	//			}
	//		}
	//		return new BigInteger(sb.toString(), 2);
	//	}

	public final static int getArrayTypeLen(final int type) {
		int len = 0;
		if (type == ARRAY16) {
			len = 2;
		} else if (type == ARRAY24) {
			len = 3;
		} else if (type == ARRAY32) {
			len = 4;
		} else if (type == ARRAY8) {
			len = 1;
		}
		return len;
	}

	public final static int getBINTypeLen(final int type) {
		int len = 0;
		if (type == BIN16) {
			len = 2;
		} else if (type == BIN24) {
			len = 3;
		} else if (type == BIN32) {
			len = 4;
		} else if (type == BIN8) {
			len = 1;
		}
		return len;
	}

	public final static int getEQCTypeOverhead(final int rawdataLength) {
		int overHead = 0;
		if (rawdataLength <= MAX_BINX_LEN) {
			overHead = 1;
		} else if (rawdataLength <= MAX_BIN8_LEN) {
			overHead = 2;
		} else if (rawdataLength <= MAX_BIN16_LEN) {
			overHead = 3;
		} else if (rawdataLength <= MAX_BIN24_LEN) {
			overHead = 4;
		} else if (rawdataLength <= MAX_BIN32_LEN) {
			overHead = 5;
		}
		return overHead;
	}

	public final static byte[] intToEQCBits(final int value) {
		return bigIntegerToEQCBits(BigInteger.valueOf(value));
	}

	public final static boolean isArray(final int type) {
		return (type == ARRAY16) || (type == ARRAY8) || (type == ARRAY24) || (type == ARRAY32) || isBINX(type);
	}

	public final static boolean isBIN(final int type) {
		return isBINX(type) || ((type == BIN16) || (type == BIN24) || (type == BIN32) || (type == BIN8));
	}

	public final static boolean isBINX(final byte[] bytes) {
		return (bytes.length == 1) && (bytes[0] >= MIN_BINX_LEN) && (bytes[0] <= MAX_BINX_LEN);
	}

	public final static boolean isBINX(final int type) {
		return type >= MIN_BINX_LEN && type <= MAX_BINX_LEN;
	}

	public static final	boolean isElementLenValid(final int type, final long elementLen) {
		if(type >= MIN_BINX_LEN && type <= MAX_BINX_LEN) {
			if(elementLen >= MIN_BINX_LEN && elementLen <= MAX_BINX_LEN) {
				return true;
			}
		}
		else if(type == ARRAY8 || type == BIN8) {
			if((elementLen >= MIN_BIN8_LEN) && (elementLen <= MAX_BIN8_LEN)) {
				return true;
			}
		}
		else if(type == ARRAY16 || type == BIN16) {
			if((elementLen >= MIN_BIN16_LEN) && (elementLen <= MAX_BIN16_LEN)) {
				return true;
			}
		}
		else if(type == ARRAY24 || type == BIN24) {
			if((elementLen >= MIN_BIN24_LEN) && (elementLen <= MAX_BIN24_LEN)) {
				return true;
			}
		}
		else if(type == ARRAY32 || type == BIN32) {
			if((elementLen >= MIN_BIN32_LEN) && (elementLen <= MAX_BIN32_LEN)) {
				return true;
			}
		}
		else {
			throw new IllegalStateException("Invalid element length: " + elementLen);
		}
		return false;
	}

	public final static boolean isEQCBits(final int type) {
		return (((byte) type & EQCBITS) == EQCBITS);
	}

	public final static boolean isInputStreamEnd(final ByteArrayInputStream is) {
		return is.available() == 0;
	}

	/**
	 * In EQCType serialization when BIN or Array EQCType represent's Object is null
	 * will save a NULL
	 *
	 * @param bytes
	 * @return boolean If current Object is null
	 */
	public final static boolean isNULL(final byte[] bytes) {
		return (bytes != null) && (bytes.length == 1) && (bytes[0] == NULL);
	}

	public final static boolean isNULL(final ByteArrayInputStream is) {
		boolean boolisNULL = false;
		int type;
		final byte[] data = null;
		final byte[] bytes = null;
		final int iLen = 0;
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
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}
		return boolisNULL;
	}

	public final static boolean isNULL(final int type) {
		return type == NULL;
	}

	public final static byte[] longToEQCBits(final long value) {
		return bigIntegerToEQCBits(BigInteger.valueOf(value));
	}

	public final static <T extends EQCObject> Vector<T> parseArray(final ByteArrayInputStream is, final T eqcSerializable) throws Exception {
		int type;
		byte[] data = null;
		/**
		 * Due to the unsigned integer in java doesn't support very good and
		 * Vector&Array's size in java is integer type. But in EQCType Array's size type
		 * is unsigned integer So here use long to represent unsigned integer.
		 */
		long elementLen = 0;
		int iLen = 0;
		final Vector<T> array = new Vector<>(); // Here exists one bug the max size of vector is 2^31-1

		// Parse type
		type = is.read();
		if(isNULL(type)) {
		} else if (isBINX(type)) {
			// Get element's length
			elementLen = type;
			for(long i=0; i<elementLen; ++i) {
				array.add(eqcSerializable.Parse(is));
			}
		} else if (isArray(type)) {
			// Get Array element's length
			iLen = getArrayTypeLen(type);
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseArray Get BIN type len error occur record len != real len");
			}
			// Get Array element's length
			elementLen = Util.bytesToLong(data);
			// Check if elementLen is valid
			if(!isElementLenValid(type, elementLen)) {
				throw new IllegalStateException("Array's element length is invalid type: " + type + " element length: " + elementLen);
			}
			// Read the content
			for(long i=0; i<elementLen; ++i) {
				array.add(eqcSerializable.Parse(is));
			}
		} else {
			throw new IllegalStateException("Unexpected array type: " + type);
		}
		return array;
	}

	/**
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws IllegalStateException
	 */
	/**
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws IllegalStateException
	 */
	public final static byte[] parseBIN(final ByteArrayInputStream is) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] data = null;
		byte[] bytes = null;
		final byte[] len = null;
		int iLen = 0;
		long elementLen = 0;

		// Parse type
		type = is.read();
		//		Log.info(Util.dumpBytesBigEndianBinary(new byte[] {(byte) type}));
		if (isNULL(type)) {
			bytes = NULL_ARRAY;
		} else if (isBINX(type)) {
			//					Log.info("Bin7 len: " + type);
			data = new byte[type];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBINX Get BIN data's len  error occur record len != real len");
			}
			bytes = data;
		} else if (isBIN(type)) {
			// Get BIN type len
			iLen = getBINTypeLen(type);
			data = new byte[iLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBIN Get BIN type len error occur record len != real len");
			}
			// Get BIN data's length
			elementLen = Util.bytesToLong(data);
			// Check if Bin data's length is valid
			if(!isElementLenValid(type, elementLen)) {
				throw new IllegalStateException("Bin data's length is invalid");
			}
			// Read the content
			// Due to the Java array size's limit to Integer.MAX_VALUE so here exists
			// problem when Bin's length exceed the max value but maybe Oracle will fix this
			// soon
			data = new byte[(int) elementLen];
			iLen = is.read(data);
			if (iLen != data.length) {
				throw new NoSuchFieldException("parseBIN Get BIN data's len error occur record len != real len");
			}
			bytes = data;
		} else {
			throw new IllegalStateException("Unexpected EQCType expected BIN type but the type code is: " + type);
		}
		return bytes;
	}

	public final static byte[] parseEQCBits(final ByteArrayInputStream is) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] bytes = null;

		//		// Parse EQCBits
		//		ByteBuffer buff = ByteBuffer.allocate(EQCBITS_BUFFER_LEN);
		//		while ((((type = is.read()) != EOF) && ((byte) type & EQCBITS) != 0)) {
		//			if(buff.remaining() == 0) {
		//				throw new IllegalStateException("The EQCBits' length is exceed the max length " + EQCBITS_BUFFER_LEN + " bytes");
		//			}
		//			buff.put((byte) type);
		//		}
		//		if (type != EOF) {
		//			buff.put((byte) type);
		//			bytes = Arrays.copyOfRange(buff.array(), 0, buff.position());
		//		}
		//		else {
		//			throw EOF_EXCEPTION;
		//		}
		//		if(bytes.length > 1) {
		//			// 20200530 due to new implement method here maybe exists bug need do more job
		//			if((bytes[0] == 128) && (bytes[1] < 192) ) {
		//				throw new IllegalStateException("Bad EQCBits format the highest byte can't be zero");
		//			}
		//		}
		// Parse EQCBits
		final ByteBuffer buff = ByteBuffer.allocate(EQCBITS_BUFFER_LEN);
		while (((type = is.read()) != EOF) && (type < EQCBITS)) {
			if (buff.remaining() == 0) {
				throw new IllegalStateException(
						"The EQCBits' length is exceed the max length " + EQCBITS_BUFFER_LEN + " bytes");
			}
			buff.put((byte) type);
		}
		if (type != EOF) {
			buff.put((byte) type);
			bytes = Arrays.copyOfRange(buff.array(), 0, buff.position());
		} else {
			throw EOF_EXCEPTION;
		}
		//		if (bytes.length > 1) {
		//			// 20200530 due to new implement method here maybe exists bug need do more job
		//			if ((bytes[0] == 128) && (bytes[1] < 192)) {
		//				throw new IllegalStateException("Bad EQCBits format the highest byte can't be zero");
		//			}
		//		}
		return bytes;
	}

	public final static ID parseID(final ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		return new ID(parseEQCBits(is));
	}

	public final static <T> ID parseID(final T type) throws Exception {
		Objects.requireNonNull(type);
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return parseID(is);
	}

	public final static byte[] parseNBytes(final ByteArrayInputStream is, final int len) throws Exception {
		final byte[] bytes = new byte[len];
		int nLen = 0;
		nLen = is.read(bytes);
		if(nLen != len) {
			throw new IllegalStateException("Expected read " + len + " bytes but actually read " + nLen + " bytes");
		}
		return bytes;
	}

	public final static String parseString(final ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		return EQCCastle.bytesToASCIISting(EQCCastle.parseBIN(is));
	}

	public final static Value parseValue(final ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		return new Value(parseEQCBits(is));
	}

	public final static <T> Value parseValue(final T type) throws Exception {
		Objects.requireNonNull(type);
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return parseValue(is);
	}

	public final static byte[] stringToASCIIBytes(final String foo) {
		if(foo == null) {
			return NULL_ARRAY;
		}
		return foo.getBytes(StandardCharsets.US_ASCII);
	}

	/**
	 * Convert String to BIN using StandardCharsets.US_ASCII charset
	 *
	 * @param foo The String which will be convert to BIN
	 * @return
	 */
	public final static byte[] stringToBIN(final String foo) {
		if(foo == null) {
			return NULL_ARRAY;
		}
		return bytesToBIN(foo.getBytes());
	}

	/**
	 * EQCLight
	 * <p>
	 * EQCLight is a series of consecutive bytes which length is from 2 to 9 bytes.
	 * EQCLight can store positive integers divisible by 1000, so that the lowest 3
	 * bits of its lowest byte can be used as status bits. The lowest 3 bits of the
	 * lowest byte of the current byte sequence are the status bits used to indicate
	 * how many bytes it contains. The endian is big endian. EQC uses EQCLight
	 * to store the transfer value in TransferTxOut. For the most efficient use of
	 * bytes, the transfer value in TransferTxOut must divisible by 1000.
	 * <p>
	 * | XXXXXSSS | XXXXXXXX | XXXXXXXX | XXXXXXXX | XXXXXXXX | ... | XXXXXXXX |
	 * <p>
	 *
	 * @param value the original value of relevant number
	 * @return byte[] the original number's EQCBits
	 */
	public static byte[] bigIntegerToEQCLight(BigInteger value) {
		EQCCastle.assertNotNegative(value);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		BigInteger remainder = null;
		byte[] bytes = null;
		EQCCastle.assertNotLess(bytes.length, EQCLIGHT_MIN_LEN);
		EQCCastle.assertNotBigger(bytes.length, EQCLIGHT_MAX_LEN);
		if(!value.mod(EQCLIGHT_MIN_VALUE).equals(BigInteger.ZERO)){
			throw new IllegalStateException("For the most efficient use of bytes, the remainder of the transfer value in TransferTxOut divided by 1000 must be equal to 0.");
		}
		bytes = value.toByteArray();
		bytes[bytes.length - 1]&=(bytes.length - EQCLIGHT_MIN_LEN);
		return bytes;
	}

	public static BigInteger eqcLightToBigInteger(final byte[] bytes) {
		BigInteger foo = null;
		bytes[bytes.length - 1] &= EQCLIGHT_MASK;
		foo = new BigInteger(bytes);
		return foo;
	}

	public final static byte[] parseEQCLight(final ByteArrayInputStream is) throws Exception {
		int type;
		byte[] bytes = null;

		// Parse EQCLight
		is.mark(0);
		type = is.read();
		if (type != EOF) {
			is.reset();
			int n = (type & ~EQCLIGHT_MASK) + EQCLIGHT_MIN_LEN;
			bytes = parseNBytes(is, n);
		} else {
			throw EOF_EXCEPTION;
		}
		return bytes;
	}

	/**
	 * EQCBitsX is a series of consecutive bytes consisting of several atomic byte
	 * units. An atomic byte unit consists of n bytes, where n is a positive integer.
	 * The highest bit of each atomic byte unit is a continuous symbol. If it is 0, it
	 * means that there are other atomic units behind the current atomic unit. If
	 * it is 1, it means that the end of the current sequence has been reached. The
	 * last atomic unit of each byte series follows its continuous symbol is m-bit
	 * state symbol, where m^2=n. Limited by space and efficiency, only the cases
	 * of m=2, 3, and 4 are implemented here. The endian is big endian.
	 * <p>
	 *
	 * @param value the original value of relevant number
	 * @param statusSize the status' size which value in current phase including 2, 3, 4
	 * @return byte[] the original number's EQCBits
	 */
	public static byte[] bigIntegerToEQCBitsX(BigInteger value, final int statusSize) {
		EQCCastle.assertNotNegative(value);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		BigInteger remainder = null;
		byte[] bytes = null;
		while (value.compareTo(BASE128) >= 0) {
			remainder = value.mod(BASE128);
			bytes = remainder.toByteArray();
			os.write(bytes.length == 1?bytes[0]:bytes[1]);
			value = value.subtract(remainder).divide(BASE128);
		}
		bytes = value.toByteArray();
		os.write((bytes.length == 1?bytes[0]:bytes[1]) | EQCBITS);
		return os.toByteArray();
	}
	public static BigInteger eqcBitsXToBigInteger(final byte[] bytes, final int statusSize) {
		BigInteger foo = BigInteger.ZERO;
		for(int i=0; i<bytes.length-1; ++i) {
			foo = foo.add(BigInteger.valueOf(bytes[i]).multiply(BASE128.pow(i)));
		}
		foo = foo.add(BigInteger.valueOf(bytes[bytes.length - 1] & EQCBITS_MASK).multiply(BASE128.pow(bytes.length - 1)));
		return foo;
	}

	public final static byte[] parseEQCBitsX(final ByteArrayInputStream is, final int statusSize) throws IOException, NoSuchFieldException, IllegalStateException {
		int type;
		byte[] bytes = null;

		//		// Parse EQCBits
		//		ByteBuffer buff = ByteBuffer.allocate(EQCBITS_BUFFER_LEN);
		//		while ((((type = is.read()) != EOF) && ((byte) type & EQCBITS) != 0)) {
		//			if(buff.remaining() == 0) {
		//				throw new IllegalStateException("The EQCBits' length is exceed the max length " + EQCBITS_BUFFER_LEN + " bytes");
		//			}
		//			buff.put((byte) type);
		//		}
		//		if (type != EOF) {
		//			buff.put((byte) type);
		//			bytes = Arrays.copyOfRange(buff.array(), 0, buff.position());
		//		}
		//		else {
		//			throw EOF_EXCEPTION;
		//		}
		//		if(bytes.length > 1) {
		//			// 20200530 due to new implement method here maybe exists bug need do more job
		//			if((bytes[0] == 128) && (bytes[1] < 192) ) {
		//				throw new IllegalStateException("Bad EQCBits format the highest byte can't be zero");
		//			}
		//		}
		// Parse EQCBits
		final ByteBuffer buff = ByteBuffer.allocate(EQCBITS_BUFFER_LEN);
		while (((type = is.read()) != EOF) && (type < EQCBITS)) {
			if (buff.remaining() == 0) {
				throw new IllegalStateException(
						"The EQCBits' length is exceed the max length " + EQCBITS_BUFFER_LEN + " bytes");
			}
			buff.put((byte) type);
		}
		if (type != EOF) {
			buff.put((byte) type);
			bytes = Arrays.copyOfRange(buff.array(), 0, buff.position());
		} else {
			throw EOF_EXCEPTION;
		}
		//		if (bytes.length > 1) {
		//			// 20200530 due to new implement method here maybe exists bug need do more job
		//			if ((bytes[0] == 128) && (bytes[1] < 192)) {
		//				throw new IllegalStateException("Bad EQCBits format the highest byte can't be zero");
		//			}
		//		}
		return bytes;
	}

	/**
	 * EQCQuantum
	 * <p>
	 * EQCQuantum is a series of consecutive bytes which length is from 1 to 4
	 * bytes. EQCQuantum can store positive integers divisible by 4, so that the
	 * lowest 2 bits of its lowest byte can be used as status bits. The lowest 2 bits
	 * of the lowest byte of the current byte sequence are the status bits used to
	 * indicate how many bytes it contains. The endian is big endian.
	 * <p>
	 * | XXXXXXSS | ... | XXXXXXXX |
	 * <p>
	 *
	 * @param value the original value of relevant number
	 * @return byte[] the original number's EQCBits
	 */
	public static byte[] bigIntegerToEQCQuantum(BigInteger value) {
		EQCCastle.assertNotNegative(value);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		BigInteger remainder = null;
		byte[] bytes = null;
		EQCCastle.assertNotLess(bytes.length, EQCQUANTUM_MIN_LEN);
		EQCCastle.assertNotBigger(bytes.length, EQCQUANTUM_MAX_LEN);
		if(!value.mod(EQCQUANTUM_MIN_VALUE).equals(BigInteger.ZERO)){
			throw new IllegalStateException("The value must divisible by 4.");
		}
		bytes = value.toByteArray();
		bytes[bytes.length - 1]&=(bytes.length - EQCQUANTUM_MIN_LEN);
		return bytes;
	}

	public static BigInteger eqcQuantumToBigInteger(final byte[] bytes) {
		BigInteger foo = null;
		bytes[bytes.length - 1] &= EQCQUANTUM_MASK;
		foo = new BigInteger(bytes);
		return foo;
	}

	public final static byte[] parseEQCQuantum(final ByteArrayInputStream is) throws Exception {
		int type;
		byte[] bytes = null;

		// Parse EQCQuantum
		is.mark(0);
		type = is.read();
		if (type != EOF) {
			is.reset();
			int n = (type & EQCQUANTUM_MASK) + EQCQUANTUM_MIN_LEN;
			bytes = parseNBytes(is, n);
		} else {
			throw EOF_EXCEPTION;
		}
		return bytes;
	}

	public static class EQCHelix {
		public Value value;
		public int lenOfPassport;
	}

	public final static EQCHelix parseEQCHelix(final ByteArrayInputStream is) throws Exception {
		int type;
		byte[] bytes = null;
		EQCHelix eqcHelix = new EQCHelix();

		// Parse EQCHelix
		is.mark(0);
		type = is.read();
		if (type != EOF) {
			is.reset();
			int n = (type & EQCLIGHT_MASK) + EQCLIGHT_MIN_LEN;
			eqcHelix.lenOfPassport = type & EQCHELIX_PASSPORT_LEN_MASK;
			bytes = parseNBytes(is, n);
			bytes[--n] &= EQCHELIX_VALUE_MASK;
			eqcHelix.value = new Value(bytes);
		} else {
			throw EOF_EXCEPTION;
		}
		return eqcHelix;
	}

	public final static byte[] parseEQCPassport(final ByteArrayInputStream is, EQCHelix eqcHelix) throws Exception {
		int type;
		EQCCastle.assertNotLess(eqcHelix.lenOfPassport, EQCQUANTUM_MIN_LEN);
		EQCCastle.assertNotBigger(eqcHelix.lenOfPassport, EQCQUANTUM_MAX_LEN);
		// Parse EQCPassport
		return parseNBytes(is, eqcHelix.lenOfPassport);
	}

}
