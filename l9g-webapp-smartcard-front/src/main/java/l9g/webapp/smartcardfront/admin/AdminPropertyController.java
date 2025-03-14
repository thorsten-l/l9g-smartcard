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
import jakarta.validation.Valid;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import l9g.webapp.smartcardfront.db.service.DbPropertyService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormProperty;
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
public class AdminPropertyController
{
  private final AdminService adminService;

  private final DbTenantService dbTenantService;

  private final DbPropertyService dbPropertyService;

  private static final String ACTIVE_PAGES = "property";

  @GetMapping("/admin/property")
  public String propertyHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("properties", dbPropertyService.ownerGetPropertiesByTenant(session, principal));
    return "admin/property";
  }

  @GetMapping("/admin/property/{id}")
  public String propertyForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("propertyForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    if("add".equals(id))
    {
      FormProperty formProperty =
        new FormProperty("add",
          dbTenantService.checkTenantOwner(session, principal).getId(),
          "", "", false);
      log.debug("formProperty={}", formProperty);
      model.addAttribute("addProperty", true);
      model.addAttribute("formProperty", formProperty);
    }
    else
    {
      model.addAttribute("formProperty",
        FormPosMapper.INSTANCE.posPropertyToFormProperty(
          dbPropertyService.ownerGetPropertyById(id, session, principal)));
    }
    return "admin/propertyForm";
  }

  @PostMapping("/admin/property/{id}")
  public String propertyFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formProperty") FormProperty formProperty,
    BindingResult bindingResult, Model model)
  {
    log.debug("propertyForm action {} for {}", id, principal.getPreferredUsername());

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addProperty", true);
      }
      model.addAttribute("formProperty", formProperty);
      return "admin/propertyForm";
    }

    try
    {
      log.debug("formProperty = {}", formProperty);
      redirectAttributes.addFlashAttribute("savedProperty",
        dbPropertyService.ownerSaveProperty(id, formProperty, session, principal));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addProperty", true);
      }
      model.addAttribute("formProperty", formProperty);
      model.addAttribute("savePropertyError", t.getMessage());
      return "admin/propertyForm";
    }

    return "redirect:/admin/property";
  }

  @GetMapping("/admin/property/{id}/delete")
  public String propertyDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("property delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedProperty",
      dbPropertyService.ownerDeleteProperty(id, session, principal));
    return "redirect:/admin/property";
  }

}
