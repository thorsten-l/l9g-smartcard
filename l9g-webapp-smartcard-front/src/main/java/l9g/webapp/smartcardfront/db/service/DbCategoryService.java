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
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.form.model.FormCategory;
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
public class DbCategoryService
{  
  private final DbUserService userService;

  private final DbTenantService tenantService;
  
  private final PosCategoriesRepository posCategoriesRepository; 

  public List<PosCategory> ownerGetCategoriesByTenant(HttpSession session, DefaultOidcUser principal)
  {
    return posCategoriesRepository.findAllByTenant(tenantService.checkTenantOwner(session, principal));
  }

  public PosCategory ownerGetCategoryById(String id, HttpSession session, DefaultOidcUser principal)
  {
    tenantService.checkTenantOwner(session, principal);
    return posCategoriesRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
  }

  public PosCategory ownerSaveCategory(String id, FormCategory formCategory,
    HttpSession session, DefaultOidcUser principal)
  {
    PosTenant tenant = tenantService.checkTenantOwner(session, principal);
    PosCategory posCategory;

    if(formCategory.getName() != null && formCategory.getName().isBlank())
    {
      formCategory.setName(null);
    }
    
    if("add".equals(id))
    {
      log.debug("add new category");
      posCategory = new PosCategory(userService.gecosFromPrincipal(principal),
        tenant, formCategory.getName());
    }
    else
    {
      posCategory = ownerGetCategoryById(id, session, principal);
      posCategory.setName(formCategory.getName());
      log.debug("posCategory={}", posCategory);
      posCategory.setModifiedBy(userService.gecosFromPrincipal(principal));
    }

    posCategory.setHidden(formCategory.isHidden());
    log.debug("posCategory = {}", posCategory);
    return posCategoriesRepository.saveAndFlush(posCategory);
  }

  public PosCategory ownerDeleteCategory(String id, HttpSession session, DefaultOidcUser principal)
  {
    PosCategory category = ownerGetCategoryById(id, session, principal);
    posCategoriesRepository.delete(category);
    posCategoriesRepository.flush();
    return category;
  }

}
