/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

# include "incls/_precompiled.incl"
# include "incls/_typeArrayKlassKlass.cpp.incl"

klassOop typeArrayKlassKlass::create_klass(TRAPS) {
  typeArrayKlassKlass o;
  KlassHandle h_this_klass(THREAD, Universe::klassKlassObj());
  KlassHandle k = base_create_klass(h_this_klass, header_size(), o.vtbl_value(), CHECK_NULL);
  assert(k()->size() == align_object_size(header_size()), "wrong size for object");
  java_lang_Class::create_mirror(k, CHECK_NULL); // Allocate mirror
  return k();
}


#ifndef PRODUCT

// Printing

void typeArrayKlassKlass::oop_print_on(oop obj, outputStream* st) {
  assert(obj->is_klass(), "must be klass");
  oop_print_value_on(obj, st);
  Klass:: oop_print_on(obj, st);
}


void typeArrayKlassKlass::oop_print_value_on(oop obj, outputStream* st) {
  assert(obj->is_klass(), "must be klass");
  st->print("{type array ");
  switch (typeArrayKlass::cast(klassOop(obj))->element_type()) {
    case T_BOOLEAN: st->print("bool");    break;
    case T_CHAR:    st->print("char");    break;
    case T_FLOAT:   st->print("float");   break;
    case T_DOUBLE:  st->print("double");  break;
    case T_BYTE:    st->print("byte");    break;
    case T_SHORT:   st->print("short");   break;
    case T_INT:     st->print("int");     break;
    case T_LONG:    st->print("long");    break;
    default: ShouldNotReachHere();
  }
  st->print("}");
}

#endif

const char* typeArrayKlassKlass::internal_name() const {
  return "{type array class}";
}
