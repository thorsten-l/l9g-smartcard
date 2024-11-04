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
import java.util.List;
import l9g.webapp.smartcardfront.db.PosPosMapper;
import l9g.webapp.smartcardfront.db.PosPropertiesRepository;
import l9g.webapp.smartcardfront.db.model.PosProperty;
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
public class PosPropertyService
{

  private final DbUserService userService;

  private final DbTenantService tenantService;

  private final PosPropertiesRepository posPropertiesRepository;

  private PosTenant checkTenantOwner(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.getSelectedTenant(session, principal);
    PosUser user = userService.posUserFromPrincipal(principal);
    if( userService.isAdmin(principal) 
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

  public List<PosProperty> ownerGetPropertiesByTenant(HttpSession session, DefaultOidcUser principal)
  {
    return posPropertiesRepository.findAllByTenant(checkTenantOwner(session, principal));
  }

  public PosProperty ownerGetPropertyById(String id, HttpSession session, DefaultOidcUser principal)
  {
    return posPropertiesRepository.findByIdAndTenant(id, checkTenantOwner(session, principal))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
  }

  public PosProperty ownerSaveProperty(String id, PosProperty formProperty,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = checkTenantOwner(session, principal);
    PosProperty posProperty;

    if(formProperty.getValue() != null && formProperty.getValue().isBlank())
    {
      formProperty.setValue(null);
    }
    
    if("add".equals(id))
    {
      log.debug("add new property");
      posProperty = new PosProperty(userService.gecosFromPrincipal(principal),
        tenant, formProperty.getKey(), formProperty.getValue());
    }
    else
    {
      posProperty = ownerGetPropertyById(id, session, principal);
      posProperty.setModifiedBy(userService.gecosFromPrincipal(principal));
      PosPosMapper.INSTANCE.updatePosPropertyFromSource(formProperty, posProperty);
    }

    log.debug("posProperty = {}", posProperty);
    return posPropertiesRepository.saveAndFlush(posProperty);
  }

  public PosProperty ownerDeleteProperty(String id, HttpSession session, DefaultOidcUser principal)
  {
    checkTenantOwner(session, principal);
    PosProperty property = ownerGetPropertyById(id, session, principal);
    posPropertiesRepository.delete(property);
    posPropertiesRepository.flush();
    return property;
  }

}
