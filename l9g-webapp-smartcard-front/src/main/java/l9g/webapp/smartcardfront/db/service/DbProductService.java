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
import l9g.webapp.smartcardfront.db.PosCategoriesRepository;
import l9g.webapp.smartcardfront.db.PosProductsRepository;
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.form.model.FormProduct;
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
public class DbProductService
{
  private final DbUserService userService;

  private final DbTenantService tenantService;

  private final PosCategoriesRepository posCategoriesRepository;

  private final PosProductsRepository posProductsRepository;

  public List<PosProduct> ownerFindAllProducts(HttpSession session, DefaultOidcUser principal)
  {
    if(tenantService.checkTenantOwner(session, principal) != null)
    {
      return posProductsRepository.findAllByOrderByNameAsc();
    }
    return null;
  }

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
      posProduct.setName(formProduct.getName());
      posProduct.setCategory(category);
      log.debug("posProduct={}", posProduct);
      posProduct.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    log.debug("posProduct = {}", posProduct);
    return posProductsRepository.saveAndFlush(posProduct);
  }

  public PosProduct ownerDeleteProduct(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosProduct product = ownerGetProductById(id, session, principal);
    posProductsRepository.delete(product);
    posProductsRepository.flush();
    return product;
  }

  public List<PosCategory> getAllCategories(HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    return posCategoriesRepository.findByTenantOrderByNameAsc(tenant);
  }

}
