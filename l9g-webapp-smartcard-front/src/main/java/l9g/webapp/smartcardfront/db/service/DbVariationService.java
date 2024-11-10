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
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosVariation;
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
  // private final DbUserService userService;

  private final DbTenantService dbTenantService;

  private final PosVariationsRepository posVariationsRepository;

  // private final PosProductsRepository posProductsRepository;
  public List<PosVariation> productFindAllVariations(String productId)
  {
    return posVariationsRepository.findAllByProduct_idOrderByNameAsc(productId);
  }

  /*

  public PosProduct ownerGetProductById(String id, HttpSession session, DefaultOidcUser principal)
  {
    tenantService.checkTenantOwner(session, principal);
    return posProductsRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  public PosProduct ownerSaveProduct(String id, FormProduct formProduct,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    log.debug("User ist authorized for tenant: {}", tenant.getId());
    PosProduct posProduct;

    if(formProduct.getName() != null && formProduct.getName().isBlank())
    {
      formProduct.setName(null);
    }

    String categoryId = formProduct.getCategoryId();
    PosCategory category = posCategoriesRepository.findById(categoryId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

    if("add".equals(id))
    {
      log.debug("add new product");
      posProduct = new PosProduct(userService.gecosFromPrincipal(principal),
        category, formProduct.getName());
    }
    else
    {
      posProduct = ownerGetProductById(id, session, principal);
      FormPosMapper.INSTANCE.updatePosProductFromFormProduct(
        formProduct, posProduct);
      posProduct.setCategory(category);
      posProduct.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    log.debug("posProduct = {}", posProduct);
    return posProductsRepository.saveAndFlush(posProduct);
  }
   */
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

  /*
  public List<PosCategory> getAllCategories(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    return posCategoriesRepository.findByTenantOrderByNameAsc(tenant);
  }
   */
}
