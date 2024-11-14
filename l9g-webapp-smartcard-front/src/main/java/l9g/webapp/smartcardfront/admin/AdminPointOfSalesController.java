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
import java.util.List;
import l9g.webapp.smartcardfront.db.model.PosPointOfSales;
import l9g.webapp.smartcardfront.db.service.DbPointOfSalesService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormPointOfSales;
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
public class AdminPointOfSalesController
{
  private final AdminService adminService;

  private final DbPointOfSalesService dbPointOfSalesService;

  private static final String ACTIVE_PAGES = "pointOfSales";

  @GetMapping("/admin/pointOfSales")
  public String pointOfSaleHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("pointOfSales", dbPointOfSalesService.ownerGetPointOfSalesByTenantAndAddress(session, principal));
    return "admin/pointOfSales";
  }

  @GetMapping("/admin/pointOfSales/{id}")
  public String pointOfSalesForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("pointOfSalesForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    List<PosPointOfSales> pointsOfSales = dbPointOfSalesService.ownerGetPointOfSalesByTenantAndAddress(session, principal);
    model.addAttribute("pointOfSales", pointsOfSales);

    if("add".equals(id))
    {
      FormPointOfSales formPointOfSales = new FormPointOfSales();
      formPointOfSales.setId("add");
      if( ! pointsOfSales.isEmpty())
      {
        formPointOfSales.setId(pointsOfSales.get(0).getId());
      }
      model.addAttribute("addPointOfSales", true);
      model.addAttribute("formPointOfSales", formPointOfSales);
      log.debug("formPointOfSales={}", formPointOfSales);
    }
    else
    {
      PosPointOfSales posPointOfSales = dbPointOfSalesService.ownerGetPointOfSalesById(id, session, principal);
      FormPointOfSales formPointOfSales = FormPosMapper.INSTANCE.posPointOfSaleToFormPointOfSales(posPointOfSales);
      model.addAttribute("formPointOfSales", formPointOfSales);
    }
    return "admin/pointOfSalesForm";
  }

  @PostMapping("/admin/pointOfSales/{id}")
  public String pointOfSalesFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formPointOfSales") FormPointOfSales formPointOfSales,
    BindingResult bindingResult, Model model)
  {
    log.debug("pointOfSalesForm action {} for {}", id, principal.getPreferredUsername());
    List<PosPointOfSales> pointsOfSales = dbPointOfSalesService.ownerGetPointOfSalesById(id, session, principal);
    model.addAttribute("pointsOfSales", pointsOfSales);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addPointOfSales", true);
      }
      model.addAttribute("formPointOfSales", formPointOfSales);
      return "admin/pointsOfSalesForm";
    }

    try
    {
      log.debug("formPointOfSales = {}", formPointOfSales);
      redirectAttributes.addFlashAttribute("savedPointOfSales",
        dbPointOfSalesService.ownerSavePointOfSales(id, formPointOfSales, session, principal));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addPointOfSales", true);
      }
      model.addAttribute("formPointOfSales", formPointOfSales);
      model.addAttribute("savePointOfSalesError", t.getMessage());
      return "admin/pointOfSalesForm";
    }

    return "redirect:/admin/pointOfSales";
  }

  @GetMapping("/admin/pointOfSales/{id}/delete")
  public String pointOfSalesDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("pointOfSales delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedproduct",
      dbPointOfSalesService.ownerDeletePointOfSales(id, session, principal));
    return "redirect:/admin/pointOfSales";
  }

}
