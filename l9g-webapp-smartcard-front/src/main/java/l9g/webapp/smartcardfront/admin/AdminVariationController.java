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
import l9g.webapp.smartcardfront.db.service.DbProductService;
import l9g.webapp.smartcardfront.db.service.DbVariationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  /*
  @GetMapping("/admin/product/{id}")
  public String productForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("productForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);

    List<PosCategory> categories = dbProductService.getAllCategories(session, principal);
    model.addAttribute("categories", categories);

    if("add".equals(id))
    {
      FormProduct formProduct = new FormProduct();
      formProduct.setId("add");
      if( ! categories.isEmpty())
      {
        formProduct.setCategoryId(categories.get(0).getId());
      }
      model.addAttribute("addProduct", true);
      model.addAttribute("formProduct", formProduct);
      log.debug("formProduct={}", formProduct);
    }
    else
    {
      PosProduct posProduct = dbProductService.ownerGetProductById(id, session, principal);
      FormProduct formProduct = FormPosMapper.INSTANCE.posProductToFormProduct(posProduct);
      model.addAttribute("formProduct", formProduct);
    }
    return "admin/productForm";
  }

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

  @GetMapping("/admin/product/{id}/delete")
  public String productDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("product delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedproduct",
      dbProductService.ownerDeleteProduct(id, session, principal));
    return "redirect:/admin/product";
  }
   */
}
