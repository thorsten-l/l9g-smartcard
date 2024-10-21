
/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
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
package l9g.webapp.smartcardmonitor.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/posname",
                produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiPosNameController
{
  @Value("${app.posname}")
  private String appPosName;
  
  /**
   * Retrieves the build information of the application.
   * This method returns a LinkedHashMap containing key-value pairs of build
   * properties,
   * such as version, time, and other metadata.
   *
   * @return a Map with build information.
   */
  @GetMapping()
  public Map<String, String> posName()
  {
    log.debug("posName");
    
    LinkedHashMap<String, String> properties = new LinkedHashMap<>();
    properties.put("posname", appPosName);

    return properties;
  }
}
