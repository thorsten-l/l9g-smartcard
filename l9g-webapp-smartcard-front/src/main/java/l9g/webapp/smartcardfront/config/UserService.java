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
package l9g.webapp.smartcardfront.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import l9g.webapp.smartcardfront.db.PosUserRepository;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
public class UserService
{
  private static final String SESSION_POS_SELECTED_TENANT = "POS_SELECTED_TENANT";

  private final Cache<String, Optional<PosUser>> byPreferredUsernameCache;

  private final PosUserRepository posUserRepository;

  public UserService(PosUserRepository posUserRepository)
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

  public PosTenant getSelectedTenant(HttpSession session, PosUser user)
  {
    PosTenant tenant;

    Object object = session.getAttribute(SESSION_POS_SELECTED_TENANT);

    if(user != null && user.getRole() == PosRole.POS_ADMINISTRATOR
      && object != null && object instanceof PosTenant)
    {
      log.debug("selected tenant found in session");
      tenant = (PosTenant)object;
    }
    else
    {
      log.debug("create new selected tenant for session");
      tenant = user.getTenant();
    }

    session.setAttribute(SESSION_POS_SELECTED_TENANT, tenant);

    return tenant;
  }

  public PosTenant getSelectedTenant(
    HttpSession session, DefaultOidcUser principal)
  {
    return getSelectedTenant(session, posUserFromPrincipal(principal));
  }

  public PosUser posUserFromPrincipal(DefaultOidcUser principal)
  {
    return findUserByPreferredUsername(
      principal.getPreferredUsername())
      .orElseThrow(()
        -> new AccessDeniedException("Access denied! - user not found"));
  }

}
