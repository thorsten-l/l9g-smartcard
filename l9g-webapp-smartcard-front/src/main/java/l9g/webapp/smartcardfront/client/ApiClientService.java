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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import l9g.smartcard.dto.DtoCreditCardReader;
import l9g.smartcard.dto.DtoMe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiClientService
{
  private final OAuth2AuthorizedClientManager authorizedClientManager;

  private final OAuth2AuthorizedClientService authorizedClientService;

  private final RestClient.Builder builder;

  private RestClient restClient;

  @Value("${app.api.base-url}")
  private String appApiBaseUrl;

  @Value("${app.oauth2.registration-id}")
  private String appOAuth2RegistrationId;

  @Getter
  @Value("${app.debug-oidc}")
  private boolean appDebugOidc;

  @PostConstruct
  private void initialize()
  {
    log.debug("initialize appApiBaseUrl={}", appApiBaseUrl);
    restClient = builder.baseUrl(appApiBaseUrl).build();
  }

  public Map<String, String> buildInfo()
  {
    log.debug("buildInfo");

    return restClient
      .get()
      .uri("/api/v1/buildinfo")
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<Map<String, String>>()
      {
      });
  }

  public Map<String, String> findPersonById(String personId)
  {
    log.debug("findPersonById");

    return restClient
      .get()
      .uri("/api/v1/person/id/{personId}", personId)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<Map<String, String>>()
      {
      });
  }

  public Map<String, String> findPersonBySerial(long serial)
  {
    log.debug("findBySerial");

    return restClient
      .get()
      .uri("/api/v1/person/serial/{serial}", serial)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<Map<String, String>>()
      {
      });
  }

  public List<Map<String, String>> findPersonsByTerm(String term)
  {
    log.debug("findBySerial");

    return restClient
      .get()
      .uri(uriBuilder -> uriBuilder
      .path("/api/v1/person/term")
      .queryParam("term", term)
      .build()
      )
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<Map<String, String>>>()
      {
      });
  }

  public List<DtoCreditCardReader> findAllCreditCardReaders()
  {
    log.debug("findAllCreditCardReaders");
    List<DtoCreditCardReader> result = restClient
      .get()
      .uri(uriBuilder -> uriBuilder
      .path("/api/v1/creditcardreader")
      .build()
      )
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoCreditCardReader>>()
      {
      });

    log.debug("result={}", result);
    return result;
  }

  public DtoCreditCardReader findCreditCardReaderById(String id)
  {
    log.debug("findByPersonId");

    return restClient
      .get()
      .uri("/api/v1/creditcardreader/{id}", id)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<DtoCreditCardReader>()
      {
      });
  }

  public DtoMe sumupMe()
  {
    log.debug("sumupMerchantsMe");
    DtoMe result = restClient
      .get()
      .uri(uriBuilder -> uriBuilder
      .path("/api/v1/me")
      .build()
      )
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<DtoMe>()
      {
      });

    log.debug("result={}", result);
    return result;
  }

  /*
  public List<DtoPerson> findAllPersons()
  {
    log.debug("findPerson");

    return restClient
      .get()
      .uri("/api/v1/person")
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoPerson>>()
      {
      });
  }

  public List<DtoApplicationPermission> findApplicationPermissions()
  {
    log.debug("findApplicationPermissions");

    return restClient
      .get()
      .uri("/api/v1/application-permission")
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoApplicationPermission>>()
      {
      });
  }

  public List<DtoApplicationPermission> findPersonPermissions(String id)
  {
    log.debug("findPersonPermissions");

    return restClient
      .get()
      .uri("/api/v1/application-permission/{id}/persons", id)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoApplicationPermission>>()
      {
      });
  }

  public DtoApplicationPermission findOwnPersonPermissions(String id)
  {
    log.debug("findOwnPersonPermissions application id={}", id);

    return restClient
      .get()
      .uri("/api/v1/application-permission/{id}/own", id)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<DtoApplicationPermission>()
      {
      });
  }

  public List<DtoApplication> findApplications()
  {
    log.debug("findApplications");

    return restClient
      .get()
      .uri("/api/v1/application")
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoApplication>>()
      {
      });
  }

  public DtoApplication findApplicationById(String id)
  {
    log.debug("findApplications {}", id);

    return restClient
      .get()
      .uri("/api/v1/application/{id}", id)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<DtoApplication>()
      {
      });
  }

  public DtoErrorStatus baseTopicUnique(String id, String baseTopic)
  {
    log.debug("baseTopicUnique {} '{}'", id, baseTopic);

    return restClient
      .get()
      .uri("/api/v1/application/{id}/basetopic/{baseTopic}", id, baseTopic)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<DtoErrorStatus>()
      {
      });
  }

  public DtoErrorStatus deleteApplicationById(String id)
  {
    log.debug("deleteApplicationById({})", id);

    ResponseEntity<DtoErrorStatus> response = restClient
      .delete()
      .uri("/api/v1/application/{id}", id)
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .toEntity(DtoErrorStatus.class);

    if (response.getStatusCode() != HttpStatus.OK)
    {
      throw new RuntimeException("Unexpected HTTP status: "
        + response.getStatusCode() + " " + response.toString());
    }

    return response.getBody();
  }

  public DtoApplication createApplication(DtoApplication app)
  {
    log.debug("createApplication");

    return restClient
      .put()
      .uri("/api/v1/application")
      .header("Authorization", "Bearer " + getBearer())
      .body(app)
      .retrieve()
      .body(new ParameterizedTypeReference<DtoApplication>()
      {
      });
  }

  public DtoApplication updateApplication(DtoApplication app, String id)
  {
    log.debug("updateApplication");

    return restClient
      .post()
      .uri("/api/v1/application/{id}", id)
      .header("Authorization", "Bearer " + getBearer())
      .body(app)
      .retrieve()
      .body(new ParameterizedTypeReference<DtoApplication>()
      {
      });
  }

  public DtoApplicationPermission updateApplicationPermission(
    String id, DtoApplicationPermission applicationPermission)
  {
    log.debug("updateApplicationPermission id={}, applicationPermission={}", id,
      applicationPermission);

    return restClient
      .post()
      .uri("/api/v1/application-permission/{id}", id)
      .header("Authorization", "Bearer " + getBearer())
      .body(applicationPermission)
      .retrieve()
      .body(new ParameterizedTypeReference<DtoApplicationPermission>()
      {
      });
  }

  public List<DtoPerson> adminFindAllPersons()
  {
    log.debug("adminFindAllPerson");

    return restClient
      .get()
      .uri("/api/v1/admin/person")
      .header("Authorization", "Bearer " + getBearer())
      .retrieve()
      .body(new ParameterizedTypeReference<List<DtoPerson>>()
      {
      });
  }
   */
  public OAuth2AccessToken getAccessToken()
  {
    log.debug("getAccessToken={}", appOAuth2RegistrationId);

    Authentication authentication =
      SecurityContextHolder.getContext().getAuthentication();

    OAuth2AuthorizedClient authorizedClient = authorizedClientService
      .loadAuthorizedClient(appOAuth2RegistrationId, authentication.getName());

    if(authorizedClient == null || authorizedClient.getAccessToken() == null
      || Instant.now().isAfter(authorizedClient.getAccessToken().getExpiresAt()))
    {
      OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.
        withClientRegistrationId(appOAuth2RegistrationId).principal(
        authentication).
        build();

      authorizedClient = authorizedClientManager.authorize(
        authorizeRequest);
    }

    log.debug("access token expires={}", authorizedClient.getAccessToken().
      getExpiresAt());

    return authorizedClient.getAccessToken();
  }

  public String getBearer()
  {
    log.debug("getBearer={}", appOAuth2RegistrationId);
    return getAccessToken().getTokenValue();
  }

}
