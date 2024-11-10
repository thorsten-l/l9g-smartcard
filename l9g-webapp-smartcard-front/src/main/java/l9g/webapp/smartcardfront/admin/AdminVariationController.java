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
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.db.service.DbProductService;
import l9g.webapp.smartcardfront.db.service.DbVariationService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormProduct;
import l9g.webapp.smartcardfront.form.model.FormVariation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminVariationController
{
  private final AdminService adminService;

  private final DbProductService dbProductService;

  private final DbVariationService dbVariationService;

  private static final String ACTIVE_PAGES = "product";

  @GetMapping("/admin/product/{productId}/variation")
  public String productHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    @PathVariable String productId,
    Model model,
    HttpSession session)
  {
    log.debug("productId={}", productId);
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    PosProduct posProduct = dbProductService.ownerGetProductById(productId, session, principal);
    log.debug("posProduct={}", posProduct);
    model.addAttribute("product", posProduct);
    return "admin/variation";
  }

  @GetMapping("/admin/product/{productId}/variation/{id}")
  public String productForm(
    @PathVariable String productId,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("productForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    PosProduct posProduct = dbProductService.ownerGetProductById(productId, session, principal);
    model.addAttribute("product", posProduct);
    FormVariation formVariation;

    if("add".equals(id))
    {
      formVariation = new FormVariation("add", productId, null, 0, 0, false);
      model.addAttribute("addVariation", true);
    }
    else
    {
      PosVariation posVariation = dbVariationService.ownerGetVariationById(id, session, principal);
      formVariation = FormPosMapper.INSTANCE.posVariationToFormVariation(posVariation);
    }

    model.addAttribute("formVariation", formVariation);
    log.debug("formVariation={}", formVariation);
    return "admin/variationForm";
  }

  /*

  @PostMapping("/admin/product/{id}")
  public String productFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formProduct") FormProduct formProduct,
    BindingResult bindingResult, Model model)
  {
    log.debug("productForm action {} for {}", id, principal.getPreferredUsername());
    List<PosCategory> categories = dbProductService.getAllCategories(session, principal);
    model.addAttribute("categories", categories);

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addProduct", true);
      }
      model.addAttribute("formProduct", formProduct);
      return "admin/productForm";
    }

    try
    {
      log.debug("formProduct = {}", formProduct);
      redirectAttributes.addFlashAttribute("savedProduct",
        dbProductService.ownerSaveProduct(id, formProduct, session, principal));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session, ACTIVE_PAGES);
      if("add".equals(id))
      {
        model.addAttribute("addProduct", true);
      }
      model.addAttribute("formProduct", formProduct);
      model.addAttribute("saveProductError", t.getMessage());
      return "admin/productForm";
    }

    return "redirect:/admin/product";
  }
   */
  @GetMapping("/admin/product/{productId}/variation/{id}/delete")
  public String productDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String productId,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("delete variation {} of product {} for {}",
      productId, id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedVariation",
      dbVariationService.ownerDeleteVariationById(id, session, principal));
    return "redirect:/admin/product/" + productId + "/variation";
  }

}
