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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "point_of_sale")
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosPointOfSale extends PosUuidObject
{
  private static final long serialVersionUID = 4418213295209635591L;

  public PosPointOfSale(String createdBy, PosTenant tenant, String name)
  {
    super(createdBy);
    this.tenant = tenant;
    this.name = name;
  }

  @JsonView(View.PointsOfSale.class)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tenant_id", nullable = false)
  private PosTenant tenant;

  @JsonView(View.Base.class)
  @Column(unique = true, nullable = false)
  private String name;

  @JsonView(View.Base.class)
  @Column(name = "sumup_reader_id", unique = true)
  private String sumupReaderId;

  @JsonView(View.Base.class)
  @Column(name = "sumup_reader_name", unique = true)
  private String sumupReaderName;

  @JsonView(View.Base.class)
  @Column(name = "cash_register_balance", nullable = false)
  private double cashRegisterBalance; // Summe des gesamten Bargeldbestandes

  @JsonView(View.Base.class)
  @Column(name = "cash_float", nullable = false)
  private double cashFloat; // Wechselgeldeinlage in einer Registrierkasse z.B. 50€

  @JsonView(View.Base.class)
  @Column(name = "card_issuing")
  private boolean cardIssuing;

  @JsonView(View.Base.class)
  @Column(name = "card_payment")
  private boolean cardPayment;

  @JsonView(View.PointsOfSale.class)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "address_id")
  private PosAddress address;

}
