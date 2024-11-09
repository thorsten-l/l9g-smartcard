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

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import l9g.webapp.smartcardfront.db.service.DbUserService;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService
{
  private final DbUserService userService;

  private final DbTenantService tenantService;

  private final PosTenantsRepository posTenantsRepository;

  @Value("${app.web.base-url}")
  private String webBaseUrl;
  
  @Value("${app.development:false}")
  private boolean appDevelopment;

  public void generalModel(DefaultOidcUser principal,
    Model model, HttpSession session, String activePages )
  {
    log.debug("preferred_username={}", principal.getPreferredUsername());

    List<PosTenant> tenants;

    if(userService.isAdmin(principal))
    {
      log.debug("{} is administrator!", principal.getName());
      tenants = posTenantsRepository.findAllByOrderByNameAsc();
    }
    else
    {
      log.debug("{} is NOT administrator!", principal.getName());
      tenants = List.of();
    }

    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute("principal", principal);
    model.addAttribute("locale", locale.toString());
    model.addAttribute("selectedTenant",
      tenantService.getSelectedTenant(session, principal));
    model.addAttribute("tenants", tenants);
    model.addAttribute("webBaseUrl", webBaseUrl);
    model.addAttribute("appDevelopment", appDevelopment);
    model.addAttribute("activePages", activePages);
  }

}
