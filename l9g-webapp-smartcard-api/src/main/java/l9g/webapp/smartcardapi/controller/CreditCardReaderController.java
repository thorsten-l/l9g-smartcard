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
package l9g.webapp.smartcardapi.controller;

import java.util.List;
import l9g.smartcard.dto.DtoCreditCardReader;
import l9g.webapp.smartcardapi.client.ApiSumUpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestController
@RequestMapping(path = "/api/v1/creditcardreader",
                produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class CreditCardReaderController
{
  private final ApiSumUpClientService apiCreditCardReaderService;
  
  @GetMapping()
  public List<DtoCreditCardReader> findAll()
  {
    log.debug("findAll");
    return apiCreditCardReaderService.sumupMerchantsReaders();
  }

  @GetMapping("/{id}")
  public DtoCreditCardReader findReaderById(@PathVariable String id)
  {
    log.debug("findReaderById={}", id);
    return apiCreditCardReaderService.sumupMerchantsReaderById(id);
  }
}
