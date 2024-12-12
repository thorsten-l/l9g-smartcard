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
package l9g.webapp.smartcardmonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * The main entry point for the Smartcard Monitor web application. This class is
 * annotated with {@code @SpringBootApplication} to indicate a Spring Boot
 * application. It excludes the {@code UserDetailsServiceAutoConfiguration}
 * class from auto-configuration.
 *
 * The {@code main} method uses {@code SpringApplication.run} to launch the
 * application.
 *
 * Annotations: {@code @SpringBootApplication} - Indicates a Spring Boot
 * application. {@code @Slf4j} - Provides a logger instance for logging
 * purposes.
 */
@SpringBootApplication(exclude =
{
  UserDetailsServiceAutoConfiguration.class
})
@Slf4j
public class Application
{

  private static final String APP_JAR_FILENAME =
    "l9g-webapp-smartcard-monitor.jar";

  private static boolean osWindows;

  public static boolean isOsWindows()
  {
    return osWindows;
  }

  public static void main(String[] args)
    throws IOException, InterruptedException
  {
    osWindows = System.getProperty("os.name")
      .toLowerCase().startsWith("windows");

    if(osWindows &&  ! "true".equals(System.getProperty("monitor.worker")))
    {
      String javaHome = System.getProperty("java.home");
      String userDir = System.getProperty("user.dir");

      System.out.println("OS = Windows");
      System.out.println("java.home = " + javaHome);
      System.out.println("user.dir = " + userDir);

      String appJarFilename = null;
      String filename = userDir + File.separator + APP_JAR_FILENAME;

      if(new File(filename).exists())
      {
        appJarFilename = filename;
      }
      else
      {
        filename = userDir + File.separator + "target"
          + File.separator + APP_JAR_FILENAME;
        if(new File(filename).exists())
        {
          appJarFilename = filename;
        }
      }

      if(appJarFilename == null)
      {
        System.err.println("JAR file not found!");
        System.exit(-1);
      }

      System.out.println("app jar filename=" + appJarFilename);

      ArrayList<String> command = new ArrayList<>();
      command.add(javaHome + File.separator + "bin" + File.separator + "java.exe");
      command.add("-Dmonitor.worker=true");
      command.add("-jar");
      command.add(appJarFilename);
      command.addAll(Arrays.asList(args));

      System.out.print("command: ");
      command.forEach(c -> System.out.print(c + " "));
      System.out.println();

      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.inheritIO();
      int exitCode;

      do
      {
        System.out.println("Starting monitor worker");
        Process workerProcess = processBuilder.start();
        exitCode = workerProcess.waitFor();
      }
      while(exitCode < -99);
    }
    else
    {
      SpringApplication.run(Application.class, args);
    }
  }
}
