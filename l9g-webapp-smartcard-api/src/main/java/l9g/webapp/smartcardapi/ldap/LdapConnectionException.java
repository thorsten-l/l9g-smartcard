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

/**
 *
 * @author Thorsten Ludewig (t.ludewig@ostfalia.de)
 */
public class LdapConnectionException extends RuntimeException
{

  /** Field description */
  private static final long serialVersionUID = 1916708941089543928L;

  //~--- constructors ---------------------------------------------------------

  public LdapConnectionException() {}

  public LdapConnectionException(String message)
  {
    super(message);
  }

  public LdapConnectionException(Throwable cause)
  {
    super(cause);
  }

  public LdapConnectionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
