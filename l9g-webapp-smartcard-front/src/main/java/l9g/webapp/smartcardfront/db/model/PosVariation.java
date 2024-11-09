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
import lombok.AllArgsConstructor;
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
@Table(name = "variations", uniqueConstraints =
     {
       @UniqueConstraint(columnNames =
       {
         "product_id", "name"
       })
})
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PosVariation extends PosUuidObject
{
  private static final long serialVersionUID = -8043510890118920536L;

  public PosVariation(String createdBy, PosProduct product,
    String name, double price, double tax)
  {
    super(createdBy);
    this.product = product;
    this.name = name;
    this.price = price;
    this.tax = tax;
  }

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  @ToString.Exclude
  private PosProduct product;

  @Column(nullable = false)
  private String name;

  private double tax;

  private double price;

}
