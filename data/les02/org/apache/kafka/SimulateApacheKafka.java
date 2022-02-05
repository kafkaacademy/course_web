package org.apache.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulateApacheKafka {
  static String msg = "org.apache.kafka {} works";
  static Logger logger = LoggerFactory.getLogger(SimulateApacheKafka.class);

  public static void testLogging() {
    logger.error(msg , "error");
    logger.info(msg , "info");
    logger.warn(msg , "warn");
    logger.debug(msg , "debug");
  }
}
