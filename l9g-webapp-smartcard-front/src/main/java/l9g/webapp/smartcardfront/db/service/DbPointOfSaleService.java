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
import l9g.smartcard.dto.DtoCreditCardReader;
import l9g.webapp.smartcardfront.client.ApiClientService;
import l9g.webapp.smartcardfront.db.PosAddressesRepository;
import l9g.webapp.smartcardfront.db.PosPointsOfSaleRepository;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormPointOfSale;
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
public class DbPointOfSaleService
{
  private final DbUserService userService;

  private final DbTenantService tenantService;

  private final PosPointsOfSaleRepository posPointsOfSaleRepository;

  private final PosAddressesRepository posAddressesRepository;
  
  private final ApiClientService apiClientService;

  public List<PosPointOfSale> findPointsOfSaleByTenant(HttpSession session, DefaultOidcUser principal)
  {

    PosTenant tenant = tenantService.getSelectedTenant(session, principal);

    log.debug("Fetching Point of Sale for tenantId={}", tenant.getId());

    return posPointsOfSaleRepository.findAllByTenantOrderByNameAsc(tenant);
  }

  public PosPointOfSale findPointOfSaleById(String id)
  {
    return posPointsOfSaleRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Point of Sale not found"));
  }

  public PosPointOfSale savePointOfSale(String id, FormPointOfSale formPointOfSale,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.getSelectedTenant(session, principal);

    PosPointOfSale posPointOfSale;

    if(formPointOfSale.getName() != null && formPointOfSale.getName().isBlank())
    {
      formPointOfSale.setName(null);
    }

    if("add".equals(id))
    {
      log.debug("add new Point of Sale");
      posPointOfSale = new PosPointOfSale(userService.gecosFromPrincipal(principal),
        tenant, formPointOfSale.getName());
    }
    else
    {
      posPointOfSale = findPointOfSaleById(id);
      FormPosMapper.INSTANCE.updatePostPointOfSaleFromFormPointOfSale(formPointOfSale, posPointOfSale);
      posPointOfSale.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    posPointOfSale.setAddress(
      posAddressesRepository.findById(
        formPointOfSale.getAddressId()).orElse(null));
    
    if ( formPointOfSale.getSumupReaderId() != null 
        && ! formPointOfSale.getSumupReaderId().isBlank()  )
    {
      DtoCreditCardReader reader = apiClientService.findCreditCardReaderById(formPointOfSale.getSumupReaderId());
      if ( reader != null )
      {
        posPointOfSale.setSumupReaderName(reader.getName());
      }
      else
      {
        posPointOfSale.setSumupReaderId(null);
        posPointOfSale.setSumupReaderName(null);        
      }
    }
    else
    {
      posPointOfSale.setSumupReaderId(null);
      posPointOfSale.setSumupReaderName(null);
    }

    log.debug("*** posPointOfSale = {}", posPointOfSale);
    return posPointsOfSaleRepository.saveAndFlush(posPointOfSale);
  }

  public PosPointOfSale deletePointOfSale(String id)
  {
    PosPointOfSale pointOfSale = findPointOfSaleById(id);
    posPointsOfSaleRepository.delete(pointOfSale);
    posPointsOfSaleRepository.flush();
    return pointOfSale;
  }

}
