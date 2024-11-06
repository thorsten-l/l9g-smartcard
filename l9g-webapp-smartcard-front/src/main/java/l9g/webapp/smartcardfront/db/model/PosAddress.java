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
package l9g.webapp.smartcardfront.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "addresses")
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PosAddress extends PosUuidObject
{
  private static final long serialVersionUID = 1200101847693707621L;

  public PosAddress(String createdBy)
  {
    super(createdBy);
  }

  private String name;

  @Column(length = 2048)
  private String description;

  private String addressLine1;

  private String addressLine2;

  private String postalCode;

  private String city;

  private String state;

  private String country;

  private String telephonenumber;

  private String email;

  private String taxnumber;

}
