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
import l9g.webapp.smartcardfront.client.ApiClientService;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.service.DbAddressService;
import l9g.webapp.smartcardfront.db.service.DbPointOfSaleService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormPointOfSale;
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
public class AdminPointOfSaleController
{
  private final AdminService adminService;

  private final DbPointOfSaleService dbPointOfSaleService;
  
  private final DbAddressService dbAddressService;
  
  private final ApiClientService apiClientService;

  private static final String ACTIVE_PAGES = "pointOfSale";

  @GetMapping("/admin/pointOfSale")
  public String pointOfSaleHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("pointsOfSale", dbPointOfSaleService.findPointsOfSaleByTenant(session, principal));
    return "admin/pointOfSale";
  }

  @GetMapping("/admin/pointOfSale/{id}")
  public String pointOfSaleForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("pointOfSaleForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    model.addAttribute("addresses", dbAddressService.findAllAddresses(principal));
    model.addAttribute("creditCardReaders", apiClientService.findAllCreditCardReaders());

    if("add".equals(id))
    {
      FormPointOfSale formPointOfSale = new FormPointOfSale();
      formPointOfSale.setId("add");
      model.addAttribute("addPointOfSale", true);
      model.addAttribute("formPointOfSale", formPointOfSale);
      log.debug("formPointOfSale={}", formPointOfSale);
    }
    else
    {
      PosPointOfSale posPointOfSale = dbPointOfSaleService.findPointOfSaleById(id);
      FormPointOfSale formPointOfSale = FormPosMapper.INSTANCE.posPointOfSaleToFormPointOfSale(posPointOfSale);
      model.addAttribute("formPointOfSale", formPointOfSale);
    }
    return "admin/pointOfSaleForm";
  }

  @PostMapping("/admin/pointOfSale/{id}")
  public String pointOfSaleFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formPointOfSale") FormPointOfSale formPointOfSale,
    BindingResult bindingResult, Model model)
  {
    log.debug("pointOfSaleForm action {} for {}", id, principal.getPreferredUsername());
    List<PosPointOfSale> pointsOfSale = null; //dbPointOfSaleService.findPointOfSaleById(id, session, principal);
    model.addAttribute("pointsOfSale", pointsOfSale);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addPointOfSale", true);
      }
      model.addAttribute("formPointOfSale", formPointOfSale);
      return "admin/pointOfSaleForm";
    }

    try
    {
      log.debug("formPointOfSale = {}", formPointOfSale);
      redirectAttributes.addFlashAttribute("savedPointOfSale",
      dbPointOfSaleService.savePointOfSale(id, formPointOfSale, session, principal));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addPointOfSale", true);
      }
      model.addAttribute("formPointOfSale", formPointOfSale);
      model.addAttribute("savePointOfSaleError", t.getMessage());
      return "admin/pointOfSaleForm";
    }

    return "redirect:/admin/pointOfSale";
  }

  @GetMapping("/admin/pointOfSale/{id}/delete")
  public String pointOfSaleDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("pointOfSale delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedproduct",
      dbPointOfSaleService.deletePointOfSale(id));
    return "redirect:/admin/pointOfSale";
  }

}
