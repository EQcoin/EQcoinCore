/**
 * EQcoin core
 * <p>
 * http://www.eqcoin.org
 *
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take
 * any legal action and pursue any right or remedy available under applicable
 * law.
 * <p>
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