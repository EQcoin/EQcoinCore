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
package org.eqcoin.transaction.txout;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

/**
 * @author Xun Wang
 * @date 2022-08-16
 * @email 10509759@qq.com
 */
public class TransferTxOut extends TransferTxOutQuantum {

    private int nLen;

    public TransferTxOut(byte[] bytes) throws Exception {
        super(bytes);
    }

    public TransferTxOut(ByteArrayInputStream is) throws Exception {
        super(is);
    }

    public TransferTxOut() {
        super();
    }

    @Override
    public void parse(ByteArrayInputStream is) throws Exception {
        // Parse Passport ID
        passportId = new ID(EQCCastle.parseEQCQuantum(is));
        // Parse Value
        value = new Value(EQCCastle.parseNBytes(is, nLen));
    }

    @Override
    public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
        os.write(passportId.toByteArray());
        os.write(value.getEQCLight());
        return os;
    }

    @Override
    public TransferTxOut Parse(ByteArrayInputStream is) throws Exception {
        throw new IllegalStateException("TransferTxOut doesn't support Parse(ByteArrayInputStream is)");
    }

    @Override
    public boolean isSanity() throws Exception {
        if(passportId == null) {
            Log.Error("passportId == null");
            return false;
        }
        if(!passportId.isSanity()) {
            Log.Error("!passportId.isSanity()");
            return false;
        }
        if(passportId.mod(BigInteger.valueOf(4)) == BigInteger.ZERO){
            Log.Error("The Passport'ID in TransferTxOutQuantum can't divisible by 4.");
            return false;
        }
        if(value == null) {
            Log.Error("value == null");
            return false;
        }
        if(!value.isSanity()) {
            Log.Error("!value.isSanity()");
            return false;
        }
        if(value.mod(BigInteger.valueOf(1000)) != BigInteger.ZERO){
            Log.Error("The transfer value in TransferTxOutQuantum must divisible by 1000.");
            return false;
        }
        return true;
    }

    @Override
    public String toInnerJson() {
        return
                "\"TransferTxOut\":" +
                        "\n{" +
                        "\"PassportId\":" + passportId + ",\n" +
                        "\"Value\":" + "\"" +  Long.toString(value.longValue()) + "\"" + "\n" +
                        "}";
    }

    public int getnLen() {
        return nLen;
    }

    public TransferTxOut setnLen(int nLen) {
        this.nLen = nLen;
        return this;
    }

}
