/*
 * Copyright 2000-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.debugger.dbx.x86;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.x86.*;
import sun.jvm.hotspot.debugger.dbx.*;
import sun.jvm.hotspot.utilities.*;

public class DbxX86Thread implements ThreadProxy {
  private DbxDebugger debugger;
  private int         id;

  public DbxX86Thread(DbxDebugger debugger, Address addr) {
    this.debugger = debugger;

    // FIXME: the size here should be configurable. However, making it
    // so would produce a dependency on the "types" package from the
    // debugger package, which is not desired.
    this.id       = (int) addr.getCIntegerAt(0, 4, true);
  }

  public DbxX86Thread(DbxDebugger debugger, long id) {
    this.debugger = debugger;
    this.id  = (int) id;
  }

  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof DbxX86Thread)) {
      return false;
    }

    return (((DbxX86Thread) obj).id == id);
  }

  public int hashCode() {
    return id;
  }

  public ThreadContext getContext() throws IllegalThreadStateException {
    DbxX86ThreadContext context = new DbxX86ThreadContext(debugger);
    long[] regs = debugger.getThreadIntegerRegisterSet(id);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(regs.length == 19, "unknown size of register set -- adjust this code");
    }
    for (int i = 0; i < regs.length; i++) {
      context.setRegister(i, regs[i]);
    }
    return context;
  }

  public boolean canSetContext() throws DebuggerException {
    return false;
  }

  public void setContext(ThreadContext context)
    throws IllegalThreadStateException, DebuggerException {
    throw new DebuggerException("Unimplemented");
  }

  public String toString() {
    return "t@" + id;
  }
}
