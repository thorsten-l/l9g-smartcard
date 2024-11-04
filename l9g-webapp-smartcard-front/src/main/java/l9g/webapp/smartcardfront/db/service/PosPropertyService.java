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
import l9g.webapp.smartcardfront.db.PosPropertiesRepository;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

  private final PosPropertiesRepository propertiesRepository;

  public List<PosProperty> getPropertiesByTenant(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.getSelectedTenant(session, principal);
    return propertiesRepository.findAllByTenant(tenant);
  }

  public PosProperty getPropertyById(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.getSelectedTenant(session, principal);
    return propertiesRepository.findByIdAndTenant(id, tenant)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
  }

  public PosProperty saveProperty(PosProperty property, HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.getSelectedTenant(session, principal);
    property.setTenant(tenant);
    property.setModifiedBy(userService.gecosFromPrincipal(principal));
    return propertiesRepository.saveAndFlush(property);
  }

  public PosProperty deleteProperty(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosProperty property = getPropertyById(id, session, principal);
    propertiesRepository.delete(property);
    propertiesRepository.flush();
    return property;
  }

}
