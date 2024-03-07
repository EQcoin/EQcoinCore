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
package org.eqcoin.util;

import org.eqcoin.serialization.EQCCastle;

import java.math.BigInteger;

/**
 * @author Xun Wang
 * @date 2024-03-06
 * @email 10509759@qq.com
 */
public class Status extends BigInteger {
    /**
     * @param BigInteger
     */
    public Status(final BigInteger value) {
        super(value.toByteArray());
        EQCCastle.assertPositive(this);
        EQCCastle.assertNotBigger(this, Util.MAX_STATUS);
    }

    /**
     * @param EQCBits
     */
    public Status(final byte[] bytes) {
        super(EQCCastle.eqcBitsToBigInteger(bytes).toByteArray());
        EQCCastle.assertPositive(this);
        EQCCastle.assertNotBigger(this, Util.MAX_STATUS);
    }

    /**
     * @param long
     */
    public Status(final long value) {
        super(BigInteger.valueOf(value).toByteArray());
        EQCCastle.assertPositive(this);
    }

    /**
     * @return current Status's EQCBits
     */
    public byte[] getEQCBits() {
        return EQCCastle.bigIntegerToEQCBits(this);
    }

    public boolean isSanity() throws Exception {
        if(this.compareTo(Value.ZERO) <= 0) {
            Log.Error(this + " <= 0");
            return false;
        }
        if(this.compareTo(Util.MAX_STATUS) > 0) {
            Log.Error(this + " > " + Util.MAX_STATUS);
            return false;
        }
        return true;
    }

}
