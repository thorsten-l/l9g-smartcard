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

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
/**
 * Service class for handling user roles.
 * <p>
 * This service provides functionality to print the roles assigned to the currently authenticated user.
 * </p>
 * 
 * <h2>Methods:</h2>
 * <ul>
 *   <li>{@link #printUserRoles()} - Prints the roles assigned to the currently authenticated user.</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * @Autowired
 * private UserRoleService userRoleService;
 * 
 * public void someMethod() {
 *     userRoleService.printUserRoles();
 * }
 * }
 * </pre>
 */
@Service
public class UserRoleService
{

  /**
   * Prints the roles assigned to the currently authenticated user.
   * <p>
   * This method retrieves the authentication information from the security context,
   * extracts the granted authorities (roles) of the authenticated user, and prints
   * each role to the standard output.
   * </p>
   */
  public void printUserRoles()
  {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    System.out.println("Assigned roles:");
    for(GrantedAuthority authority : authorities)
    {
      System.out.println(" - " + authority.getAuthority());
    }
  }

}
