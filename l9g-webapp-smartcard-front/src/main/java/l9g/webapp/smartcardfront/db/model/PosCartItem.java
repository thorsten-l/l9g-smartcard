/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.db.model;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Setter;
import l9g.webapp.smartcardfront.db.model.PosProduct;

/**
 *
 * @author kevin
 */
@AllArgsConstructor
@Getter
@Setter
public class PosCartItem
{
  private PosProduct product;

  private int quantity;

  public PosCartItem(PosProduct product)
  {

    this.product = product;
    this.quantity = 1;
  }

  public void incrementQuantity()
  {
    this.quantity++;
  }

}
