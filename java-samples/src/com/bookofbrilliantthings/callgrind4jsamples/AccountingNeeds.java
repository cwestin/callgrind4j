// TODO need license header
package com.bookofbrilliantthings.callgrind4jsamples;

import java.util.concurrent.atomic.AtomicLong;

public class AccountingNeeds {
  public void uninstrumentedMethod(final SomeClass someClass) {
    someClass.a();
    someClass.b();
  }

  /*
   * Instrumentation requires a total for the entire function, and a value for
   * each function called from within; these should be distinct, even if they
   * are multiple calls to the same function. The generated file format provides
   * for a means to distinguish these by line number.
   *
   * Note that because of Java's capability to throw exceptions, we have to wrap
   * the function body, and all function calls, with try-finally in order to be
   * sure we capture the end time.
   *
   * When generating that file, the self value for the function must subtract
   * all the function call values from the function's total before reporting it.
   * This implies we have to have an easy way to identify which of the added
   * statics belong to that function. We can use a convention such as
   * callgrind4j_methodName_lineNumber_methodName Note that this won't
   * distinguish between multiple calls on the same line, (even though we
   * could).
   *
   * We can dump the data from a shutdown hook, by walking through the loaded
   * classes looking for our instrumentation. It looks like the ClassLoader
   * doesn't have a means to list the loaded classes, so we'll have to keep
   * track of the instrumented classes ourselves with a List or a Map. This
   * conveniently solves the problem of what to do in the face of multiple class
   * loaders, such as those used by Drill.
   */
  private final static AtomicLong instrumentedMethod_total = new AtomicLong();
  private final static AtomicLong instrumentedMethod_15_a = new AtomicLong();
  private final static AtomicLong instrumentedMethod_19_b = new AtomicLong();

  public void instrumentedMethod(final SomeClass someClass) {
    final long startTime = System.nanoTime();
    try {
      long callStartTime;

      callStartTime = System.nanoTime();
      try {
        someClass.a();
      } finally {
        instrumentedMethod_15_a.addAndGet(System.nanoTime() - callStartTime);
      }

      callStartTime = System.nanoTime();
      try {
        someClass.b();
      } finally {
        instrumentedMethod_19_b.addAndGet(System.nanoTime() - callStartTime);
      }
    } finally {
      instrumentedMethod_total.addAndGet(System.nanoTime() - startTime);
    }
  }
}
