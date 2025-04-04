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
import l9g.webapp.smartcardfront.form.model.FormProperty;
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
public class DbPropertyService
{
  private final DbUserService dbUserService;

  private final DbTenantService dbTenantService;

  private final PosPropertiesRepository posPropertiesRepository;

  public List<PosProperty> ownerGetPropertiesByTenant(HttpSession session, DefaultOidcUser principal)
  {
    return posPropertiesRepository.findAllByTenant(dbTenantService.checkTenantOwner(session, principal));
  }

  public PosProperty ownerGetPropertyById(String id, HttpSession session, DefaultOidcUser principal)
  {
    dbTenantService.checkTenantOwner(session, principal);
    return posPropertiesRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
  }

  public PosProperty ownerSaveProperty(String id, FormProperty formProperty,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = dbTenantService.checkTenantOwner(session, principal);
    PosProperty posProperty;

    if(formProperty.getValue() != null && formProperty.getValue().isBlank())
    {
      formProperty.setValue(null);
    }

    if("add".equals(id))
    {
      log.debug("add new property");
      posProperty = new PosProperty(dbUserService.gecosFromPrincipal(principal),
        tenant, formProperty.getKey(), formProperty.getValue());
    }
    else
    {
      posProperty = ownerGetPropertyById(id, session, principal);
      posProperty.setKey(formProperty.getKey());
      posProperty.setValue(formProperty.getValue());
      log.debug("posProperty={}", posProperty);
      posProperty.setModifiedBy(dbUserService.gecosFromPrincipal(principal));
    }

    log.debug("posProperty = {}", posProperty);
    return posPropertiesRepository.saveAndFlush(posProperty);
  }

  public PosProperty ownerDeleteProperty(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosProperty property = ownerGetPropertyById(id, session, principal);
    posPropertiesRepository.delete(property);
    posPropertiesRepository.flush();
    return property;
  }

  public PosProperty emptyProperty(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = dbTenantService.checkTenantOwner(session, principal);
    PosProperty property =
      new PosProperty(dbUserService.gecosFromPrincipal(principal), tenant, "", "");
    return property;
  }

  public String getPropertyValue(
    HttpSession session, DefaultOidcUser principal, String key)
  {
    // TODO: Error handling / Override default property by tenants property
    return posPropertiesRepository.findByTenantAndKey(
      dbTenantService.getSystemTenant(), key)
      .get().getValue();
  }

  public String getCurrency(HttpSession session, DefaultOidcUser principal)
  {
    return getPropertyValue(session, principal, DbService.KEY_DEFAULT_CURRENCY);
  }

  public String getCurrencySymbol(
    HttpSession session, DefaultOidcUser principal)
  {
    return getPropertyValue(session, principal,
      DbService.KEY_DEFAULT_CURRENCY_SYMBOL);
  }
  
  public String getCurrencySymbolPlacement(
    HttpSession session, DefaultOidcUser principal)
  {
    return getPropertyValue(session, principal,
      DbService.KEY_DEFAULT_CURRENCY_SYMBOL_PLACEMENT);
  }

}
