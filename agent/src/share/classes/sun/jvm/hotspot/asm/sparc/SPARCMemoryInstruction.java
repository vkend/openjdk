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

import sun.jvm.hotspot.asm.MemoryInstruction;
import sun.jvm.hotspot.asm.SymbolFinder;

public abstract class SPARCMemoryInstruction extends SPARCInstruction
    implements MemoryInstruction {
    final protected SPARCRegisterIndirectAddress address;
    final protected SPARCRegister register;
    final protected int dataType;
    final protected int opcode;

    public SPARCMemoryInstruction(String name, int opcode, SPARCRegisterIndirectAddress address, SPARCRegister register, int dataType) {
        super(name);
        this.address = address;
        this.register = register;
        this.dataType = dataType;
        this.opcode = opcode;
    }

    protected String getDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(address.toString());
        buf.append(comma);
        buf.append(register.toString());
        return buf.toString();
    }

    public String asString(long currentPc, SymbolFinder symFinder) {
        return getDescription();
    }

    public int getDataType() {
        return dataType;
    }

    public boolean isConditional() {
        return false;
    }

    public int getOpcode() {
        return opcode;
    }
}
