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
package org.eqcoin.ut.misc;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eqcoin.crypto.ECDSASignature;
import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 12, 2020
 * @email 10509759@qq.com
 */
public class ECCSignatureTest {
	
	private static EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(ECCTYPE.P256);
	private static StringBuilder sb;
	
	public static void main(String[] args) throws Exception {
		 byte[] signature1 = getSignature();  
		 Log.info(Util.bytesToHexString(signature1));
		 ECDSASignature ecdsaSignature = ECDSASignature.decodeFromDER(signature1);
		 Log.info(Util.bytesToHexString(ecdsaSignature.encodeToDER()));
		
//        Security.addProvider(new BouncyCastleProvider());
		
//		for(int i=0; i<10000; ++i) {
//			 byte[] signature1 = getSignature();  
//			 byte id = 0;
//			 byte[] pub = eqcPublicKey.recoveryPublickey(Util.SHA3_256("message to sign".getBytes("UTF-8")), signature1, eqcPublicKey.getCompressedPublicKeyEncoded());
////			 Log.info("\n" + sb.append(Util.bytesToHexString(pub)).toString());
//			 if(pub == null) {
//				 Log.Error("can't recovery publickey");
//			 }
//		}
		
		 //		Log.info(""+id);
//		byte[] pub = eqcPublicKey.recoverFromSignature(id, ECDSASignature.decodeFromDER(signature1), Util.SHA3_256("message to sign".getBytes("UTF-8")));
//		 Log.info("Compressed Publickey: " + Util.bytesToHexString(pub));
		 byte[] bytes = null;
		 int n130 = 0, n131 = 0, n132 = 0;
		 
		  StringBuilder sb = new StringBuilder();
//		for(int i=0; i<100000; ++i) {
//			int total = 0;
//			 byte[] signature = getSignature();    
//			  sb.append("\n");
//			 sb.append(Util.getHexString(signature));
//		        ASN1Primitive asn1 = toAsn1Primitive(signature);
//
//		        if (asn1 instanceof ASN1Sequence) {
//		            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
//		            ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
//		            for (ASN1Encodable asn1Encodable : asn1Encodables) {
//		                ASN1Primitive asn1Primitive = asn1Encodable.toASN1Primitive();
//		                if (asn1Primitive instanceof ASN1Integer) {
//		                    ASN1Integer asn1Integer = (ASN1Integer) asn1Primitive;
//		                    java.math.BigInteger integer = asn1Integer.getValue();
////		                    System.out.println(integer.toString());
////		                    sb.append("\n");
//		                    bytes = integer.toByteArray();
//		                    total += bytes.length;
////		                    if(bytes[0] == 0) {
////		                    	sb.append("0-"+ (bytes.length - 1) + " ");
////		                    }
////		                    else {
////		                    	sb.append(""+bytes.length + " ");
////		                    }
////		                    sb.append(integer.toString(16).length());
////		                    sb.append(" ");
////		                    sb.append(integer.toString(16));
//		                }
//		            }
////		            sb.append("\n");
//		        }
//		        if(total == 130) {
//                	++n130;
//                }
//                else if(total == 131) {
//                	++n131;
//                }
//                else if(total == 132) {
//                	++n132;
//                }
//                else {
//                	Log.info("" + total);
//                }
//		}
//		Log.info("n130: " + n130);
//		Log.info("n131: " + n131);
//		Log.info("n132: " + n132);
//		 Log.info(sb.toString());
    }

    private static ASN1Primitive toAsn1Primitive(byte[] data) throws Exception
    {
        try (ByteArrayInputStream inStream = new ByteArrayInputStream(data);
                ASN1InputStream asnInputStream = new ASN1InputStream(inStream);) 
        {
            return asnInputStream.readObject();
        }
    }

    private static byte[] getSignature() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "SunEC");//getInstance("ECDSA");
        ECGenParameterSpec ecParameterSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecParameterSpec, SecureRandom.getInstance("SHA1PRNG"));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        eqcPublicKey.setECPoint((ECPublicKey) keyPair.getPublic());
//        Log.info("Compressed Publickey: " + Util.bytesToHexString(eqcPublicKey.getCompressedPublicKeyEncoded()));
        sb = new StringBuilder();
        sb.append(Util.bytesToHexString(eqcPublicKey.getCompressedPublicKeyEncoded()));
        sb.append("\n");
        Signature signature = Signature.getInstance("NONEwithECDSA", "SunEC");//Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(MessageDigest.getInstance(Util.SHA3_256).digest("message to sign".getBytes("UTF-8")));

        return signature.sign();
    }

}

