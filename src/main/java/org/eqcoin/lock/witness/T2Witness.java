/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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
package org.eqcoin.lock.witness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;

import org.eqcoin.crypto.ECDSASignature;
import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.crypto.RecoverySECP256R1Publickey;
import org.eqcoin.crypto.RecoverySECP521R1Publickey;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public class T2Witness extends Witness {
	private byte[] compressedPublickey;
	/**
	 * The relevant passport's master lock
	 */
	private LockMate lockMate;
	
	public T2Witness() {
	}
	
	public T2Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public T2Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(witness == null) {
			Log.Error("signature == null");
			return false;
		}
		if(witness.length != Util.P521_SIGNATURE_LEN.intValue()) {
			Log.Error("signature.length != Util.P521_SIGNATURE_LEN.intValue()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"T2Witness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(witness) + "\"\n" +
				"}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		witness = EQCCastle.parseNBytes(is, Util.P521_SIGNATURE_LEN.intValue());
	}

	public static byte[] DERToEQCSignature(byte[] derSignature) throws Exception {
		ECDSASignature ecdsaSignature = null;
		byte[] r = null, s = null;
		
		ecdsaSignature = ECDSASignature.decodeFromDER(derSignature);
		r = ecdsaSignature.getR().toByteArray();
		s = ecdsaSignature.getS().toByteArray();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(r.length < Util.P521_POINT_LEN.intValue()) {
			os.write(new byte[Util.P521_POINT_LEN.intValue() - r.length]);
		}
		os.write(r);
		if(s.length < Util.P521_POINT_LEN.intValue()) {
			os.write(new byte[Util.P521_POINT_LEN.intValue() - s.length]);
		}
		os.write(s);
		return os.toByteArray();
	}

	public byte[] getDERSignature() throws Exception {
		byte[] r = null, s = null;
		ByteArrayInputStream is = new ByteArrayInputStream(witness);
		r = EQCCastle.parseNBytes(is, Util.P521_POINT_LEN.intValue());
		s = EQCCastle.parseNBytes(is, Util.P521_POINT_LEN.intValue());
		ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
		return ecdsaSignature.encodeToDER();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#setWitness(byte[])
	 */
	@Override
	public void setWitness(byte[] witness) throws Exception {
		this.witness = DERToEQCSignature(witness);
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
		bytes[1] = witness[21];
		bytes[2] = witness[42];
		bytes[3] = witness[63];
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#getMaxBillingLength()
	 */
	@Override
	public BigInteger getMaxBillingLength() {
		return Util.P521_SIGNATURE_LEN;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.transaction.Witness#verifySignature()
	 */
	@Override
	public boolean verifySignature() throws Exception {
		boolean isTransactionValid = false;
		Signature signature = null;
		// Verify Signature
		try {
			signature = Signature.getInstance("NONEwithECDSA", "SunEC");
			EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(ECCTYPE.P521);
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
	 * @see org.eqcoin.transaction.Witness#initPlanting()
	 */
	@Override
	public boolean isMeetPreCondition() throws Exception {
		Lock lock = null;
		ID lockMateId = null;
		for (int i = 0; i < 2; ++i) {
			compressedPublickey = RecoverySECP521R1Publickey.getInstance().recoverFromSignature(i,
					getDERSignature(), transaction.getSignBytesHash());
			lock = LockTool.publickeyToEQCLock(LockType.T2, compressedPublickey);
			if ((lockMateId = transaction.getEQCHive().getGlobalState().isLockMateExists(lock)) != null) {
				break;
			}
		}
		if(lockMateId == null) {
			Log.Error("Witness relevant lock doesn't exists");
			return false;
		}
		if(lockMateId.compareTo(transaction.getEQCHive().getPreRoot().getTotalLockMateNumbers()) >= 0) {
			Log.Error("Witness relevant lock mate doesn't confirmed");
			return false;
		}
//		lockMate = transaction.getChangeLog().getFilter().getLock(lockMateId, true);
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

	/**
	 * @return the lockMate
	 */
	public LockMate getLockMate() {
		return lockMate;
	}
	
}
