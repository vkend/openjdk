/*
 * Copyright 2000-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

// ObjArrayKlass is the klass for ObjArrays

public class ObjArrayKlass extends ArrayKlass {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type = db.lookupType("objArrayKlass");
    elementKlass = new OopField(type.getOopField("_element_klass"), Oop.getHeaderSize());
    bottomKlass  = new OopField(type.getOopField("_bottom_klass"), Oop.getHeaderSize());
  }

  ObjArrayKlass(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }

  private static OopField elementKlass;
  private static OopField bottomKlass;

  public Klass getElementKlass() { return (Klass) elementKlass.getValue(this); }
  public Klass getBottomKlass()  { return (Klass) bottomKlass.getValue(this); }

  public long computeModifierFlags() {
    long elementFlags = getElementKlass().computeModifierFlags();
    long arrayFlags = 0L;
    if ((elementFlags & (JVM_ACC_PUBLIC | JVM_ACC_PROTECTED)) != 0) {
       // The array type is public if the component type is public or protected
       arrayFlags = JVM_ACC_ABSTRACT | JVM_ACC_FINAL | JVM_ACC_PUBLIC;
    } else {
       // The array type is private if the component type is private
       arrayFlags = JVM_ACC_ABSTRACT | JVM_ACC_FINAL;
    }
    return arrayFlags;
  }

  public void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(elementKlass, true);
      visitor.doOop(bottomKlass, true);
    }
  }

  public Klass arrayKlassImpl(boolean orNull, int n) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(getDimension() <= n, "check order of chain");
    }
    int dimension = (int) getDimension();
    if (dimension == n) {
      return this;
    }
    ObjArrayKlass ak = (ObjArrayKlass) getHigherDimension();
    if (ak == null) {
      if (orNull) return null;
      // FIXME: would need to change in reflective system to actually
      // allocate klass
      throw new RuntimeException("Can not allocate array klasses in debugging system");
    }

    if (orNull) {
      return ak.arrayKlassOrNull(n);
    }
    return ak.arrayKlass(n);
  }

  public Klass arrayKlassImpl(boolean orNull) {
    return arrayKlassImpl(orNull, (int) (getDimension() + 1));
  }

  public void printValueOn(PrintStream tty) {
    tty.print("ObjArrayKlass for ");
    getElementKlass().printValueOn(tty);
  }
};
