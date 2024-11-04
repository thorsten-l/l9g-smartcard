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

import l9g.webapp.smartcardfront.admin.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminTenantController
{
  private final AdminService adminService;

  private final TenantService tenantService;

  @GetMapping("/admin/tenant/{id}")
  public String tenantForm(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("tenantForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session);

    if("add".equals(id))
    {
      model.addAttribute("addTenant", true);
      model.addAttribute("formTenant", new PosTenant());
    }
    else
    {
      model.addAttribute("formTenant", tenantService.adminFindTenantById(id, principal));
    }
    return "admin/tenantForm";
  }

  @PostMapping("/admin/tenant/{id}")
  public String tenantFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formTenant") PosTenant formTenant,
    BindingResult bindingResult,
    Model model)
  {
    log.debug("tenantForm action {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      bindingResult.getAllErrors().forEach(error -> log.debug(" - Error: {}", error));
      log.debug("show errors....");

      if("add".equals(id))
      {
        model.addAttribute("addTenant", true);
      }
      model.addAttribute("formTenant", formTenant);
      return "admin/tenantForm";
    }

    redirectAttributes.addFlashAttribute("savedTenant",
      tenantService.adminSaveTenant(id, formTenant, principal));
    return "redirect:/admin/tenant";
  }

  @GetMapping("/admin/tenant/{id}/delete")
  public String tenantDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model)
  {
    log.debug("tenant delete {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session);
    redirectAttributes.addFlashAttribute("deletedTenant",
      tenantService.adminDeleteTenant(id, principal));
    return "redirect:/admin/tenant";
  }

  @GetMapping("/admin/tenant/{id}/select")
  public String tenantSelect(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("selectedTenant {} for {}", id,
      principal.getPreferredUsername());
    tenantService.adminSelectTenant(session, id, principal);
    adminService.generalModel(principal, model, session);
    return "redirect:/admin/home";
  }

}
