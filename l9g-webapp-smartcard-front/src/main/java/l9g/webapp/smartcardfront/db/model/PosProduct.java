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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "products", uniqueConstraints =
     {
       @UniqueConstraint(columnNames =
       {
         "category_id", "name"
       })
})
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PosProduct extends PosUuidObject
{
  private static final long serialVersionUID = 7718553203197438944L;

  public PosProduct(String createdBy, PosCategory category, String name,
    String description, double price, double tax)
  {
    super(createdBy);
    this.category = category;
    this.name = name;
    this.description = description;
    this.price = price;
    this.tax = tax;
  }

  public PosProduct(String createdBy, PosCategory category, String name)
  {
    this(createdBy, category, name, null, 0.0, 0.0);
  }

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  @ToString.Exclude
  private PosCategory category;

  @Column(nullable = false)
  private String name;

  @Column(length = 2048)
  private String description;

  private double tax = 1.0;

  private double price;

  @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE,
             fetch = FetchType.EAGER)
  private List<PosVariation> variations = new ArrayList<>();

}
