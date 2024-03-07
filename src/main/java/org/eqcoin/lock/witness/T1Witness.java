/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.lock.witness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

import org.eqcoin.crypto.ECDSASignature;
import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.crypto.RecoverySECP256R1Publickey;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public class T1Witness extends Witness {
	private byte[] compressedPublickey;
	/**
	 * The relevant passport's master lock
	 */
	private LockMate lockMate;
	
	public T1Witness() {
	}
	
	public T1Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public T1Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(witness == null) {
			Log.Error("witness == null");
			return false;
		}
		if(witness.length != Util.P256_SIGNATURE_LEN.intValue()) {
			Log.Error("witness.length != Util.P256_SIGNATURE_LEN.intValue()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#initPlanting()
	 */
	@Override
	public boolean isMeetPreCondition() throws Exception {
		Lock lock = null;
		ID lockMateId = null;
		for (int i = 0; i < 2; ++i) {
			compressedPublickey = RecoverySECP256R1Publickey.getInstance().recoverFromSignature(i,
					getDERSignature(), transaction.getSignBytesHash());
			lock = LockTool.publickeyToEQCLock(LockType.T1, compressedPublickey);
			if ((lockMateId = transaction.getEQCHive().getGlobalState().isLockMateExists(lock)) != null) {
				break;
			}
		}
		if(lockMateId == null) {
			Log.Error("Witness relevant lock mate doesn't exists");
			return false;
		}
		if(lockMateId.compareTo(transaction.getEQCHive().getPreRoot().getTotalLockMateNumbers()) >= 0) {
			Log.Error("Witness relevant lock mate doesn't confirmed");
			return false;
		}
//		lockMate = transaction.getEQCHive().getGlobalState().getLockMate(lockMateId);
//		if(lockMate == null) {
//			Log.Error("lockMate == null");
//			return false;
//		}
		passport = transaction.getEQCHive().getGlobalState().getPassportFromLockMateId(lockMateId);
		if(passport == null) {
			Log.Error("passport == null");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isValid()
	 */
	@Override
	public boolean isValid() throws Exception {
		if (!lockMate.getPublickey().isNULL()) {
			if(!Arrays.equals(lockMate.getPublickey().getPublickey(), compressedPublickey)) {
				Log.Error("Although relevant lock's proof equal but the corresponding publickey doesn't equal");
				return false;
			}
		}
		return verifySignature();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"T1Witness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(witness) + "\"\n" +
				"}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		witness = EQCCastle.parseNBytes(is, Util.P256_SIGNATURE_LEN.intValue());
	}
	
	public static byte[] DERTosignature(byte[] derSignature) throws Exception {
		ECDSASignature ecdsaSignature = null;
		byte[] r = null, s = null;
		
		ecdsaSignature = ECDSASignature.decodeFromDER(derSignature);
		r = ecdsaSignature.getR().toByteArray();
		s = ecdsaSignature.getS().toByteArray();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(r.length < Util.P256_POINT_LEN.intValue()) {
			os.write(new byte[Util.P256_POINT_LEN.intValue() - r.length]);
		}
		os.write(r);
		if(s.length < Util.P256_POINT_LEN.intValue()) {
			os.write(new byte[Util.P256_POINT_LEN.intValue() - s.length]);
		}
		os.write(s);
		return os.toByteArray();
	}

	public final byte[] getDERSignature() throws Exception {
		byte[] r = null, s = null;
		ByteArrayInputStream is = new ByteArrayInputStream(witness);
		r = EQCCastle.parseNBytes(is, Util.P256_POINT_LEN.intValue());
		s = EQCCastle.parseNBytes(is, Util.P256_POINT_LEN.intValue());
		ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
		return ecdsaSignature.encodeToDER();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#setWitness(byte[])
	 */
	@Override
	public void setWitness(byte[] witness) throws Exception {
		this.witness = DERTosignature(witness);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#getProof()
	 */
	@Override
	public byte[] getProof() {
		byte[] bytes = new byte[Util.PROOF_SIZE];
		bytes[0] = witness[0];
		bytes[1] = witness[9];
		bytes[2] = witness[20];
		bytes[3] = witness[31];
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#getMaxBillingLength()
	 */
	@Override
	public BigInteger getMaxBillingLength() {
		return Util.P256_SIGNATURE_LEN;
	}

	public boolean verifySignature() throws Exception {
		boolean isTransactionValid = false;
		Signature signature = null;
		// Verify Signature
		try {
			signature = Signature.getInstance("NONEwithECDSA", "SunEC");
			EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(ECCTYPE.P256);
			// Create EQPublicKey according to compressed Publickey
			eqcPublicKey.setECPoint(compressedPublickey);
			signature.initVerify(eqcPublicKey);
//			Log.info("\nPublickey: " + Util.dumpBytesLittleEndianHex(txInLockMate.getEqcPublickey().getPublickey()));
//			Log.info("\nMessageLen: " + getBytes().length + "\nMessageBytes: " + Util.dumpBytesLittleEndianHex(getBytes()));
//			Log.info("\nMessage Hash: " + Util.dumpBytesLittleEndianHex(MessageDigest.getInstance(Util.SHA3_512).digest(getBytes())));
			signature.update(transaction.getSignBytesHash());
			isTransactionValid = signature.verify(getDERSignature());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | InvalidKeyException e) {
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#free()
	 */
	@Override
	public void free() {
		super.free();
		compressedPublickey = null;
		lockMate = null;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#planting()
	 */
	@Override
	public void planting() throws Exception {
		super.planting();
		// Update publickey if need here exists one bug when forbidden doesn't update publickey
		if (lockMate.isLively() && lockMate.getPublickey().isNULL()) {
			lockMate.getPublickey().setPublickey(compressedPublickey);
			lockMate.setEQCHive(transaction.getEQCHive()).planting();
		}
	}
	
	/**
	 * @return the lockMate
	 */
	public LockMate getLockMate() {
		return lockMate;
	}

	@Override
	public void forbidden() throws Exception {
		lockMate.forbidden();
	}
	
}
