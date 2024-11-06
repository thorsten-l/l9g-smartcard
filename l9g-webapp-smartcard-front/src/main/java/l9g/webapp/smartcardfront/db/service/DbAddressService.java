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

import java.util.List;
import l9g.webapp.smartcardfront.db.PosAddressesRepository;
import l9g.webapp.smartcardfront.db.model.PosAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DbAddressService
{

  private final DbUserService userService;

  private final PosAddressesRepository posAddressesRepository;
  
  
  
  public List<PosAddress> findAllAddresses(DefaultOidcUser principal){
  
    if(userService.isAdmin(principal))
    {
      return posAddressesRepository.findAll();
    }
    else
    {
      throw new AccessDeniedException("Permission denied.");
    }
  }

  /*
  public PosTenant adminFindTenantById(String id, DefaultOidcUser principal)
  {
    PosTenant posTenant = null;

    if(userService.isAdmin(principal) && id != null)
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
    if(userService.isAdmin(principal) && id != null)
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
      posTenant = new PosTenant(userService.gecosFromPrincipal(principal), formTenant.getName());
    }
    else
    {
      posTenant = adminFindTenantById(id, principal);
    }
    
    posTenant.setModifiedBy(userService.gecosFromPrincipal(principal));
    posTenant.setName(formTenant.getName());
    posTenant.setShorthand(formTenant.getShorthand());
    log.debug("posTenant = {}", posTenant);
    return posTenantsRepository.saveAndFlush(posTenant);
  }

  public PosTenant adminDeleteTenant(String id, DefaultOidcUser principal)
  {
    log.debug("delete tenant: {} with principal {}", id, principal.getName());
    PosTenant tenant = posTenantsRepository.findById(id).orElseThrow(
      () -> new AccessDeniedException("Unknown tenant id"));

    if(userService.isAdmin(principal))
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
    PosUser user = userService.posUserFromPrincipal(principal);
    if(userService.isAdmin(principal)
      || (userService.isOwner(principal)
      && tenant.getId().equals(user.getTenant().getId())))
    {
      log.debug("access granted on properties");
    }
    else
    {
      throw new AccessDeniedException("No permissions on properties.");
    }
    return tenant;
  }
*/
}
