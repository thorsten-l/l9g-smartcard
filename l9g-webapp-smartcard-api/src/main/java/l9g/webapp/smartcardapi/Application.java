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
package l9g.webapp.smartcardapi;

import l9g.webapp.smartcardapi.crypto.CryptoHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application
{
  public static void main(String[] args)
  {
    if(args.length == 2 && args[0].equals("-e"))
    {
      CryptoHandler cryptoHandler = CryptoHandler.getInstance();
      System.out.println(args[1] + " = " + cryptoHandler.encrypt(args[1]));
      System.exit(0);
    }

    SpringApplication.run(Application.class, args);
  }

}
