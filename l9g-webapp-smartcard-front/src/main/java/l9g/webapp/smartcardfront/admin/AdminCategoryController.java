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
import l9g.webapp.smartcardfront.db.service.DbCategoryService;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormCategory;
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
public class AdminCategoryController
{
  private final AdminService adminService;

  private final DbTenantService dbTenantService;

  private final DbCategoryService dbCategoryService;
  
  

  @GetMapping("/admin/category")
  public String categoryHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session);
    model.addAttribute("categories", dbCategoryService.ownerGetCategoriesByTenant(session, principal));
    return "admin/category";
  }

  @GetMapping("/admin/category/{id}")
  public String categoryForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("categoryForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session);
    if("add".equals(id))
    {
      FormCategory formCategory =
        new FormCategory();
      formCategory.setTenantId(dbTenantService.getSelectedTenant(session, principal).getId());
      formCategory.setId("add");
      log.debug("formCategory={}", formCategory);
      model.addAttribute("addCategory", true);
      model.addAttribute("formCategory", formCategory);
    }
    else
    {
      model.addAttribute("formCategory",
        FormPosMapper.INSTANCE.posCategoryToFormCategory(
          dbCategoryService.ownerGetCategoryById(id, session, principal)));
    }
    return "admin/categoryForm";
  }

  @PostMapping("/admin/category/{id}")
  public String categoryFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formCategory") FormCategory formCategory,
    BindingResult bindingResult, Model model)
  {
    log.debug("categoryForm action {} for {}", id, principal.getPreferredUsername());

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session);
      if("add".equals(id))
      {
        model.addAttribute("addCategory", true);
      }
      model.addAttribute("formCategory", formCategory);
      return "admin/categoryForm";
    }

    try
    {
      log.debug("formCategory = {}", formCategory);
      redirectAttributes.addFlashAttribute("savedCategory",
        dbCategoryService.ownerSaveCategory(id, formCategory, session, principal));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session);
      if("add".equals(id))
      {
        model.addAttribute("addCategory", true);
      }
      model.addAttribute("formCategory", formCategory);
      model.addAttribute("saveCategoryError", t.getMessage());
      return "admin/categoryForm";
    }

    return "redirect:/admin/category";
  }

  @GetMapping("/admin/category/{id}/delete")
  public String categoryDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("category delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedCategory",
      dbCategoryService.ownerDeleteCategory(id, session, principal));
    return "redirect:/admin/category";
  }

}
