/*
 * Copyright 2024 Thorsten Ludewig <t.ludewig@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Component
public class AppAuthoritiesConverter implements
  Converter<Jwt, Collection<GrantedAuthority>>
{
  @Value("${app.resource-access-roles}")
  private String resourceAccessRoles;
  
  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt)
  {
    List<String> realmRoles
      = (jwt.getClaimAsMap("realm_access") != null
      && jwt.getClaimAsMap("realm_access").get("roles") != null)
      ? (List<String>) jwt.getClaimAsMap("realm_access").get("roles")
      : List.of();

    List<String> resourceRoles
      = (jwt.getClaimAsMap("resource_access") != null
      && jwt.getClaimAsMap("resource_access").get(resourceAccessRoles) != null
      && ((Map) jwt.getClaimAsMap("resource_access").get(resourceAccessRoles)).get("roles")
      != null)
        ? ((Map<String, List<String>>) jwt
          .getClaimAsMap("resource_access").get(resourceAccessRoles)).get("roles")
        : List.of();

    List<GrantedAuthority> authorities = Stream.concat(realmRoles.stream(),
      resourceRoles.stream())
      .map(role -> "ROLE_" + role)
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toList());

    return authorities;
  }
}
