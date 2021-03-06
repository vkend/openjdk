/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.asm.x86;

import sun.jvm.hotspot.asm.*;

public class X86ShiftInstruction extends X86Instruction implements ShiftInstruction {
   final private int operation;
   final private Operand operand1;
   final private ImmediateOrRegister operand2;

   public X86ShiftInstruction(String name, int operation, Operand operand1, ImmediateOrRegister operand2, int size, int prefixes) {
      super(name, size, prefixes);
      this.operand1 = operand1;
      this.operand2 = operand2;
      this.operation = operation;
   }

   public String asString(long currentPc, SymbolFinder symFinder) {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      buf.append(spaces);
      buf.append(getOperandAsString(operand1));

      if(operand2 != null) {
         buf.append(comma);

         if ((operand2 instanceof Register)) {
            buf.append(operand2.toString());
         }
         else {
            Number number = ((Immediate)operand2).getNumber();
            buf.append("0x");
            buf.append(Integer.toHexString(number.intValue()));
         }
      }
      return buf.toString();
   }

   public int getOperation() {
      return operation;
   }

   public Operand getShiftDestination() {
      return operand1;
   }

   public Operand getShiftLength() {
      return operand2;
   }

   public Operand getShiftSource() {
      return operand1;
   }

   public boolean isShift() {
      return true;
   }

   protected String getOperand2String() {
      return operand2.toString();
   }
}
