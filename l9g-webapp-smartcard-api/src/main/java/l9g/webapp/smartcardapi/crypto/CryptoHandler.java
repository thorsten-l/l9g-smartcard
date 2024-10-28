/*
 * Copyright 2023 Thorsten Ludewig (t.ludewig@gmail.com).
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
package l9g.webapp.smartcardapi.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
public class CryptoHandler
{
  public final static String AES256_PREFIX = "{AES256}";

  public CryptoHandler()
  {
    log.debug("CryptoHandler()");
    aes256 = new AES256(new AppSecretKey().getSecretKey());
  }

  public String encrypt(String text)
  {
    return AES256_PREFIX + aes256.encrypt(text);
  }

  public String decrypt(String encryptedText)
  {
    String text;

    if(encryptedText != null && encryptedText.startsWith(AES256_PREFIX))
    {
      text = aes256.decrypt(encryptedText.substring(AES256_PREFIX.length()));
    }
    else
    {
      text = encryptedText;
    }

    return text;
  }

  private final AES256 aes256;
}
