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
package l9g.webapp.smartcardfront.admin;

import l9g.webapp.smartcardfront.db.service.UserService;
import l9g.webapp.smartcardfront.db.service.DbService;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiAdminController
{
  private final UserService userService;

  private final PosTenantsRepository posTenantsRepository;

  @DeleteMapping("/tenant/{id}")
  public ResponseEntity deleteTenant(
    @PathVariable String id,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.debug("delete tenant: {} with principal {}", id, principal.getName());
    if(userService.isAdmin(principal))
    {
      PosTenant systemTenant = posTenantsRepository.findByName(DbService.KEY_SYSTEM_TENANT).get();

      if(systemTenant.getId().equals(id))
      {
        throw new AccessDeniedException("Deleting system tenant is forbidden");
      }
      else
      {
        posTenantsRepository.deleteById(id);
      }
    }
    return ResponseEntity.ok("OK");
  }

}
