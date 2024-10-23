/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package l9g.webapp.smartcardfront.db;

import java.util.Optional;
import l9g.webapp.smartcardfront.db.model.PosPerson;
import l9g.webapp.smartcardfront.db.model.PosPointOfSales;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * DbService is responsible for managing and updating the properties related to
 * tenants...
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbService
{
  public static final String KEY_SYSTEM_USER = "*SYSTEM USER*";

  public static final String KEY_SYSTEM_TENANT = "*SYSTEM TENANT*";

  public static final String KEY_SYSTEM_POS = "*SYSTEM POS*";

  public static final String KEY_UNSET = "*** unset ***";

  public static final String KEY_DB_INITIALIZED = "database.initialized";

  public static final String KEY_DEFAULT_CURRENCY = "default.currency";

  public static final String KEY_DEFAULT_TAX = "default.tax";

  /**
   * Handles the application startup event.
   *
   * This method is triggered when the application is ready and performs
   * necessary initialization tasks.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onStartup()
  {
    log.info("Application startup");

    PosTenant systemTenant = posTenantsRepository.findByName(KEY_SYSTEM_TENANT)
      .orElseGet(() -> posTenantsRepository.save(
      new PosTenant(KEY_SYSTEM_USER, KEY_SYSTEM_TENANT)));

    Optional<PosProperty> dbInitialized = posPropertiesRepository
      .findByTenantAndKey(systemTenant, KEY_DB_INITIALIZED);

    if( ! dbInitialized.isPresent())
    {
      log.info("\n\n*** INITIALIZE DATABASE ***\n");
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_USER,
        systemTenant, KEY_DB_INITIALIZED, "true"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_USER,
        systemTenant, KEY_DEFAULT_CURRENCY, "EUR"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_USER,
        systemTenant, KEY_DEFAULT_TAX, "1.0"));

      PosPointOfSales pos = posPointsOfSalesRepository.save(
        new PosPointOfSales(KEY_SYSTEM_USER, systemTenant, KEY_SYSTEM_POS)
      );
      postTransactionsRepository.save(
        new PosTransaction(KEY_SYSTEM_USER, systemTenant, pos)
      );
      
      pos = posPointsOfSalesRepository.save(
        new PosPointOfSales(KEY_SYSTEM_USER, systemTenant, "rz-dev1")
      );
      pos.setAmountCash(123.89);
      pos.setSumupReaderId("-reader id-");
      pos.setCardIssuing(true);
      pos.setCardPayment(true);
      posPointsOfSalesRepository.save(pos);
      
      postTransactionsRepository.save(
        new PosTransaction(KEY_SYSTEM_USER, systemTenant, pos)
      );
    }
    else
    {
      log.info("Database already initialized.");
    }

    if(adminUsernames != null && adminUsernames.length > 0
      &&  ! KEY_UNSET.equals(adminUsernames[0]))
    {
      for(String username : adminUsernames)
      {
        log.debug("Create/update '{}' as administrator", username);
        PosPerson person = posPersonsRepository.findByUsername(username)
          .orElseGet(() -> posPersonsRepository.save(
          new PosPerson(KEY_SYSTEM_USER, systemTenant, username, 
            PosRole.POS_ADMINISTRATOR)));
        person.setTenant(systemTenant);
        posPersonsRepository.save(person);
      }
    }
    else
    {
      log.warn("No administrator usernames specified in config.yaml");
    }
  }

  @Value("${app.administrator.usernames}")
  private String[] adminUsernames;

  private final PosPersonsRepository posPersonsRepository;

  private final PosTenantsRepository posTenantsRepository;

  private final PosPropertiesRepository posPropertiesRepository;

  private final PosPointsOfSalesRepository posPointsOfSalesRepository;

  private final PosTransactionsRepository postTransactionsRepository;

}
