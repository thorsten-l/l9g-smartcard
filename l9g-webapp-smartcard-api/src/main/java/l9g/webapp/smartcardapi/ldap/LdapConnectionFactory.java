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

//~--- non-JDK imports --------------------------------------------------------
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

//~--- JDK imports ------------------------------------------------------------
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocketFactory;
import l9g.webapp.smartcardapi.crypto.EncryptedValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@ostfalia.de)
 */
@Component
@RequiredArgsConstructor
public final class LdapConnectionFactory
{
  public LDAPConnection getConnection()
    throws LDAPException
  {
    LDAPConnectionOptions options = new LDAPConnectionOptions();
    LDAPConnection connection;

    if(hostSslEnabled)
    {
      connection = new LDAPConnection(createSSLSocketFactory(), options,
        hostName, hostPort, bindDn, bindPassword);
    }
    else
    {
      connection = new LDAPConnection(options, hostName,
        hostPort, bindDn, bindPassword);
    }

    connection.setConnectionName(hostName);
    return connection;
  }

  //~--- methods --------------------------------------------------------------
  private static SSLSocketFactory createSSLSocketFactory()
  {

    // trust all ??
    try
    {
      SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());

      return sslUtil.createSSLSocketFactory();
    }
    catch(GeneralSecurityException ex)
    {
      throw new LdapConnectionException("could not create ldap connection", ex);
    }
  }

  @Value("${ldap.host.ssl-enabled}")
  private boolean hostSslEnabled;

  @Value("${ldap.host.name}")
  private String hostName;

  @Value("${ldap.host.port}")
  private int hostPort;

  @Value("${ldap.bind.dn}")
  private String bindDn;

  @EncryptedValue("ldap.bind.password")
  private String bindPassword;

}
