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
