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
package l9g.webapp.smartcardapi.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@ostfalia.de)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LdapUtil
{

  private final LdapConnectionFactory ldapConnectionFactory;

  public Map<String,String> searchForCard(long serialNumber)
  {
    Map<String,String> account = new LinkedHashMap<>();
    LDAPConnection connection = null;

    log.debug("LDAP search for card = " + serialNumber);
    
    if(serialNumber > 0)
    {
      try
      {
        connection = ldapConnectionFactory.getConnection();        
        MessageFormat searchFormat = new MessageFormat(searchFilter);

        String _searchFilter = searchFormat.format(new Object[]
        {
          Long.toString(serialNumber)
        });

        log.debug(_searchFilter);
        
        SearchResult searchResult = connection.search(baseDn,
          getSearchScope(searchScope), _searchFilter, searchAttributes);
        
        List<SearchResultEntry> searchResultEntry = searchResult.
          getSearchEntries();

        if(searchResultEntry.size() == 1)
        {
          log.debug( "Account for smartcard serial number '{}' found.", serialNumber);
          searchResultEntry.get(0).getAttributes().forEach( 
            attribute -> account.put(attribute.getName(), attribute.getValue()));
        }
        else
        {
          log.warn("Smartcard serial number '{}' not found.", serialNumber);
        }
      }
      catch(LDAPException ex)
      {
        log.error("searchForCard", ex);
      }
      finally
      {
        if(connection != null)
        {
          connection.close();
        }
      }
    }
    
    return account;
  }

  private static SearchScope getSearchScope(String name)
  {
    SearchScope scope;

    scope = switch(name.toUpperCase())
    {
      case "ONE" -> SearchScope.ONE;
      case "SUB" -> SearchScope.SUB;
      default -> SearchScope.BASE;
    };

    return scope;
  }

  @Value("${ldap.base-dn}")
  private String baseDn;

  @Value("${ldap.search.filter}")
  private String searchFilter;

  @Value("${ldap.search.scope}")
  private String searchScope;
  
  @Value("${ldap.search.attributes}")
  private String[] searchAttributes;
}
