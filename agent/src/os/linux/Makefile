#
# Copyright 2002-2006 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#  
#

ARCH := $(shell if ([ `uname -m` = "ia64" ])  ; then echo ia64 ; elif ([ `uname -m` = "x86_64" ]) ; then echo amd64; elif ([ `uname -m` = "sparc64" ]) ; then echo sparc; else echo i386 ; fi )
GCC      = gcc

JAVAH    = ${JAVA_HOME}/bin/javah

SOURCES  = salibelf.c   \
        symtab.c        \
	libproc_impl.c  \
	ps_proc.c       \
	ps_core.c       \
	LinuxDebuggerLocal.c 

INCLUDES = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux 

OBJS     = $(SOURCES:.c=.o)

LIBS     = -lthread_db

CFLAGS   = -c -fPIC -g -D_GNU_SOURCE -D$(ARCH) $(INCLUDES)

LIBSA = $(ARCH)/libsaproc.so

all: $(LIBSA)

LinuxDebuggerLocal.o: LinuxDebuggerLocal.c
	$(JAVAH) -jni -classpath ../../../build/classes  \
		sun.jvm.hotspot.debugger.x86.X86ThreadContext \
		sun.jvm.hotspot.debugger.sparc.SPARCThreadContext \
		sun.jvm.hotspot.debugger.amd64.AMD64ThreadContext 
	$(GCC) $(CFLAGS) $<

.c.obj:
	$(GCC) $(CFLAGS)

ifndef LDNOMAP
  LFLAGS_LIBSA = -Xlinker --version-script=mapfile
endif

$(LIBSA): $(OBJS) mapfile
	if [ ! -d $(ARCH) ] ; then mkdir $(ARCH) ; fi
	$(GCC) -shared $(LFLAGS_LIBSA) -o $(LIBSA) $(OBJS) $(LIBS)

test.o: test.c
	$(GCC) -c -o test.o -g -D_GNU_SOURCE -D$(ARCH) $(INCLUDES) test.c

test: test.o
	$(GCC) -o test test.o -L$(ARCH) -lsaproc $(LIBS)

clean:
	rm -rf $(LIBSA)
	rm -rf $(OBJS)
	rmdir $(ARCH)

