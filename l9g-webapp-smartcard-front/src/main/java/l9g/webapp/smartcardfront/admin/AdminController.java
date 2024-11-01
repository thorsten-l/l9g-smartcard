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

import java.util.Locale;
import l9g.webapp.smartcardfront.db.model.PosRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController
{
  @Value("${app.barcode.enabled}")
  private boolean barcodeEnabled;

  @Value("${app.customer-number.enabled}")
  private boolean customerNumberEnabled;

  private void generalModel(DefaultOidcUser principal, Model model)
  {
    log.debug("preferred_username={}", principal.getPreferredUsername());
    Locale locale = LocaleContextHolder.getLocale();
    model.addAttribute("principal", principal);
    model.addAttribute("locale", locale.toString());
    model.addAttribute("barcodeEnabled", Boolean.toString(barcodeEnabled));
    model.addAttribute("customerNumberEnabled", customerNumberEnabled);

    boolean isAdmin = principal.getAuthorities().stream()
      .anyMatch(auth -> auth.getAuthority()
        .equals("ROLE_" + PosRole.POS_ADMINISTRATOR));

    if(isAdmin)
    {
      log.debug("{} is administrator!", principal.getName());
    }
    else
    {
      log.debug("{} is NOT administrator!", principal.getName());
    }
  }

  @GetMapping("/admin/{page}")
  public String pos(@PathVariable String page,
    @AuthenticationPrincipal DefaultOidcUser principal, Model model)
  {
    generalModel(principal, model);
    return "admin/" + page;
  }

}
