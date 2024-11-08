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
@Table(name = "transactions")
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PosTransaction extends PosUuidObject
{
  private static final long serialVersionUID = -3887176763514856101L;

  public PosTransaction(String createdBy, PosTenant tenant,
    PosPointOfSales pointOfSales)
  {
    super(createdBy);
    this.tenant = tenant;
    this.pointOfSales = pointOfSales;
  }

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
  private PosTenant tenant;

  @ManyToOne
  @JoinColumn(name = "point_of_sales_id")
  private PosPointOfSales pointOfSales;

  private String cashier;

  private double amount;

  @Column(name = "amount_received")
  private double amountReceived;
  
  @Column(name = "amount_refundet")
  private double amountRefundet;

  private String currency;

  @Column(name = "customer_name")
  private String customerName;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "customer_email_sent")
  private boolean customerEmailSent;

  @Column(name = "customer_number")
  private String customerNumber;

  @Column(name = "customer_card_serial")
  private String customerCardSerial;

  @Column(name = "payment_type")
  private String paymentType;

  private String purpose;

  private String status;

  @Column(name = "sumup_user")
  private String sumupUser;

  @Column(name = "sumup_transaction_id")
  private String sumupTransactionId;

}
