package com.bookofbrilliantthings.callgrind4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;

/**
 * Transforms bytecode to generate the callgrind input file.
 *
 * <p>This should be registered as a retransformation incapable transformer.
 * See {@link java.lang.instrument.ClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])}.
 * </p>
 *
 * @see http://valgrind.org/docs/manual/cl-format.html
 */
public class CallgrindTransformer implements ClassFileTransformer {
  private static class ClassDetails {
    final ClassLoader classLoader;

    ClassDetails(final ClassLoader classLoader) {
      this.classLoader = classLoader;
    }
  }

  /**
   * We have to keep track of information about the classes we transform, because
   * there's no other way to find them once we transform them.
   */
  private final ConcurrentHashMap<String, ClassDetails> classMap = new ConcurrentHashMap<>();

  final static String fieldPrefix = "callgrind4j"; // TODO make this settable

  private class DataWriter implements Runnable {
    private final PrintWriter printWriter;

    public DataWriter(final PrintWriter printWriter) {
      this.printWriter = printWriter;
    }

    @Override
    public void run() {
      try {
        final Set<Map.Entry<String, ClassDetails>> classSet = classMap.entrySet();
        for(final Map.Entry<String, ClassDetails> mapEntry : classSet) {
          final String className = mapEntry.getKey();
          final ClassDetails classDetails = mapEntry.getValue();

          // get the class
          Class<?> theClass;
          try {
            theClass = classDetails.classLoader.loadClass(className);
          } catch(ClassNotFoundException e) {
            System.err.println("Unable to find class "+ className + '\n');
            continue;
          }

          // write the file name
          printWriter.println("fl=" + className + ".java");

          // for each method in the class, find the collected call statistics
          final Method[] methods = theClass.getDeclaredMethods();
          final Field[] fields = theClass.getFields();
          for(final Method method : methods) {
            final MethodStats methodStats = new MethodStats(method, fields);
            printWriter.println("fn=" + method.getName());

            // write the self time
            methodStats.writeSelfTime(printWriter);

            printWriter.println(String.format("%d %d", lineNumber, methodSelfNanos)); // TODO

            // write the call time for each called function
            methodStats.writeCallTimes(printWriter);

            printWriter.println();
          }
        }
      } finally {
        printWriter.close();
      }
    }
  }

  private final static String OUTPUT_CHARSET_NAME = "US-ASCII";

  /**
   * Register the transformer when it is launched as a java agent.
   *
   * @param agentArgs agent argument string
   * @param inst instrumentation interface
   */
  public static void premain(String agentArgs, Instrumentation inst) {
    // create the output file
    // TODO create better name, support options
    final String filename = "callgrind.out";
    final File file = new File(filename);
    final PrintWriter printWriter;
    try {
      printWriter = new PrintWriter(file, OUTPUT_CHARSET_NAME);
    } catch(UnsupportedEncodingException e) {
      // This shouldn't happen because OUTPUT_CHARSET_NAME should be a mandatory character set
      throw new RuntimeException("Invalid output Charset specified (" + OUTPUT_CHARSET_NAME + ')', e);
    } catch(FileNotFoundException e) {
      throw new RuntimeException("Couldn't open file \"" + filename + "\"", e);
    }

    // write the file preamble
    printWriter.println("events: nanos");

    // install the transformer
    final CallgrindTransformer transformer = new CallgrindTransformer();
    inst.addTransformer(transformer, false);

    // install the shutdown hook to dump the collected data
    final Runtime runtime = Runtime.getRuntime();
    runtime.addShutdownHook(new Thread(transformer.new DataWriter(printWriter)));
  }

  private String makeDottedNameFromSlashName(final String slashName) {
    return slashName.replace('/', '.');
  }

  @Override
  public byte[] transform(final ClassLoader loader, final String className,
      final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain,
      final byte[] classfileBuffer) throws IllegalClassFormatException {
    // load the class file
    final ClassReader classReader = new ClassReader(classfileBuffer);
    // TODO

    // record the class we've transformed
    final String dottedName = makeDottedNameFromSlashName(className);
    classMap.put(dottedName, new ClassDetails(loader));

    // TODO Auto-generated method stub
    return null;
  }
}
