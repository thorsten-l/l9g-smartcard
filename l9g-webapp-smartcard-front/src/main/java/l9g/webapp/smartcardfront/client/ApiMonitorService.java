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
package l9g.webapp.smartcardfront.client;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiMonitorService
{
  private final RestClient.Builder builder;

  private RestClient restClient;

  @PostConstruct
  private void initialize()
  {
    log.debug("initialize");
    restClient = builder.baseUrl("http://localhost:38080").build();
  }

  public Map<String, String> buildInfo()
  {
    log.debug("buildInfo");

    return restClient
      .get()
      .uri("/api/v1/buildinfo")
      .retrieve()
      .body(new ParameterizedTypeReference<Map<String, String>>()
      {
      });
  }

  public String pointOfSalesName()
  {
    String value = null;
    log.debug("pointOfSalesName");

    try
    {
      ResponseEntity<Map<String, String>> response = restClient
        .get()
        .uri("/api/v1/pos-name")
        .retrieve()
        .toEntity(new ParameterizedTypeReference<Map<String, String>>()
        {
        });

      if(response.getStatusCode().is2xxSuccessful())
      {
        Map<String, String> result = response.getBody();
        if(result != null)
        {
          value = result.get("pos-name");
        }
      }
    }
    catch(Throwable t)
    {
      // ignore all errors
      log.error("{}", t.getMessage());
    }

    return value;
  }

}
