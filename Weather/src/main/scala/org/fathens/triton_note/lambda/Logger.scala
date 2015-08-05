package org.fathens.triton_note.lambda

import com.amazonaws.services.lambda.runtime.LambdaLogger

object LogLevel extends Enumeration {
  val DEBUG, INFO, WARN, ERROR, FATAL = Value
}

class Logger(logger: LambdaLogger, logLevel: LogLevel.Value = LogLevel.DEBUG) {
  private def println(line: String) { logger.log(line + "\n") }

  def debug(log: => String) = if (logLevel <= LogLevel.DEBUG) println(log)
  def info(log: => String) = if (logLevel <= LogLevel.INFO) println(log)
  def warn(log: => String) = if (logLevel <= LogLevel.WARN) println(log)
  def error(log: => String) = if (logLevel <= LogLevel.ERROR) println(log)
  def fatal(log: => String) = if (logLevel <= LogLevel.FATAL) println(log)
}
