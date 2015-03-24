package com.bookofbrilliantthings.callgrind4j;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodStats {
  // method name and line number
  private final static String selfTimeRegex =
      CallgrindTransformer.fieldPrefix + "_([_a-zA-Z0-9]+?)_([0-9]+)";
  private final static Pattern selfTimePattern = Pattern.compile(selfTimeRegex);

  // method name, called method name, and line number
  private final static String callTimeRegex =
      CallgrindTransformer.fieldPrefix + "_([_a-zA-Z0-9]+?)_call_([_a-zA-Z0-9]+?)_([0-9]+)";
  private final static Pattern callTimePattern = Pattern.compile(callTimeRegex);

  private static class Stat {
    private final int lineNumber;
    private final long nanoTime;
    private final String calledMethod;

    public Stat() {
    }
  }

  private Stat selfStat = null;
  private final List<Stat> callStats = new LinkedList<>();

  public MethodStats(final Method method, final Field[] fields) {
    final String methodName = method.getName();

    // go through the fields, checking for stats belonging to this method
    for(Field field : fields) {
      final String fieldName = field.getName();
      final int fieldEnd = fieldName.length() - 1;

      // check for the call record
      final Matcher callMatcher = callTimePattern.matcher(fieldName);
      if (callMatcher.matches() && (callMatcher.start() == 0) && (callMatcher.end() == fieldEnd)) {
        final String callingName = callMatcher.group(0);
        if (!callingName.equals(methodName))
          continue; // not for this method

        final String calleeName = callMatcher.group(1);
        final int callLineNumber = Integer.parseInt(callMatcher.group(2));
        callStats.add(new Stat());
        // TODO

        continue;
      }

      // check for the self record
      final Matcher selfMatcher = selfTimePattern.matcher(fieldName);
      if (selfMatcher.matches() && (selfMatcher.start() == 0) && (selfMatcher.end() == fieldEnd)) {
        final String callingName = callMatcher.group(0);
        if (!callingName.equals(methodName))
          continue; // not for this method
        // TODO

        continue;
      }
    }
  }

  public void writeSelfTime(final PrintWriter printWriter) {
    // TODO
  }

  public void writeCallTimes(final PrintWriter printWriter) {

  }
}
