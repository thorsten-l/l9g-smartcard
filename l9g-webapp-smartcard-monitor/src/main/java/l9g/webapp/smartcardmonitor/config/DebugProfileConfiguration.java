/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.webapp.smartcardmonitor.config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Configuration
@Profile("debug")
@Slf4j
public class DebugProfileConfiguration
{
  private String loggingFilename;

  /**
   * Configuration class for setting up the debug profile.
   * This class redirects all console output (System.out and System.err) to a specified log file.
   *
   * @param environment The environment from which to retrieve the logging file name property.
   *                    If the property "logging.file.name" is not set, it defaults to "smartcard-monitor.log".
   *
   * The constructor performs the following actions:
   * - Retrieves the logging file name from the environment.
   * - Logs the initialization of the DebugProfileConfiguration.
   * - Logs the name of the logging file.
   * - Redirects all console output to the specified log file.
   *
   * If an IOException occurs during the redirection of console output, it logs an error message.
   */
  public DebugProfileConfiguration(Environment environment)
  {
    this.loggingFilename = environment.getProperty("logging.file.name", "smartcard-monitor.log");
    log.info("--- DebugProfileConfiguration");
    log.info("  - logging file name : {}", loggingFilename);
    log.info("  - redirecting all console output to logfile.");

    try
    {
      FileOutputStream fileOut = new FileOutputStream(loggingFilename, true);
      PrintStream printStream = new PrintStream(fileOut, true);
      System.setOut(printStream);
      System.setErr(printStream);
    }
    catch(IOException e)
    {
      log.error("Error when redirecting System.out and System.err to the log file", e);
    }
  }

}
