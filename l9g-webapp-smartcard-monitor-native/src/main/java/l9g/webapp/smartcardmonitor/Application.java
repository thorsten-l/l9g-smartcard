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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * The main entry point for the Smartcard Monitor web application.
 * This class is annotated with {@code @SpringBootApplication} to indicate a Spring Boot application.
 * It excludes the {@code UserDetailsServiceAutoConfiguration} class from auto-configuration.
 * 
 * The {@code main} method uses {@code SpringApplication.run} to launch the application.
 * 
 * Annotations:
 * {@code @SpringBootApplication} - Indicates a Spring Boot application.
 * {@code @Slf4j} - Provides a logger instance for logging purposes.
 */
@SpringBootApplication(exclude =
{
  UserDetailsServiceAutoConfiguration.class
})
@Slf4j
public class Application
{
  public static void main(String[] args)
  {
    SpringApplication.run(Application.class, args);
  }
}
