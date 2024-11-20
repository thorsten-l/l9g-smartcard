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
import l9g.webapp.smartcardfront.db.service.DbAddressService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormAddress;
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
public class AdminAddressController
{
  private final AdminService adminService;

  private final DbAddressService dbAddressService;
  
  private static final String ACTIVE_PAGES = "address";

  @GetMapping("/admin/address")
  public String addressHome(
    @AuthenticationPrincipal DefaultOidcUser principal, Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("addresses", dbAddressService.findAllAddresses(principal));
    return "admin/address";
  }

  @GetMapping("/admin/address/{id}")
  public String addressForm(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("addressForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    if("add".equals(id))
    {
      model.addAttribute("addAddress", true);
      model.addAttribute("formAddress", new FormAddress());
    }
    else
    {
      model.addAttribute("formAddress", 
        FormPosMapper.INSTANCE.posAddressToFormAddress(
          dbAddressService.adminFindAddressById(id, principal)));
    }
    return "admin/addressForm";
  }

  @GetMapping("/admin/address/{id}/clone")
  public String addressClone(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("addressClone {} for {}", id, principal.getPreferredUsername());
    dbAddressService.adminCloneAddressById(id, principal);
    return "redirect:/admin/address";
  }

  @PostMapping("/admin/address/{id}")
  public String addressFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formAddress") FormAddress formAddress,
    BindingResult bindingResult,
    Model model)
  {
    log.debug("addressForm action {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      bindingResult.getAllErrors().forEach(error -> log.debug(" - Error: {}", error));
      log.debug("show errors....");

      if("add".equals(id))
      {
        model.addAttribute("addAddress", true);
      }
      model.addAttribute("formAddress", formAddress);
      return "admin/addressForm";
    }

    redirectAttributes.addFlashAttribute("savedAddress",
      dbAddressService.adminSaveAddress(id, formAddress, principal));
    return "redirect:/admin/address";
  }

  @GetMapping("/admin/address/{id}/delete")
  public String addressDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model)
  {
    log.debug("address delete {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    redirectAttributes.addFlashAttribute("deletedAddress",
      dbAddressService.adminDeleteAddress(id, principal));
    return "redirect:/admin/address";
  }
}
