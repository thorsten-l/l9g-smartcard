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
import l9g.webapp.smartcardfront.db.PosVariationsRepository;
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormVariation;
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
public class DbVariationService
{
  private final DbUserService userService;

  private final DbTenantService dbTenantService;

  private final DbPropertyService dbPropertyService;

  private final PosVariationsRepository posVariationsRepository;

  public List<PosVariation> productFindAllVariations(String productId)
  {
    return posVariationsRepository.findAllByProduct_idOrderByNameAsc(productId);
  }

  public PosVariation ownerSaveVariation(PosProduct posProduct, String id, FormVariation formVariation,
    HttpSession session, DefaultOidcUser principal)
  {
    PosVariation posVariation;

    if(formVariation.getName() != null && formVariation.getName().isBlank())
    {
      formVariation.setName(null);
    }

    if("add".equals(id))
    {
      log.debug("add new variation");
      posVariation = new PosVariation(
        userService.gecosFromPrincipal(principal), posProduct, 
        formVariation.getName(), formVariation.getPrice(),
        formVariation.getTax());
/*
        Double.parseDouble(
          dbPropertyService.getPropertyValue(
            session, principal, DbService.KEY_DEFAULT_TAX)));
*/
    }
    else
    {
      posVariation = ownerGetVariationById(id, session, principal);
      FormPosMapper.INSTANCE.updatePosVariationFromFormVariation(
        formVariation, posVariation);
      posVariation.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    log.debug("posVariation = {}", posVariation);
    return posVariationsRepository.saveAndFlush(posVariation);
  }

  public PosVariation ownerGetVariationById(
    String id, HttpSession session, DefaultOidcUser principal)
  {
    dbTenantService.checkTenantOwner(session, principal);
    return posVariationsRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(
      HttpStatus.NOT_FOUND, "Variation not found"));
  }

  public PosVariation ownerDeleteVariationById(
    String id, HttpSession session, DefaultOidcUser principal)
  {
    PosVariation variation = ownerGetVariationById(id, session, principal);
    posVariationsRepository.delete(variation);
    posVariationsRepository.flush();
    return variation;
  }

}
