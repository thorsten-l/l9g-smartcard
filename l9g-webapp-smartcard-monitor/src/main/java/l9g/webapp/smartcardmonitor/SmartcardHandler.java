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

  @Override
  public void run(ApplicationArguments args)
    throws Exception
  {
    TerminalFactory factory = TerminalFactory.getDefault();
    CardTerminals terminals = factory.terminals();

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
            ResponseAPDU response = card.getBasicChannel().transmit(commandAPDU);
            byte[] uidBytes = response.getData();
            log.trace("Card UID: {}", bytesToHex(uidBytes));

            // In this format the card uid/serial is stored in our directory service
            long serial = bytesToLongLittleEndian(uidBytes);
            log.trace("Card Serial: {}", serial);

            webSockerHandler.fireEvent(new DtoEvent(
              new DtoCard(card.getProtocol(), bytesToHex(cardAtr), 
                bytesToHex(uidBytes), serial)));

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
