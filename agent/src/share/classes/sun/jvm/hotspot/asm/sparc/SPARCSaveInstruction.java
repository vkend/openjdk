/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package sun.jvm.hotspot.asm.sparc;

import sun.jvm.hotspot.asm.*;

public class SPARCSaveInstruction extends SPARCFormat3AInstruction {
    final private boolean trivial;
    public SPARCSaveInstruction(SPARCRegister rs1, ImmediateOrRegister operand2, SPARCRegister rd) {
        super("save", SAVE, rs1, operand2, rd);
        SPARCRegister G0 = SPARCRegisters.G0;
        trivial = (rs1 == G0 && operand2 == G0 && rd == G0);
    }

    public boolean isTrivial() {
        return trivial;
    }

    protected String getOperand2String() {
        StringBuffer buf = new StringBuffer();
        if (operand2.isRegister()) {
            buf.append(operand2.toString());
        } else {
            Number number = ((Immediate)operand2).getNumber();
            int value = number.intValue();
            if (value < 0) {
                buf.append("-0x");
                value = -value;
            } else {
                buf.append("0x");
            }

            buf.append(Integer.toHexString(value));
        }
        return buf.toString();
    }

    protected String getDescription() {
        return (trivial) ? getName() : super.getDescription();
    }
}
