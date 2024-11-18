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
package l9g.webapp.smartcardfront.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import l9g.webapp.smartcardfront.client.ApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminApiTestController
{
  private final AdminService adminService;

  private final ApiClientService apiClientService;

  private static final String ACTIVE_PAGES = "apitest";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @PostConstruct
  private void initialize()
  {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @GetMapping("/admin/apitest/{id}")
  public String apiTests(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("apiTests '{}' for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    String testResult = null;

    switch(id)
    {
      case "me" ->
        testResult = apiClientService.sumupMerchantsMe();
    }

    if(testResult != null)
    {
      try
      {
        String prettyJson = objectMapper.writeValueAsString(objectMapper.readTree(testResult));
        log.debug("Test result for '{}' : \n{}", id, prettyJson);
      }
      catch(Exception e)
      {
        log.error("Failed to pretty print JSON: {}", testResult, e);
      }
    }
    else
    {
      log.debug("Test result = null");
    }
    return "admin/apitest";
  }

}
