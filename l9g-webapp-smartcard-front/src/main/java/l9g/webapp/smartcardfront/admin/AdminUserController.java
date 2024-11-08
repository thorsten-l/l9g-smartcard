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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import l9g.webapp.smartcardfront.db.service.DbUserService;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormUser;
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
public class AdminUserController
{
  private final AdminService adminService;

  private final DbTenantService dbTenantService;

  private final DbUserService dbUserService;

  @GetMapping("/admin/user")
  public String userHome(
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    adminService.generalModel(principal, model, session);

    model.addAttribute("users",
      dbUserService.ownerGetUsersByTenant(session, principal,
        dbTenantService.checkTenantOwner(session, principal)));
    return "admin/user";
  }

  @GetMapping("/admin/user/{id}")
  public String userForm(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    HttpSession session)
  {
    log.debug("userForm {} for {}", id, principal.getPreferredUsername());
    adminService.generalModel(principal, model, session);

    PosTenant tenant = dbTenantService.checkTenantOwner(session, principal);

    boolean isAdmin = dbUserService.isAdmin(principal);
    List<String> roles = new ArrayList<>();
    for(PosRole role : PosRole.values())
    {
      if(role != PosRole.POS_ADMINISTRATOR || isAdmin)
      {
        roles.add(role.name());
      }
    }

    Collections.sort(roles);
    model.addAttribute("roles", roles);

    if("add".equals(id))
    {

      FormUser formUser =
        new FormUser("add", tenant.getId(), "", "", PosRole.POS_CASHIER.name());

      log.debug("formUser={}", formUser);
      model.addAttribute("addUser", true);
      model.addAttribute("formUser", formUser);
    }
    else
    {      
      model.addAttribute("formUser",
        FormPosMapper.INSTANCE.posUserToFormUser(
          dbUserService.ownerGetUserById(id, principal, tenant)));
    }
    return "admin/userForm";
  }

  @PostMapping("/admin/user/{id}")
  public String userFormAction(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal,
    @Valid @ModelAttribute("formUser") FormUser formUser,
    BindingResult bindingResult, Model model)
  {
    log.debug("userForm action {} for {}", id, principal.getPreferredUsername());

    if(bindingResult.hasErrors())
    {
      log.debug("Form error: {}", bindingResult);
      adminService.generalModel(principal, model, session);
      if("add".equals(id))
      {
        model.addAttribute("addUser", true);
      }
      model.addAttribute("formUser", formUser);
      return "admin/userForm";
    }

    try
    {
      log.debug("formUser = {}", formUser);
      redirectAttributes.addFlashAttribute("savedUser",
        dbUserService.ownerSaveUser(id, formUser, session, principal,
          dbTenantService.checkTenantOwner(session, principal)));
    }
    catch(Throwable t)
    {
      adminService.generalModel(principal, model, session);
      if("add".equals(id))
      {
        model.addAttribute("addUser", true);
      }
      model.addAttribute("formUser", formUser);
      model.addAttribute("saveUserError", t.getMessage());
      return "admin/userForm";
    }

    return "redirect:/admin/user";
  }

  @GetMapping("/admin/user/{id}/delete")
  public String userDelete(
    RedirectAttributes redirectAttributes,
    HttpSession session,
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("user delete {} for {}", id, principal.getPreferredUsername());
    redirectAttributes.addFlashAttribute("deletedUser",
      dbUserService.ownerDeleteUser(id, session, principal,
        dbTenantService.checkTenantOwner(session, principal)));
    return "redirect:/admin/user";
  }

}
