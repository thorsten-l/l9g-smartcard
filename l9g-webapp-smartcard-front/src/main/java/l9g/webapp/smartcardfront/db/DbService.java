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
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public static final String KEY_SYSTEM_TENANT = "*SYSTEM*";

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
      new PosTenant(KEY_SYSTEM_TENANT, KEY_SYSTEM_TENANT)));

    Optional<PosProperty> dbInitialized = posPropertiesRepository
      .findByTenantAndKey(systemTenant, KEY_DB_INITIALIZED);

    if( ! dbInitialized.isPresent())
    {
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_TENANT,
        systemTenant, KEY_DB_INITIALIZED, "true"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_TENANT,
        systemTenant, KEY_DEFAULT_CURRENCY, "EUR"));
      posPropertiesRepository.save(new PosProperty(KEY_SYSTEM_TENANT,
        systemTenant, KEY_DEFAULT_TAX, "1.0"));
      //posTransactionsRepository.save(
      //  new PosTransaction(KEY_SYSTEM_TENANT, systemTenant));
    }
    else
    {
      log.info("Database already initialized.");
    }
  }

  private final PosTenantsRepository posTenantsRepository;

  private final PosPropertiesRepository posPropertiesRepository;

  private final PosTransactionsRepository posTransactionsRepository;

}
