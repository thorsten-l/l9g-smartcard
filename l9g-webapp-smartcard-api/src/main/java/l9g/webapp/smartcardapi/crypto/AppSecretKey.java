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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
public class AppSecretKey
{
  private final static String SECRET_FILE = "secret.bin";

  public AppSecretKey()
  {
    try
    {
      File secretFile = new File(SECRET_FILE);

      if (secretFile.exists())
      {
        secretKey = new byte[48];
        log.debug("Loading secret file");
        try (FileInputStream input = new FileInputStream(secretFile))
        {
          input.read(secretKey);
        }
      }
      else
      {
        log.info("Writing secret file");

        try (FileOutputStream output = new FileOutputStream(secretFile))
        {
          AES256 aes256 = new AES256();
          secretKey = aes256.getSecret();
          output.write(secretKey);
        }

        // file permissions - r-- --- ---
        secretFile.setExecutable(false, false);
        secretFile.setWritable(false, false);
        secretFile.setReadable(false, false);
        secretFile.setReadable(true, true);
      }
    }
    catch (IOException | NoSuchAlgorithmException e)
    {
      log.error("ERROR: secret file ", e);
      System.exit(-1);
    }
  }

  @Getter
  private byte[] secretKey = null;
}
