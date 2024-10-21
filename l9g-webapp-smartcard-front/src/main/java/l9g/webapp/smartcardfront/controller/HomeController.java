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
package l9g.webapp.smartcardfront.controller;

import l9g.webapp.smartcardfront.client.ApiMonitorService;
import l9g.webapp.smartcardfront.config.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController
{
  private final ApiMonitorService monitorService;
  private final UserRoleService userRoleService;
  
  @GetMapping("/")
  public String home(
    @AuthenticationPrincipal DefaultOidcUser principal, Model model)
  {
    // userRoleService.printUserRoles();
    String pointOfSalesName = monitorService.pointOfSalesName();
    log.debug("pointOfSalesName={}",pointOfSalesName);
    model.addAttribute("pointOfSalesName", pointOfSalesName);
    model.addAttribute("fullname", principal.getFullName());
    return "home";
  }
}
