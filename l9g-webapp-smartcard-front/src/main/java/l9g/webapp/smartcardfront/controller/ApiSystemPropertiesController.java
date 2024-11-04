/*
 * Copyright 2024 Thorsten Ludewig <t.ludewig@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package l9g.webapp.smartcardfront.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the API controller for retrieving system information.
 * It provides an endpoint for getting the current runtime system information.
 * The endpoint is accessible through the path "/api/v1/systeminfo" and returns
 * the system properties in JSON format.
 *
 * The class is annotated with the "@RestController" annotation, indicating that
 * it handles RESTful requests. It also has the "@RequestMapping" annotation
 * specifying the base path for all the endpoints in this controller.
 *
 * The class has a single method, "systeminfo()", which is annotated with the
 * "@GetMapping" annotation to handle GET requests to the "/api/v1/systeminfo"
 * endpoint. This method retrieves the system properties using the
 * "System.getProperties()" method, filters out certain properties based on
 * their keys, and returns the remaining properties as a LinkedHashMap in JSON
 * format.
 *
 * The class is also annotated with the "@Tag" annotation from the Swagger API
 * documentation library, specifying that it belongs to the "System Information"
 * tag with a description of "Current runtime system information".
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/system-properties",
                produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiSystemPropertiesController
{
  /**
   * Retrieves the system information of the application.
   * This method returns a LinkedHashMap containing key-value pairs of system
   * properties,
   * such as operating system, Java version, and other relevant metadata.
   *
   * @return a LinkedHashMap with system information.
   */
  @GetMapping()
  public Map<String, String> systemPropertiesMap()
  {
    LinkedHashMap<String, String> properties = new LinkedHashMap<>();
    String[] keys = System.getProperties().keySet().toArray(String[]::new);
    Arrays.sort(keys);
    for (String key : keys)
    {
      if (key.startsWith("java.")
        || key.startsWith("os.")
        || key.startsWith("sun.")
        || key.startsWith("file.")
        || key.startsWith("native")
        || key.startsWith("user.lang")
        || key.startsWith("user.time")
        || key.startsWith("user.coun"))
      {
        if (!key.endsWith("path")
          && !key.endsWith(".tmpdir"))
        {
          properties.put(key, System.getProperty(key));
        }
      }
    }
    return properties;
  }
}
