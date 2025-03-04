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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import l9g.webapp.smartcardfront.db.service.DbAddressService;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormTenant;
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

  private final DbTenantService dbTenantService;

  private final DbAddressService dbAddressService;

  private static final String ACTIVE_PAGES = "tenant";

  @GetMapping("/admin/tenant")
  public String tenantHome(
    @AuthenticationPrincipal DefaultOidcUser principal, Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    return "admin/tenant";
  }

  @GetMapping("/admin/tenant/{id}")
  public String tenantForm(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("tenantForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    model.addAttribute("addresses", dbAddressService.findAllAddresses(principal));

    if("add".equals(id))
    {
      model.addAttribute("addTenant", true);
      model.addAttribute("formTenant", new FormTenant());
    }
    else
    {
      model.addAttribute("formTenant",
        FormPosMapper.INSTANCE.posTenantToFormTenant(
          dbTenantService.adminFindTenantById(id, principal)));
    }
    return "admin/tenantForm";
  }

  @PostMapping("/admin/tenant/{id}")
  public String tenantFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formTenant") FormTenant formTenant,
    BindingResult bindingResult,
    Model model)
  {
    log.debug("tenantForm action {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("addresses", dbAddressService.findAllAddresses(principal));

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
      dbTenantService.adminSaveTenant(id, formTenant, principal));
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
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    redirectAttributes.addFlashAttribute("deletedTenant",
      dbTenantService.adminDeleteTenant(id, principal));
    return "redirect:/admin/tenant";
  }

  @GetMapping("/admin/tenant/{id}/select")
  public String tenantSelect(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session, HttpServletRequest request)
  {
    log.debug("selectedTenant {} for {}", id,
      principal.getPreferredUsername());
    dbTenantService.adminSelectTenant(session, id, principal);
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    String referrer = request.getHeader("Referer");

    log.debug("referrer={}", referrer);

    if(referrer == null || referrer.isEmpty())
    {
      referrer = "/admin/home";
    }
    else
    {
      int index = referrer.indexOf("://");
      if(index >= 0)
      {
        index += 3;
        for(int i = 0; i < 3; i ++)
        {
          index = referrer.indexOf("/", index + 1);
          if(index <= 0)
          {
            break;
          }
        }
        referrer = (index >= 0) ? referrer.substring(0, index) : referrer;
      }
    }

    log.debug("redirect=redirect:{}", referrer);

    return "redirect:" + referrer;
  }

}
