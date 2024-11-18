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
import l9g.webapp.smartcardfront.db.PosAddressesRepository;
import l9g.webapp.smartcardfront.db.PosPointsOfSalesRepository;
import l9g.webapp.smartcardfront.db.model.PosAddress;
import l9g.webapp.smartcardfront.db.model.PosPointOfSales;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.form.model.FormPointOfSales;
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
public class DbPointOfSalesService
{
  private final DbUserService userService;

  private final DbTenantService tenantService;

  private final PosAddressesRepository posAddressesRepository;

  private final PosPointsOfSalesRepository posPointsOfSalesRepository;

  public List<PosPointOfSales> ownerGetPointOfSalesByTenantAndAddress(String tenantId, String addressId, HttpSession session, DefaultOidcUser principal)
  {

    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    PosAddress address = posAddressesRepository.findById(addressId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));

    log.debug("Fetching Point of Sales for tenantId={} and addressId={}", tenant.getId(), address.getId());

    return posPointsOfSalesRepository.findAllByTenant(tenant);
  }

  public PosPointOfSales ownerGetPointOfSalesById(String id, HttpSession session, DefaultOidcUser principal)
  {
    tenantService.checkTenantOwner(session, principal);
    return posPointsOfSalesRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Point of Sales not found"));
  }

  public PosPointOfSales ownerSavePointOfSales(String id, String addressId, FormPointOfSales formPointOfSales,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    PosPointOfSales posPointOfSales;

    if(formPointOfSales.getName() != null && formPointOfSales.getName().isBlank())
    {
      formPointOfSales.setName(null);
    }

    if("add".equals(id))
    {
      log.debug("add new Point of Sales");
      posPointOfSales = new PosPointOfSales(userService.gecosFromPrincipal(principal),
        tenant, formPointOfSales.getName());
    }
    else
    {
      posPointOfSales = ownerGetPointOfSalesById(id, session, principal);
      posPointOfSales.setName(formPointOfSales.getName());
      log.debug("posPointOfSales={}", posPointOfSales);
      posPointOfSales.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    posPointOfSales.setHidden(posPointOfSales.isHidden());
    log.debug("posPointOfSales = {}", posPointOfSales);
    return posPointsOfSalesRepository.saveAndFlush(posPointOfSales);
  }

  public PosPointOfSales ownerDeletePointOfSales(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosPointOfSales pointOfSales = ownerGetPointOfSalesById(id, session, principal);
    posPointsOfSalesRepository.delete(pointOfSales);
    posPointsOfSalesRepository.flush();
    return pointOfSales;
  }

}
