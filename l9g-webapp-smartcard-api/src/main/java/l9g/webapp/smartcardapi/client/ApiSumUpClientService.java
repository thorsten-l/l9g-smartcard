/*
 * Copyright 2024 Thorsten Ludewig <t.ludewig@gmail.com>.
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
package l9g.webapp.smartcardapi.client;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l9g.smartcard.dto.DtoCreditCardReader;
import l9g.webapp.smartcardapi.crypto.EncryptedValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * https://developer.sumup.com/api
 * 
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiSumUpClientService
{
  private final RestClient.Builder builder;

  private RestClient restClient;

  @Value("${sumup.api.base-url}")
  private String sumupApiBaseUrl;

  @EncryptedValue("${sumup.api.merchant-code}")
  private String sumupApiMercantCode;

  @EncryptedValue("${sumup.api.token}")
  private String sumupApiToken;

  @PostConstruct
  private void initialize()
  {
    log.debug("initialize appApiBaseUrl={}", sumupApiBaseUrl);
    restClient = builder.baseUrl(sumupApiBaseUrl).build();
  }

  /**
   * https://developer.sumup.com/api/readers/list-readers
   * 
   * @return 
   */
  public List<DtoCreditCardReader> sumupMerchantsReaders()
  {
    List<DtoCreditCardReader> readersList = new ArrayList<>();

    ResponseEntity<Map<String, List<DtoCreditCardReader>>> responseEntity = restClient
      .get()
      .uri("/v0.1/merchants/{sumupApiMercantCode}/readers", sumupApiMercantCode)
      .header("Authorization", "Bearer " + sumupApiToken)
      .retrieve()
      .toEntity(new ParameterizedTypeReference<Map<String, List<DtoCreditCardReader>>>()
      {
      });

    if(responseEntity.getStatusCode().is2xxSuccessful())
    {
      log.debug("*** is2xxSuccessful");
      readersList = responseEntity.getBody().get("items");
    }

    log.debug("readersList={}", readersList);
    return readersList;
  }

  public String sumupMe()
  {
    String result = null;
    
    ResponseEntity<String> responseEntity = restClient
      .get()
      .uri("/v0.1/me")
      .header("Authorization", "Bearer " + sumupApiToken)
      .retrieve()
      .toEntity(new ParameterizedTypeReference<String>()
      {
      });

    if(responseEntity.getStatusCode().is2xxSuccessful())
    {
      log.debug("*** is2xxSuccessful");
      result = responseEntity.getBody();
    }

    log.debug("result={}", result);
    return result;
  }

  
  /*
  
  TODO: Implement the following API-calls
  
  https://developer.sumup.com/api/readers/create-reader
  https://developer.sumup.com/api/readers/get-reader
  https://developer.sumup.com/api/readers/delete-reader
  https://developer.sumup.com/api/readers/update-reader
  https://developer.sumup.com/api/readers/create-reader-checkout
  https://developer.sumup.com/api/readers/create-reader-terminate
  
  https://developer.sumup.com/api/members/list-merchant-members
  https://developer.sumup.com/api/receipts/get
  
  https://developer.sumup.com/api/merchant/get
  
  https://developer.sumup.com/api/transactions/list-detailed
  https://developer.sumup.com/api/transactions/get
  
  */
  
}
