/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
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
package org.eqcoin.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Vector;

import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Nov 12, 2018
 * @email 10509759@qq.com
 */
public class MerkleTree {
	private byte[] root;
	private Vector<byte[]> nodeList;
	private MessageDigest messageDigest;

	public MerkleTree(Vector<byte[]> bytes, boolean isHashing) throws NoSuchAlgorithmException {
		nodeList = bytes;
		root = null;
		messageDigest = MessageDigest.getInstance("SHA3-512");
		if(isHashing) {
			for(byte[] element:bytes) {
				element = messageDigest.digest(element);
			}
		}
	}

	public void generateRoot() {
		if(nodeList.size() == 0) {
			return;
		}
		Vector<byte[]> nodes = nodeList;
		if(nodeList.size() > 1) {
			while ((nodes = getNextNodeList(nodes)).size() > 1) {
			}
		}
		root = nodes.get(0);
	}

	public Vector<byte[]> getNextNodeList(Vector<byte[]> nodes) {
		Vector<byte[]> nextNodeList = new Vector<byte[]>();
		byte[] left = null, right = null, bytes = null;
		Iterator<byte[]> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			// Left node
			left = iterator.next();
			// Right node
			if (iterator.hasNext()) {
				right = iterator.next();
				// Left node and right node's hash
				bytes = new byte[left.length + right.length];
				System.arraycopy(left, 0, bytes, 0, left.length);
				System.arraycopy(right, 0, bytes, left.length, right.length);
			}
			else {
				// Left node and pre right node's flip's hash
				bytes = new byte[left.length + right.length];
				System.arraycopy(left, 0, bytes, 0, left.length);
				System.arraycopy(right, 0, bytes, left.length, right.length);
			}
			nextNodeList.add(messageDigest.digest(bytes));
		}
		return nextNodeList;
	}

	public byte[] getRoot() {
		return root;
	}

}
