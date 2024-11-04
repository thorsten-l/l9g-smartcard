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
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import l9g.webapp.smartcardfront.db.validator.UniqueTenant;
import l9g.webapp.smartcardfront.json.View;
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
@Table(name = "tenants")
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@UniqueTenant(message = "{error.tenant.unique}")
public class PosTenant extends PosUuidObject
{
  private static final long serialVersionUID = 8748451088430252946L;

  public PosTenant(String createdBy, String name, boolean immutable)
  {
    super(createdBy, immutable);
    this.name = name;
  }

  public PosTenant(String createdBy, String name)
  {
    this(createdBy, name, false);
  }

  @JsonView(View.Base.class)
  @Column(name = "name", nullable = false, unique = true)
  @NotBlank(message = "{error.name.notBlank}")
  private String name;

  @JsonView(View.Base.class)
  @Column(name = "shorthand", unique = true)
  private String shorthand;

  @JsonView(View.Tenant.class)
  @ManyToOne
  @JoinColumn(name = "address_id")
  @ToString.Exclude
  private PosAddress address;

  @JsonView(View.Tenant.class)
  @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE,
             fetch = FetchType.EAGER)
  @ToString.Exclude
  private List<PosProperty> properties;

}
