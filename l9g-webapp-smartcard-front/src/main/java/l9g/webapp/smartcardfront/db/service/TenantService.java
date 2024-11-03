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
import l9g.webapp.smartcardfront.db.PosPosMapper;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
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
public class TenantService
{
  private static final String SESSION_POS_SELECTED_TENANT = "POS_SELECTED_TENANT";

  private final UserService userService;

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
    return getSelectedTenant(session, userService.posUserFromPrincipal(principal));
  }

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

  public PosTenant adminSaveTenant(String id, PosTenant tenant, DefaultOidcUser principal)
  {
    PosTenant posTenant;
    
    if ( tenant.getShorthand() != null && tenant.getShorthand().isBlank())
    {
      tenant.setShorthand(null);
    }
    
    if("add".equals(id))
    {
      log.debug("add new tenant");
      posTenant = new PosTenant(principal.getPreferredUsername(), tenant.getName());
    }
    else
    {
      posTenant = adminFindTenantById(id, principal);
    }
    tenant.setModifiedBy(principal.getPreferredUsername());
    PosPosMapper.INSTANCE.updatePosTenantFromSource(tenant, posTenant);
    log.debug("posTenant = {}", posTenant);
    posTenantsRepository.saveAndFlush(posTenant);
    return posTenant;
  }

}
