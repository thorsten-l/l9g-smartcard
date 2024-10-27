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
package l9g.webapp.smartcardmonitor;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import l9g.smartcard.dto.DtoCard;
import l9g.smartcard.dto.DtoEvent;
import l9g.webapp.smartcardmonitor.ws.MonitorWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SmartcardHandler implements ApplicationRunner
{
  private final MonitorWebSocketHandler webSockerHandler;
  private final BuildProperties buildProperties;
  
  @Value("${app.posname}")
  private String appPosName;

  /**
   * This method is the entry point for the application when it is run.
   * It initializes the terminal factory and card terminals, and continuously
   * monitors for card presence in the card reader. When a card is detected,
   * it reads the card's UID and fires events accordingly.
   *
   * @param args Application arguments passed to the run method.
   * @throws Exception if an error occurs during the execution.
   *
   * The method performs the following steps:
   * 1. Initializes the terminal factory and card terminals.
   * 2. Logs application details such as name, description, version, and build time.
   * 3. Enters an infinite loop to monitor card presence.
   * 4. If card terminals are available, it logs the available card readers.
   * 5. Uses the first available card reader to wait for a card to be present.
   * 6. When a card is detected, it connects to the card and logs card details.
   * 7. Sends a command APDU to the card to retrieve the card's UID.
   * 8. Logs the card UID and serial number.
   * 9. Fires an event with the card details.
   * 10. Waits for the card to be removed and fires a card removal event.
   * 11. If no card reader is found, logs an error and fires an error event.
   * 12. Catches any exceptions, logs the error, and fires an error event.
   */
  @Override
  public void run(ApplicationArguments args)
    throws Exception
  {
    TerminalFactory factory = TerminalFactory.getDefault();
    CardTerminals terminals = factory.terminals();

    System.out.println("\n\n**********************************************");
    System.out.println( "Point of sales name = " + appPosName );
    System.out.println( "\nApplication:");
    System.out.println( "  - name: " + buildProperties.get("name") );
    System.out.println( "  - description: " + buildProperties.get("description") );
    System.out.println( "  - version: " + buildProperties.get("version") );
    System.out.println( "  - build time: " + buildProperties.getTime().toString() );
    System.out.println("**********************************************\n\n");
    
    log.info("appPosName={}",appPosName);
    log.info("appName={}",buildProperties.get("name"));
    log.info("appVersion={}",buildProperties.get("version"));
    log.info("appBuildDate={}",buildProperties.getTime().toString());
      
    while(true)
    {
      try
      {
        if(terminals != null &&  ! terminals.list().isEmpty())
        {
          log.debug("Available card readers: {}", terminals.list());

          CardTerminal terminal = terminals.list().get(0);
          log.debug("Using card reader: {}", terminal.getName());
          log.debug("Waiting for a card...");

          // DO NOT USE BLOCKING I/O
          // You will not be able to check the card reader's presence 
          // by using terminal.waitForCardPresent(0).
          terminal.waitForCardPresent(terminalTimeout);

          if(terminal.isCardPresent())
          {
            Card card = terminal.connect("*");
            log.debug("Card: {}", card);
            log.debug("Card Protocol: {}", card.getProtocol());
            byte[] cardAtr = card.getATR().getBytes();
            log.debug("Card ATR: {}", bytesToHex(cardAtr));

            byte[] command = new byte[]
            {
              (byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00
            };
            CommandAPDU commandAPDU = new CommandAPDU(command);

            long startTime = System.currentTimeMillis();
            ResponseAPDU response = card.getBasicChannel().transmit(commandAPDU);
            long endTime = System.currentTimeMillis();
            log.debug("transmit command APDU in {}ms", endTime - startTime);

            startTime = System.currentTimeMillis();
            byte[] uidBytes = response.getData();
            endTime = System.currentTimeMillis();
            log.debug("reading card uid in {}ms", endTime - startTime);
            log.trace("Card UID: {}", bytesToHex(uidBytes));

            long serial = bytesToLongLittleEndian(uidBytes);
            log.trace("Card Serial: {}", serial);

            DtoEvent event = new DtoEvent(
              new DtoCard(card.getProtocol(), bytesToHex(cardAtr),
                bytesToHex(uidBytes), Long.toString(serial)));

            log.debug("fire event");
            startTime = System.currentTimeMillis();
            webSockerHandler.fireEvent(event);
            endTime = System.currentTimeMillis();
            log.debug("event fired in {}ms", endTime - startTime);

            // remove active sessions on card
            card.disconnect(true);

            do
            {
              log.debug("Waiting for card removal...");
              // DO NOT USE BLOCKING I/O
              terminal.waitForCardAbsent(terminalTimeout);
            }
            while(terminal.isCardPresent() &&  ! terminals.list().isEmpty());
            webSockerHandler
              .fireEvent(new DtoEvent(DtoEvent.EVENT_CARD_REMOVED));
          }
        }
        else
        {
          log.error("ERROR: No card reader found!");
          webSockerHandler
            .fireEvent(new DtoEvent(DtoEvent.EVENT_ERROR,
              "No card reader found!"));
          Thread.sleep(terminalTimeout);
        }
      }
      catch(Throwable t)
      {
        log.error("ERROR: {}", t.getMessage());
        webSockerHandler
          .fireEvent(new DtoEvent(DtoEvent.EVENT_ERROR,
            t.getMessage()));
        Thread.sleep(terminalTimeout);
      }
    }
  }

  private static String bytesToHex(byte[] bytes)
  {
    StringBuilder sb = new StringBuilder();
    for(byte b : bytes)
    {
      sb.append(String.format("%02X", b));
    }
    return sb.toString();
  }

  public static long bytesToLongLittleEndian(byte[] bytes)
  {
    long uid = 0;
    for(int i = bytes.length - 1; i >= 0; i --)
    {
      uid <<= 8;
      uid += (0x00ff & bytes[i]);
    }
    return uid;
  }

  @Value("${smartcard.terminal.timeout}")
  private long terminalTimeout;

}
