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
package l9g.webapp.smartcardmonitor.config;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ClientSecurityConfig
{

  /**
   * Configures the security filter chain for the application.
   * 
   * <p>This method sets up the security filter chain by configuring CORS and CSRF settings.
   * It uses a custom CORS configuration source and disables CSRF protection for all requests.
   * 
   * Cross-Origin Resource Sharing (CORS)
   * CORS is a security feature implemented by web browsers to control how resources 
   * on a web page can be requested from another domain outside the domain from which 
   * the resource originated. It is a way to allow or restrict requested resources on 
   * a web server depending on where the HTTP request was initiated.
   *
   * Purpose: To prevent unauthorized access to resources on a server from a different origin.
   * Configuration: In the provided code, CORS is configured using a custom CORS configuration 
   * source, which is likely defined elsewhere in the application. This configuration source 
   * specifies which origins are allowed to access the resources, what HTTP methods are 
   * permitted, and other settings.
   *
   * Cross-Site Request Forgery (CSRF)
   * CSRF is a type of attack that tricks the victim into submitting a malicious request. 
   * It exploits the trust that a web application has in the user's browser. For example, 
   * if a user is logged into a banking site, an attacker could trick the user into submitting 
   * a request to transfer money without the user's knowledge.
   *
   * Purpose: To prevent unauthorized commands from being transmitted from a user that the 
   * web application trusts.
   * Configuration: In the provided code, CSRF protection is disabled for all requests using 
   * csrf.ignoringRequestMatchers("/**"). This means that the application will not enforce CSRF 
   * protection, which might be suitable for APIs or certain types of applications where CSRF 
   * protection is not necessary.
   * 
   * @param http the {@link HttpSecurity} to modify
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs while configuring the security filter chain
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception
  {
    log.debug("securityFilterChain");
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(csrf -> csrf.ignoringRequestMatchers("/**"));

    return http.build();
  }

  /**
   * Configures the CORS (Cross-Origin Resource Sharing) settings for the application.
   * 
   * This bean defines the CORS configuration source, which specifies the allowed origins,
   * methods, headers, and credentials policy for cross-origin requests.
   * 
   * @return a {@link CorsConfigurationSource} with the specified CORS settings.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource()
  {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    configuration.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
