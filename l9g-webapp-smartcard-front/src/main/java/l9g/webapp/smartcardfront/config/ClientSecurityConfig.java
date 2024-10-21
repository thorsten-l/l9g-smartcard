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
package l9g.webapp.smartcardfront.config;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class ClientSecurityConfig
{
  private final AppAuthoritiesConverter appAuthoritiesConverter;

  @Value("${app.login-access-role}")
  private String loginAccessRole;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
    ClientRegistrationRepository clientRegistrationRepository)
    throws Exception
  {
    log.debug("filterChain clientRegistrationRepository={}",
      clientRegistrationRepository);

    DefaultOAuth2AuthorizationRequestResolver resolver =
      new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);

    resolver.setAuthorizationRequestCustomizer(
      OAuth2AuthorizationRequestCustomizers.withPkce());

    http.authorizeHttpRequests(
      authorize -> authorize.requestMatchers("/error/**", "/api/v1/buildinfo",
        "/webjars/**", "/icons/**", "/css/**", "/images/**", "/actuator/**", "/logout").
        permitAll().anyRequest().hasRole(loginAccessRole))
      .oauth2Login(
        login -> login
          .authorizationEndpoint(
            authorizationEndpointCustomizer -> authorizationEndpointCustomizer
              .authorizationRequestResolver(resolver))
          .userInfoEndpoint(userInfo -> userInfo
          .oidcUserService(this.oidcUserService())
          ))
      .oauth2Client(withDefaults())
      .logout(
        logout -> logout.logoutSuccessHandler(
          oidcLogoutSuccessHandler(clientRegistrationRepository))
      );

    return http.build();
  }

  private LogoutSuccessHandler oidcLogoutSuccessHandler(
    ClientRegistrationRepository clientRegistrationRepository)
  {
    log.debug("oidcLogoutSuccessHandler");
    OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
      new OidcClientInitiatedLogoutSuccessHandler(
        clientRegistrationRepository);
    oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
    return oidcLogoutSuccessHandler;
  }

  private OidcUserService oidcUserService()
  {
    log.debug("oidcUserService");
    OidcUserService delegate = new OidcUserService();

    return new OidcUserService()
    {
      @Override
      public OidcUser loadUser(OidcUserRequest userRequest)
      {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        Jwt accessToken = decodeAccessToken(
          userRequest.getAccessToken().getTokenValue(),
          userRequest.getClientRegistration().getProviderDetails().getIssuerUri()
        );

        Collection<GrantedAuthority> authorities =
          appAuthoritiesConverter.convert(accessToken);

        if(log.isDebugEnabled())
        {
          authorities.stream()
            .map(GrantedAuthority :: getAuthority)
            .forEach(System.out :: println);
        }

        /*
        TODO: Get APPLICATION ROLES FROM DATABASE!
        */
        
        return new DefaultOidcUser(
          authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
      }

    };
  }

  private Jwt decodeAccessToken(String token, String issuerUri)
  {
    // Verwende einen JwtDecoder, um das Access-Token zu dekodieren
    JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
    return jwtDecoder.decode(token);
  }

}
