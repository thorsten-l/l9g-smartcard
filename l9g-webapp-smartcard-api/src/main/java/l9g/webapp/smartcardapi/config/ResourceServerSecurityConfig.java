/*
 * Copyright 2022 Thorsten Ludewig (t.ludewig@gmail.com).
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
package l9g.webapp.smartcardapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class ResourceServerSecurityConfig
{
  private final AppAuthoritiesConverter appAuthoritiesConverter;
  
  @Value("${spring.security.oauth2.resourceserver.jwt.jwks-uri}")
  private String jwksUri;
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
  {
    log.debug("filterChain");
    http.authorizeHttpRequests(
      authorize -> authorize.requestMatchers(
        "/webjars/**", "/actuator/**", "/api-docs/**", "/swagger-ui/**",
        "/", "/index.html"
      ).permitAll()
   // .requestMatchers("/api/v1/admin/**").access(hasScope("profile"))
   // .requestMatchers("/api/v1/admin/**").hasAuthority("admin")
      .anyRequest().authenticated())
      .oauth2ResourceServer(
        (oauth2) -> oauth2.jwt(
          jwt->jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );

    return http.build();
  }

  @Bean
  JwtDecoder jwtDecoder()
  {
    log.debug("jwtDecoder");
    log.debug("jwksUri={}", jwksUri);
    return NimbusJwtDecoder.withJwkSetUri(this.jwksUri).build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter()
  {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(appAuthoritiesConverter);
    return converter;
  }
}
