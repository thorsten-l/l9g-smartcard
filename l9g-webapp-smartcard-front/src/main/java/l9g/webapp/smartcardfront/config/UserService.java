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
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import l9g.webapp.smartcardfront.db.PosUserRepository;
import l9g.webapp.smartcardfront.db.model.PosUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
public class UserService
{
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
    Optional<PosUser> optional 
      = byPreferredUsernameCache.get(preferredUsername, 
        posUserRepository::findByUsername);
     
    if ( optional.isEmpty())
    {
      byPreferredUsernameCache.invalidate(preferredUsername);
    }
    
    log.debug("user = {}", optional);
    
    return optional;
  }  
}
