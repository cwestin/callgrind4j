package com.bookofbrilliantthings.callgrind4j;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CallgrindClassVisitor extends ClassVisitor {

  public CallgrindClassVisitor(final int flags, final ClassWriter classWriter) {
    super(flags, classWriter);
  }

  // TODO Auto-generated constructor stub
}
