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
package l9g.webapp.smartcardfront.db.service;

import jakarta.servlet.http.HttpSession;
import l9g.webapp.smartcardfront.db.PosCategoriesRepository;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
import l9g.webapp.smartcardfront.form.model.FormTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DbTenantService
{
  private static final String SESSION_POS_SELECTED_TENANT = "POS_SELECTED_TENANT";

  private final DbUserService dbUserService;

  private final DbAddressService dbAddressService;

  private final PosCategoriesRepository posCategoriesRepository;

  private final PosTenantsRepository posTenantsRepository;

  public PosTenant getSelectedTenant(HttpSession session, PosUser user)
  {
    PosTenant tenant;

    Object object = session.getAttribute(SESSION_POS_SELECTED_TENANT);

    if(user != null && user.getRole() == PosRole.POS_ADMINISTRATOR
      && object != null && object instanceof PosTenant)
    {
      log.debug("selected tenant found in session");
      tenant = (PosTenant)object;
    }
    else
    {
      log.debug("create new selected tenant for session");
      tenant = user.getTenant();
    }

    session.setAttribute(SESSION_POS_SELECTED_TENANT, tenant);

    return tenant;
  }

  public PosTenant getSelectedTenant(
    HttpSession session, DefaultOidcUser principal)
  {
    return getSelectedTenant(session, dbUserService.posUserFromPrincipal(principal));
  }

  public PosTenant adminFindTenantById(String id, DefaultOidcUser principal)
  {
    PosTenant posTenant = null;

    if(dbUserService.isAdmin(principal) && id != null)
    {
      posTenant = posTenantsRepository.findById(id).orElseThrow(()
        -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));
      log.debug("create or update a tenant: {}", posTenant.getName());
    }
    else
    {
      throw new AccessDeniedException("You are not allowed to create or update tenants.");
    }
    return posTenant;
  }

  public void adminSelectTenant(HttpSession session, String id, DefaultOidcUser principal)
  {
    if(dbUserService.isAdmin(principal) && id != null)
    {
      log.debug("setting tenannt in session.");
      PosTenant posTenant = posTenantsRepository.findById(id).orElseThrow(()
        -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));
      session.setAttribute(SESSION_POS_SELECTED_TENANT, posTenant);
    }
    else
    {
      throw new AccessDeniedException("You are not allowed to change your tenant.");
    }
  }

  public PosTenant adminSaveTenant(String id, FormTenant formTenant, DefaultOidcUser principal)
  {
    PosTenant posTenant;

    if(formTenant.getShorthand() != null && formTenant.getShorthand().isBlank())
    {
      formTenant.setShorthand(null);
    }

    if("add".equals(id))
    {
      log.debug("add new tenant");
      posTenant = new PosTenant(dbUserService.gecosFromPrincipal(principal), formTenant.getName());
    }
    else
    {
      posTenant = adminFindTenantById(id, principal);
    }

    posTenant.setModifiedBy(dbUserService.gecosFromPrincipal(principal));
    posTenant.setName(formTenant.getName());
    posTenant.setShorthand(formTenant.getShorthand());

    if(formTenant.getAddressId() != null &&  ! formTenant.getAddressId().isBlank())
    {
      posTenant.setAddress(dbAddressService.adminFindAddressById(formTenant.getAddressId(), principal));
    }

    log.debug("posTenant = {}", posTenant);
    posTenant = posTenantsRepository.saveAndFlush(posTenant);

    if("add".equals(id))
    {
      log.debug("add default categoy for tenant {}", posTenant.getName());
      PosCategory posCategory =
        new PosCategory(
          dbUserService.gecosFromPrincipal(principal),
          posTenant, "Default", true, true);
      posCategoriesRepository.saveAndFlush(posCategory);
    }

    return posTenant;
  }

  public PosTenant adminDeleteTenant(String id, DefaultOidcUser principal)
  {
    log.debug("delete tenant: {} with principal {}", id, principal.getName());
    PosTenant tenant = posTenantsRepository.findById(id).orElseThrow(
      () -> new AccessDeniedException("Unknown tenant id"));

    if(dbUserService.isAdmin(principal))
    {
      PosTenant systemTenant = posTenantsRepository.findByName(DbService.KEY_SYSTEM_TENANT).get();

      if(systemTenant.getId().equals(id))
      {
        throw new AccessDeniedException("Deleting system tenant is forbidden");
      }
      else
      {
        posTenantsRepository.deleteById(id);
        posTenantsRepository.flush();
      }
    }

    return tenant;
  }

  public PosTenant checkTenantOwner(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = getSelectedTenant(session, principal);
    PosUser user = dbUserService.posUserFromPrincipal(principal);
    if(dbUserService.isAdmin(principal)
      || (dbUserService.isOwner(principal)
      && tenant.getId().equals(user.getTenant().getId())))
    {
      log.debug("access granted on properties");
    }
    else
    {
      throw new AccessDeniedException("No permissions.");
    }
    return tenant;
  }

}
