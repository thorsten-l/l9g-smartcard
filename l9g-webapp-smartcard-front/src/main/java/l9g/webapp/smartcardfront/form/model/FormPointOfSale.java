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
package l9g.webapp.smartcardfront.form.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import l9g.webapp.smartcardfront.form.validator.UniquePointOfSale;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@UniquePointOfSale(message = "{error.pointOfSale.name.unique}")
public class FormPointOfSale
{

  private String id;

  @NotBlank(message = "{error.name.notBlank}")
  private String name;

  private String tenantId;

  private String addressId;

  private String sumupReaderId;

  private double cashRegisterBalance;

  private double cashFloat;

  private boolean cardIssuing;

  private boolean cardPayment;

  private boolean hidden;

}
