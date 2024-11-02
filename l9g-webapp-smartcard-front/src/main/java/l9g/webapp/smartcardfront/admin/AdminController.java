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
import java.util.List;
import java.util.Locale;
import l9g.webapp.smartcardfront.config.UserService;
import l9g.webapp.smartcardfront.db.PosDtoMapper;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController
{
  private static final String SESSION_POS_SELECTED_TENANT = "POS_SELECTED_TENANT";

  private final UserService userService;

  private final PosTenantsRepository posTenantsRepository;

  private void generalModel(DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("preferred_username={}", principal.getPreferredUsername());

    List<PosTenant> tenants;

    if(userService.isAdmin(principal))
    {
      log.debug("{} is administrator!", principal.getName());
      tenants = posTenantsRepository.findAllByOrderByNameAsc();
    }
    else
    {
      log.debug("{} is NOT administrator!", principal.getName());
      tenants = List.of();
    }

    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute("principal", principal);
    model.addAttribute("locale", locale.toString());
    model.addAttribute("selectedTenant",
      userService.getSelectedTenant(session, principal));
    model.addAttribute("tenants", tenants);
  }

  @GetMapping("/admin/{page}")
  public String pos(@PathVariable String page,
    @AuthenticationPrincipal DefaultOidcUser principal, Model model,
    HttpSession session)
  {
    generalModel(principal, model, session);
    return "admin/" + page;
  }

  @GetMapping("/admin/tenant/select/{id}")
  public String selectTenant(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("selectedTenant {} for {}", id,
      principal.getPreferredUsername());

    if(userService.isAdmin(principal) && id != null)
    {
      log.debug("setting tenannt in session.");
      PosTenant posTenant = posTenantsRepository.findById(id).orElseThrow(()
        -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));
      session.setAttribute(SESSION_POS_SELECTED_TENANT, posTenant);
    }
    else
    {
      throw new AccessDeniedException("You are not allowed to change your tenant.");
    }

    generalModel(principal, model, session);
    return "redirect:/admin/home";
  }

}
