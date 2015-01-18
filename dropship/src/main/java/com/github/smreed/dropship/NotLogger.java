package com.github.smreed.dropship;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
class NotLogger {

  static void debug(String format, Object arg, Object... otherArgs) {
    if (Settings.DEBUG) {
      debug(format(format, arg, otherArgs));
    }
  }

  static void debug(String message) {
    if (Settings.DEBUG) {
      write("DEBUG", message);
    }
  }

  static void info(String format, Object arg, Object... otherArgs) {
    info(format(format, arg, otherArgs));
  }

  static void info(String message) {
    write(" INFO", message);
  }

  static void warn(String format, Object arg, Object... otherArgs) {
    warn(format(format, arg, otherArgs));
  }

  static void warn(String message) {
    write(" WARN", message);
  }

  private static void write(String level, String line) {
    System.out.format("[Dropship %s] %s%n", level, line);
  }

  private static String format(String format, Object arg, Object... otherArgs) {
    checkNotNull(format);
    checkNotNull(arg);
    checkNotNull(otherArgs);

    if (otherArgs.length > 0) {
      Object[] formatArgs = new Object[otherArgs.length + 1];
      formatArgs[0] = arg;
      System.arraycopy(otherArgs, 0, formatArgs, 1, otherArgs.length);
      return String.format(format, formatArgs);
    } else {
      return String.format(format, arg);
    }
  }
}
