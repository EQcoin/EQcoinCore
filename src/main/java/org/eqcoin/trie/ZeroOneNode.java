/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.trie;

import org.eqcoin.persistence.statedb.StateDB;
import org.eqcoin.serialization.EQCStateObject;
import org.eqcoin.util.ID;

/**
 * @author Xun Wang
 * @date 2022-02-13
 * @email 10509759@qq.com
 */
public abstract class ZeroOneNode<V extends EQCStateObject> extends EQCStateObject implements Node<V> {

    private ID status;

    private ZeroNode<V> zeroNode;

    private OneNode<V> oneNode;

    // here maybe exists one bug the above two objects should be merged into only one object
    private V value;

    private ID id;

    protected byte orbit;

    private boolean isZeroNodeNull;

    private boolean isOneNodeNull;

    private boolean isDirty;

    private boolean isLoaded;

    public ZeroOneNode() {
        isZeroNodeNull = true;
        isOneNodeNull = true;
    }

    public ZeroOneNode(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    public byte[] getHash() throws Exception {
        return new byte[0];
    }

    @Override
    public Node accept(NodeVisitor nodeVisitor) {
        return null;
    }

    @Override
    public byte getOrbit() {
        return orbit;
    }

    @Override
    public ID getID() {
        return id;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void markDirty() {
        isDirty = true;
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void load() {
        isLoaded = true;
    }

    @Override
    public void unload() {
        isLoaded = false;
    }

    public boolean isZeroNodeNull(){
        return  isZeroNodeNull;
    };

    public boolean isOneNodeNull(){
        return  isOneNodeNull;
    };

    public ZeroNode<V> getZeroNode() {
        return zeroNode;
    }

    public void setZeroNode(ZeroNode<V> zeroNode) {
        this.zeroNode = zeroNode;
        isZeroNodeNull = false;
    }

    public OneNode<V> getOneNode() {
        return oneNode;
    }

    public void setOneNode(OneNode<V> oneNode) {
        this.oneNode = oneNode;
        isOneNodeNull = false;
    }

    @Override
    public void setStateDB(StateDB stateDB) {

    }

    @Override
    public ID getStatus() {
        return status;
    }

    public void setStatus(ID status) {
        this.status = status;
    }

    @Override
    public void setValue(V value) {
        this.value = value;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

}
