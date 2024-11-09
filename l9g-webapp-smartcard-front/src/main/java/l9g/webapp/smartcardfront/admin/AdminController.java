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

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
public class AdminController
{
  private final AdminService adminService;
  
  private static final String ACTIVE_PAGES = "home";

  @GetMapping("/admin/home")
  public String adminHome(@AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    return "admin/home";
  }

  @GetMapping("/ui/{category}/{page}")
  public String ui(
    Authentication authentication,
    @PathVariable String category,
    @PathVariable String page,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("uiGET({}/{})", category, page);

    model.addAttribute("layoutCategory", category);
    model.addAttribute("layoutPage", page);
    model.addAttribute("layoutFragment", "ui/" + category + "/" + page);

    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    return "ui/" + category + "/" + page;
  }

}
