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
import java.util.Date;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoMe
{
  private int id;

  private DtoAccount account;

  @JsonProperty("personal_profile")
  private DtoPersonalProfile personalProfile;

  @JsonProperty("merchant_profile")
  private DtoMerchantProfile merchantProfile;

  private ArrayList<Object> requirements;

  private ArrayList<Object> verifications;

  @JsonProperty("is_migrated_payleven_br")
  private boolean migratedPaylevenBr;

  @JsonProperty("signup_time")
  private Date signupTime;

  @JsonProperty("details_submitted")
  private boolean detailsSubmitted;

}
