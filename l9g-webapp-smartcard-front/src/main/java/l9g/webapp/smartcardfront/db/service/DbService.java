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
package l9g.webapp.smartcardfront.db.service;

import java.util.Optional;
import l9g.webapp.smartcardfront.db.PosCategoriesRepository;
import l9g.webapp.smartcardfront.db.PosProductsRepository;
import l9g.webapp.smartcardfront.db.PosPropertiesRepository;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.PosTransactionsRepository;
import l9g.webapp.smartcardfront.db.PosUserRepository;
import l9g.webapp.smartcardfront.db.PosVariationsRepository;
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosUser;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosRole;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import l9g.webapp.smartcardfront.db.PosPointsOfSaleRepository;

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
  public static final String KEY_SYSTEM_USER = "SYSTEM USER";

  public static final String KEY_SYSTEM_TENANT = "SYSTEM TENANT";

  public static final String KEY_TEST_TENANT = "Test";

  public static final String KEY_SYSTEM_POS = "SYSTEM POS";

  public static final String KEY_UNSET = "*** unset ***";

  public static final String KEY_DB_INITIALIZED = "database.initialized";

  public static final String KEY_DEFAULT_CURRENCY = "default.currency";
  public static final String KEY_DEFAULT_CURRENCY_SYMBOL = "default.currency.symbol";
  public static final String KEY_DEFAULT_CURRENCY_SYMBOL_PLACEMENT = "default.currency.symbol.placement";

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
      new PosTenant(KEY_SYSTEM_USER, KEY_SYSTEM_TENANT, true)));

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
        systemTenant, KEY_DEFAULT_CURRENCY_SYMBOL, "€"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_USER,
        systemTenant, KEY_DEFAULT_CURRENCY_SYMBOL_PLACEMENT, "s"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_USER,
        systemTenant, KEY_DEFAULT_TAX, "0.0"));

      PosPointOfSale pos = posPointsOfSalesRepository.save(new PosPointOfSale(KEY_SYSTEM_USER, systemTenant, KEY_SYSTEM_POS)
      );
      posTransactionsRepository.save(
        new PosTransaction(KEY_SYSTEM_USER, systemTenant, pos)
      );

      pos = posPointsOfSalesRepository.save(new PosPointOfSale(KEY_SYSTEM_USER, systemTenant, "rz-dev1")
      );

      pos.setSumupReaderId("rz-dev1-sumup");
      pos.setCashRegisterBalance(2.34);
      pos.setCashFloat(50.0);
      pos.setCardIssuing(true);
      pos.setCardPayment(true);
      posPointsOfSalesRepository.save(pos);

      PosCategory posCategory = posCategoriesRepository.saveAndFlush(
        new PosCategory(KEY_SYSTEM_USER, systemTenant, "Default", true, true));

      
      PosProduct posProduct = posProductsRepository.saveAndFlush(
        new PosProduct(KEY_SYSTEM_USER, posCategory, "Sample product", 
          "The description", 1.0, 0.0));
      
      posVariationsRepository.saveAndFlush(
        new PosVariation(KEY_SYSTEM_USER, posProduct, "Sample Var1", 10.10, 0.0));
      posVariationsRepository.saveAndFlush(
        new PosVariation(KEY_SYSTEM_USER, posProduct, "Sample Var2", 20.20, 7.0));
      posVariationsRepository.saveAndFlush(
        new PosVariation(KEY_SYSTEM_USER, posProduct, "Sample Var3", 30.30, 19.0));

      if(adminUsernames != null && adminUsernames.length > 0
        &&  ! KEY_UNSET.equals(adminUsernames[0]))
      {
        for(String username : adminUsernames)
        {
          log.debug("Create/update '{}' as administrator", username);
          PosUser person = posUserRepository.findByUsername(username)
            .orElseGet(() -> posUserRepository.save(new PosUser(KEY_SYSTEM_USER, systemTenant, username, "System Admin (" + username + ")",
            PosRole.POS_ADMINISTRATOR)));
          person.setTenant(systemTenant);
          posUserRepository.save(person);
        }
      }
      else
      {
        log.warn("No administrator usernames specified in config.yaml");
      }
    }
    else
    {
      log.info("Database already initialized.");
    }
  }

  @Value("${app.administrator.usernames}")
  private String[] adminUsernames;

  private final PosUserRepository posUserRepository;

  private final PosTenantsRepository posTenantsRepository;

  private final PosPropertiesRepository posPropertiesRepository;

  private final PosPointsOfSaleRepository posPointsOfSalesRepository;

  private final PosTransactionsRepository posTransactionsRepository;

  private final PosCategoriesRepository posCategoriesRepository;

  private final PosProductsRepository posProductsRepository;

  private final PosVariationsRepository posVariationsRepository;

}
