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
package l9g.smartcard.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoMerchantProfile
{
  private int id;

  @JsonProperty("merchant_code")
  private String merchantCode;

  @JsonProperty("company_name")
  private String companyName;

  @JsonProperty("legal_type")
  private DtoLegalType legalType;

  @JsonProperty("merchant_category_code")
  private String merchantCategoryCode;

  @JsonProperty("mobile_phone")
  private String mobilePhone;

  private DtoAddress address;

  @JsonProperty("business_owners")
  private ArrayList<Object> businessOwners;

  @JsonProperty("doing_business_as")
  private DtoDoingBusinessAs doingBusinessAs;

  private String locale;

  private boolean complete;

  private boolean extdev;

  private String country;

  @JsonProperty("default_currency")
  private String defaultCurrency;

  @JsonProperty("third_party_business_details_confirmed")
  private boolean thirdPartyBusinessDetailsConfirmed;

}
