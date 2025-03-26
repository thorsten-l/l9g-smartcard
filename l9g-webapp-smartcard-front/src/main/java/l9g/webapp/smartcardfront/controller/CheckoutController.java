/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.controller;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l9g.webapp.smartcardfront.db.model.PosCartItem;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.db.service.DbProductService;
import l9g.webapp.smartcardfront.db.service.DbVariationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author kevin
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class CheckoutController
{

  private final DbProductService dbProductService;

  private final DbVariationService dbVariationService;

  @GetMapping("/posx/sales")
  public String receiptProducts(Model model, HttpSession session, @AuthenticationPrincipal DefaultOidcUser principal)
  {
    if(principal != null)
    {
      log.debug("Benutzerrollen: {}", principal.getAuthorities());
    }
    List<PosProduct> products = dbProductService.ownerFindAllProducts(session, principal);
    Map<String, PosCartItem> cart = (Map<String, PosCartItem>) session.getAttribute("cart");
    if(cart == null)
    {
      cart = new HashMap<>();
    }
    
    double totalTax = 0.0;
    double totalPriceExel = 0.0;
    double totalPriceIncl = 0.0;
    
    for(PosCartItem item : cart.values()){
    double price = item.getProduct().getPrice();
    double tax = item.getProduct().getTax();
    int quantity = item.getQuantity();
    totalTax += tax * quantity;
    totalPriceExel += price * quantity;
    totalPriceIncl += (price + tax) * quantity;
    
    }

    model.addAttribute("products", products);
    model.addAttribute("cart", cart);
    model.addAttribute("totalTax", totalTax);
    model.addAttribute("totalPriceExel", totalPriceExel);
    model.addAttribute("totalPriceIncl", totalPriceIncl);
    return "posx/sales";
  }

  @PostMapping("/posx/sales/addToCart/{productId}/{variationId}")
  public String addToCart(
    @PathVariable("productId") String productId,
    @PathVariable(value = "variationId", required = false) String variationId,
    HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal, Model model)
  {

    log.debug("Füge Produkt mit ID: {} und Variation mit ID: {} zum Warenkorb hinzu", productId, variationId);

    PosProduct product = dbProductService.ownerGetProductById(productId, session, principal);
    if(product == null)
    {
      return "redirect:/posx/sales";
    }

    if(variationId != null)
    {
      PosVariation variation = dbVariationService.ownerGetVariationById(variationId, session, principal);
      if(variation != null)
      {
        product.setName(product.getName() + " - " + variation.getName());
        product.setPrice(variation.getPrice());
        product.setTax(variation.getTax());
      }
    }

    Map<String, PosCartItem> cart = (Map<String, PosCartItem>) session.getAttribute("cart");
    if(cart == null)
    {
      cart = new HashMap<>();
      session.setAttribute("cart", cart);
    }

    if(cart.containsKey(product.getId()))
    {
      
      cart.get(product.getId()).incrementQuantity();
    }
    else
    {
      
      cart.put(product.getId(), new PosCartItem(product));
    }

    return "redirect:/posx/sales";
  }

  // Leert den Warenkorb
  @PostMapping("/posx/sales/clearCart")
  public String clearCart(@AuthenticationPrincipal DefaultOidcUser principal, HttpSession session)
  {
    session.removeAttribute("cart");
    return "redirect:/posx/sales";
  }

  @PostMapping("/posx/sales/checkout")
  public String checkout(@AuthenticationPrincipal DefaultOidcUser principal, HttpSession session)
  {
    session.removeAttribute("cart");
    return "redirect:/posx/customer";
  }

}
