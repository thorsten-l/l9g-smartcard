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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "properties", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "p_key"})
})
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PosProperty extends PosUuidObject
{
  private static final long serialVersionUID = 2377357483632195188l;

  public PosProperty(String createdBy, PosTenant tenant, String key, String value)
  {
    super(createdBy);
    this.tenant = tenant;
    this.key = key;
    this.value = value;
  }

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false)
  private PosTenant tenant;

  @Column(name = "p_key", nullable = false)
  @NotBlank(message = "{error.key.notBlank}")
  private String key;

  @Column(name = "p_value", length = 2048)
  private String value;

}
