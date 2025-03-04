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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
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
  private final static String ATTRIBUTE_JPEGPHOTO = "jpegPhoto";

  private final LdapConnectionFactory ldapConnectionFactory;

  public Map<String, String> searchForUserId(String userId)
  {
    Map<String, String> account = new LinkedHashMap<>();
    LDAPConnection connection = null;

    log.debug("LDAP search for userId = " + userId);

    if(userId != null &&  ! userId.isBlank())
    {
      try
      {
        connection = ldapConnectionFactory.getConnection();
        MessageFormat searchUserIdFormat = new MessageFormat(searchUserIdFilter);

        String _searchFilter = searchUserIdFormat.format(new Object[]
        {
          userId
        });

        log.debug(_searchFilter);

        SearchResult searchResult = connection.search(baseDn,
          getSearchScope(searchScope), _searchFilter, searchAttributes);

        List<SearchResultEntry> searchResultEntry = searchResult.
          getSearchEntries();

        if(searchResultEntry.size() == 1)
        {
          log.debug("Account for smartcard user id '{}' found.", userId);
          searchResultEntry.get(0).getAttributes().forEach(
            attribute ->
          {
            String attributeName = attribute.getName();
            if(ATTRIBUTE_JPEGPHOTO.equals(attributeName))
            {
              account.put(attributeName, Base64.getEncoder()
                .encodeToString(attribute.getValueByteArray()));
            }
            else
            {
              account.put(attributeName, attribute.getValue());
            }
          });
        }
        else
        {
          log.warn("Smartcard user id '{}' not found.", userId);
        }
      }
      catch(LDAPException ex)
      {
        log.error("searchForUserId", ex);
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

  public Map<String, String> searchForCard(long serialNumber)
  {
    Map<String, String> account = new LinkedHashMap<>();
    LDAPConnection connection = null;

    log.debug("LDAP search for card = " + serialNumber);

    if(serialNumber > 0)
    {
      try
      {
        connection = ldapConnectionFactory.getConnection();
        MessageFormat searchCardFormat = new MessageFormat(searchCardFilter);

        String _searchFilter = searchCardFormat.format(new Object[]
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
          log.debug("Account for smartcard serial number '{}' found.", serialNumber);
          searchResultEntry.get(0).getAttributes().forEach(
            attribute ->
          {
            String attributeName = attribute.getName();
            if(ATTRIBUTE_JPEGPHOTO.equals(attributeName))
            {
              account.put(attributeName, Base64.getEncoder()
                .encodeToString(attribute.getValueByteArray()));
            }
            else
            {
              account.put(attributeName, attribute.getValue());
            }
          });
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

  public List<Map<String, String>> searchForTerm(String term)
  {
    List<Map<String, String>> accounts = new ArrayList<Map<String, String>>();
    LDAPConnection connection = null;

    String[] tokens = term.split("\\s+");

    if(tokens.length == 0)
    {
      return accounts;
    }
    /*
    for(String token : tokens)
    {
      if(token.length() < 2)
      {
        return accounts;
      }
    }
     */
    log.debug("LDAP search for term = '{}' {}", term, tokens);

    StringBuffer subFilter = new StringBuffer();
    subFilter.append("(|");

    if(tokens.length == 1)
    {
      subFilter.append("(sn=");
      subFilter.append(tokens[0]);
      subFilter.append("*)");
      subFilter.append("(givenName=");
      subFilter.append(tokens[0]);
      subFilter.append("*)");
    }
    else
    {
      if(tokens.length == 2)
      {
        subFilter.append("(&(sn=");
        subFilter.append(tokens[0]);
        subFilter.append("*)");
        subFilter.append("(givenName=");
        subFilter.append(tokens[1]);
        subFilter.append("*))");
        
        subFilter.append("(&(sn=");
        subFilter.append(tokens[1]);
        subFilter.append("*)");
        subFilter.append("(givenName=");
        subFilter.append(tokens[0]);
        subFilter.append("*))");
      }
    }
    subFilter.append(")");

    MessageFormat searchTermFormat = new MessageFormat(searchTermFilter);

    String _searchTermFilter = searchTermFormat.format(new Object[]
    {
      subFilter.toString()
    });

    log.debug(_searchTermFilter);

    try
    {
      connection = ldapConnectionFactory.getConnection();

      SearchResult searchResult = connection.search(baseDn,
        getSearchScope(searchScope), _searchTermFilter, searchAttributes);

      List<SearchResultEntry> searchResultEntry = searchResult.
        getSearchEntries();

      //////
      if(searchResultEntry.size() > 0)
      {
        log.debug("{} entries found.", Integer.toString(searchResultEntry.size()));

        List<SearchResultEntry> entries = new ArrayList<>(searchResult.getSearchEntries());

        entries.sort(Comparator.comparing((SearchResultEntry entry) -> entry.getAttributeValue("sn"),
          Comparator.nullsLast(String :: compareToIgnoreCase))
          .thenComparing(entry -> entry.getAttributeValue("givenName"),
            Comparator.nullsLast(String :: compareToIgnoreCase)));

        for(SearchResultEntry entry : entries)
        {
          Map<String, String> account = new LinkedHashMap<>();

          entry.getAttributes().forEach(attribute ->
          {
            String attributeName = attribute.getName();
            if(ATTRIBUTE_JPEGPHOTO.equals(attributeName))
            {
              account.put(attributeName, Base64.getEncoder()
                .encodeToString(attribute.getValueByteArray()));
            }
            else
            {
              account.put(attributeName, attribute.getValue());
            }
          });

          accounts.add(account);
        }
        ////////
      }
      else
      {
        log.warn("no entries for term '{}' found.", term);
      }
    }
    catch(LDAPException ex)
    {
      log.error("searchForTerm ", ex);
    }
    finally
    {
      if(connection != null)
      {
        connection.close();
      }
    }

    return accounts;
  }

  private static SearchScope getSearchScope(String name)
  {
    SearchScope scope;

    scope = switch(name.toUpperCase())
    {
      case "ONE" ->
        SearchScope.ONE;
      case "SUB" ->
        SearchScope.SUB;
      default ->
        SearchScope.BASE;
    };

    return scope;
  }

  @Value("${ldap.base-dn}")
  private String baseDn;

  @Value("${ldap.search.filter.userid}")
  private String searchUserIdFilter;

  @Value("${ldap.search.filter.card}")
  private String searchCardFilter;

  @Value("${ldap.search.filter.term}")
  private String searchTermFilter;

  @Value("${ldap.search.scope}")
  private String searchScope;

  @Value("${ldap.search.attributes}")
  private String[] searchAttributes;

}
