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
import l9g.webapp.smartcardfront.client.ApiClientService;
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
public class AdminCreditCardReaderController
{
  private final AdminService adminService;
  private final ApiClientService apiClientService;
  
  private static final String ACTIVE_PAGES = "creditcardreader";

  @GetMapping("/admin/creditcardreader")
  public String creditCardReaderHome(
    @AuthenticationPrincipal DefaultOidcUser principal, Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    model.addAttribute("creditCardReaders", apiClientService.findAllCreditCardReaders());
    return "admin/creditcardreader";
  }
/*
  @GetMapping("/admin/creditcardreader/{id}")
  public String creditCardReaderForm(@PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    log.debug("creditcardreaderForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    if("add".equals(id))
    {
      model.addAttribute("addCreditCardReader", true);
      model.addAttribute("formCreditCardReader", new FormCreditCardReader());
    }
    else
    {
      model.addAttribute("formCreditCardReader", 
        FormPosMapper.INSTANCE.posCreditCardReaderToFormCreditCardReader(
          dbCreditCardReaderService.adminFindCreditCardReaderById(id, principal)));
    }
    return "admin/creditcardreaderForm";
  }

  @PostMapping("/admin/creditcardreader/{id}")
  public String creditCardReaderFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formCreditCardReader") FormCreditCardReader formCreditCardReader,
    BindingResult bindingResult,
    Model model)
  {
    log.debug("creditcardreaderForm action {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      bindingResult.getAllErrors().forEach(error -> log.debug(" - Error: {}", error));
      log.debug("show errors....");

      if("add".equals(id))
      {
        model.addAttribute("addCreditCardReader", true);
      }
      model.addAttribute("formCreditCardReader", formCreditCardReader);
      return "admin/creditcardreaderForm";
    }

    redirectAttributes.addFlashAttribute("savedCreditCardReader",
      dbCreditCardReaderService.adminSaveCreditCardReader(id, formCreditCardReader, principal));
    return "redirect:/admin/creditcardreader";
  }

  @GetMapping("/admin/creditcardreader/{id}/delete")
  public String creditCardReaderDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model)
  {
    log.debug("creditcardreader delete {} for {}", id,
      principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    redirectAttributes.addFlashAttribute("deletedCreditCardReader",
      dbCreditCardReaderService.adminDeleteCreditCardReader(id, principal));
    return "redirect:/admin/creditcardreader";
  }
*/
}
