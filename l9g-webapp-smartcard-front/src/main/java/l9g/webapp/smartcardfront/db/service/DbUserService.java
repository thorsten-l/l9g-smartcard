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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import l9g.webapp.smartcardfront.db.PosUserRepository;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
import l9g.webapp.smartcardfront.form.model.FormUser;
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
public class DbUserService
{
  private final Cache<String, Optional<PosUser>> byPreferredUsernameCache;

  private final PosUserRepository posUserRepository;

  public DbUserService(PosUserRepository posUserRepository)
  {
    this.posUserRepository = posUserRepository;
    this.byPreferredUsernameCache = Caffeine.newBuilder()
      .expireAfterWrite(8, TimeUnit.HOURS)
      .build();
  }

  public Optional<PosUser> findUserByPreferredUsername(String preferredUsername)
  {
    Optional<PosUser> optional =
      byPreferredUsernameCache.get(preferredUsername,
        posUserRepository :: findByUsername);

    if(optional.isEmpty())
    {
      byPreferredUsernameCache.invalidate(preferredUsername);
    }

    log.debug("user = {}", optional);

    return optional;
  }

  public void invalidateCache(String preferredUsername)
  {
    log.debug("invalidate cache for user = {}", preferredUsername);
    byPreferredUsernameCache.invalidate(preferredUsername);
  }

  public boolean isAdmin(DefaultOidcUser principal)
  {
    return principal.getAuthorities().stream()
      .anyMatch(auth -> auth.getAuthority()
      .equals("ROLE_" + PosRole.POS_ADMINISTRATOR));
  }

  public boolean isOwner(DefaultOidcUser principal)
  {
    return principal.getAuthorities().stream()
      .anyMatch(auth
        -> auth.getAuthority().equals("ROLE_" + PosRole.POS_OWNER));
  }

  public PosUser posUserFromPrincipal(DefaultOidcUser principal)
  {
    return findUserByPreferredUsername(
      principal.getPreferredUsername())
      .orElseThrow(()
        -> new AccessDeniedException("Access denied! - user not found"));
  }

  public String gecosFromPrincipal(DefaultOidcUser principal)
  {
    return principal.getFamilyName() + ", " + principal.getGivenName()
      + ", " + principal.getEmail() + ", "
      + principal.getPreferredUsername();
  }

  public List<PosUser> ownerGetUsersByTenant(HttpSession session,
    DefaultOidcUser principal, PosTenant tenant)
  {
    return posUserRepository.findAllByTenantOrderByGecosAsc(tenant);
  }

  public PosUser ownerGetUserById(String id, DefaultOidcUser principal, PosTenant tenant)
  {
    if(isAdmin(principal) || posUserFromPrincipal(principal).getTenant().getId().equals(tenant.getId()))
    {
      return posUserRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    else
    {
      throw new AccessDeniedException("Access denied");
    }
  }

  public PosUser ownerSaveUser(String id, FormUser formUser,
    HttpSession session, DefaultOidcUser principal, PosTenant tenant)
  {
    PosUser posUser;

    if(formUser.getUsername() != null && formUser.getUsername().isBlank())
    {
      formUser.setUsername(null);
    }

    if("add".equals(id))
    {
      log.debug("add new property");
      posUser = new PosUser(gecosFromPrincipal(principal),
        tenant, formUser.getUsername(), formUser.getGecos(), PosRole.valueOf(formUser.getRole()));
    }
    else
    {
      posUser = ownerGetUserById(id, principal, tenant);
      log.debug("posProperty={}", posUser);
      posUser.setRole(PosRole.valueOf(formUser.getRole()));
      posUser.setModifiedBy(gecosFromPrincipal(principal));
    }

    log.debug("posUser = {}", posUser);
    return posUserRepository.saveAndFlush(posUser);
  }

  public PosUser ownerDeleteUser(String id, HttpSession session,
    DefaultOidcUser principal, PosTenant tenant)
  {
    PosUser posUser = ownerGetUserById(id, principal, tenant);
    log.debug("posUser={}", posUser);

    posUserRepository.delete(posUser);
    posUserRepository.flush();
    return posUser;
  }

}
