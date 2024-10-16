
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
package l9g.webapp.smartcardapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class represents the API controller for retrieving build information.
 * It is responsible for handling requests related to application version and
 * build process.
 * The controller exposes an endpoint at "/api/v1/buildinfo" and produces JSON
 * responses.
 *
 * The main method in this class is the "buildinfo" method, which retrieves the
 * build properties
 * and sorts them alphabetically by key. The sorted properties are then returned
 * as a LinkedHashMap
 * with the key-value pairs representing the build information.
 *
 * This class requires an instance of the BuildProperties class to be injected
 * via constructor
 * dependency injection.
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/buildinfo",
                produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Build Information",
     description = "Application version and build process")
public class ApiBuildinfoController
{
  /**
   * Retrieves the build information of the application.
   * This method returns a LinkedHashMap containing key-value pairs of build
   * properties,
   * such as version, time, and other metadata.
   *
   * @return a Map with build information.
   */
  @GetMapping()
  public Map<String, String> buildinfo(
    JwtAuthenticationToken jwtAuthenticationToken)
  {
    log.debug("buildinfo jwtAuthenticationToken={}", jwtAuthenticationToken);

    if( jwtAuthenticationToken != null )
    {
    jwtAuthenticationToken.getTokenAttributes()
      .forEach((key, value) ->
      {
        System.out.println(key + " : " + value);
      });
    }
    
    ///////////////////////////////////////////////////
    ArrayList<String> keys = new ArrayList<>();
    buildProperties.forEach(p -> keys.add(p.getKey()));
    Collections.sort(keys);
    LinkedHashMap<String, String> properties = new LinkedHashMap<>();
    for (String key : keys)
    {
      properties.put(key, buildProperties.get(key));
    }

    return properties;
  }

  /**
   * The build properties of the application.
   * This field holds information about the build, such as version, time, and
   * other metadata.
   * It is injected by Spring from the application's build properties.
   */
  private final BuildProperties buildProperties;
}
