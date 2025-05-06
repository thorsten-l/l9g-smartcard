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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

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
    log.debug("Geladene Produkte: {}", products);

    Map<String, PosCartItem> cart = (Map<String, PosCartItem>) session.getAttribute("cart");

    if(cart == null)
    {
      log.debug("Kein Warenkorb in der Session gefunden. Initialisiere neuen Warenkorb.");
      cart = new HashMap<>();
    }
    else
    {
      log.debug("Warenkorb aus der Session geladen: {}", cart);
    }

    double totalTax = 0.0;
    double totalPriceExcl = 0.0;
    double totalPriceIncl = 0.0;

    for(PosCartItem item : cart.values())
    {
      double price = item.getProduct().getPrice();
      double taxRate = item.getProduct().getTax();  // Steuer in Prozent (z.B. 19 für 19%)
      int quantity = item.getQuantity();

      double taxPerItem = price * taxRate / 100.0;
      totalTax += taxPerItem * quantity;
      totalPriceExcl += price * quantity;
      totalPriceIncl += (price + taxPerItem) * quantity;
    }

    log.debug("Steuer: {}, Preis exkl. MwSt: {}, Preis inkl. MwSt: {}", totalTax, totalPriceExcl, totalPriceIncl);

    model.addAttribute("products", products);
    model.addAttribute("cart", cart);
    model.addAttribute("totalTax", totalTax);
    model.addAttribute("totalPriceExel", totalPriceExcl);
    model.addAttribute("totalPriceIncl", totalPriceIncl);
    return "posx/sales";
  }

  @PostMapping("/posx/sales/addToCart")
  public String addToCart(
    @RequestParam("productId") String productId,
    @RequestParam(value = "variationId", required = false) String variationId,
    HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal,
    Model model,
    @RequestHeader(value = "HX-Request", required = false) String hxRequest
  )
  {
    log.debug("Produkt ID: {}", productId);
    if(variationId != null)
    {
      log.debug("Variante ID: {}", variationId);
    }

    PosProduct product = dbProductService.ownerGetProductById(productId, session, principal);
    if(product == null)
    {
      log.debug("Produkt mit ID {} konnte nicht gefunden werden.", productId);
      return "redirect:/posx/sales";
    }

    String cartKey = product.getId();

    if(variationId != null)
    {
      PosVariation variation = dbVariationService.ownerGetVariationById(variationId, session, principal);
      if(variation != null)
      {
        product.setName(product.getName() + " - " + variation.getName());
        product.setPrice(variation.getPrice());
        product.setTax(variation.getTax());
        log.debug("Produkt nach Variation aktualisiert: {}", product);
        cartKey = variation.getId();
      }
      else
      {
        log.debug("Variante mit ID {} konnte nicht gefunden werden.", variationId);
      }
    }

    Map<String, PosCartItem> cart = (Map<String, PosCartItem>) session.getAttribute("cart");
    if(cart == null)
    {
      log.debug("Kein Warenkorb in der Session gefunden. Initialisiere neuen Warenkorb.");
      cart = new HashMap<>();
      session.setAttribute("cart", cart);
    }
    else
    {
      log.debug("Warenkorb aus der Session geladen: {}", cart);
    }

    cart.compute(cartKey, (key, item) ->
    {
      if(item == null)
      {
        log.debug("Produkt zum Warenkorb hinzugefügt: {}", product);
        return new PosCartItem(product);
      }
      item.incrementQuantity();
      return item;
    });

    // Berechnung der Steuer- und Gesamtsummen...
    double totalTax = 0.0;
    double totalPriceExcl = 0.0;
    double totalPriceIncl = 0.0;
    for(PosCartItem item : cart.values())
    {
      double price = item.getProduct().getPrice();
      double taxRate = item.getProduct().getTax();  // Steuer in Prozent
      int quantity = item.getQuantity();

      double taxPerItem = price * taxRate / 100.0;
      totalTax += taxPerItem * quantity;
      totalPriceExcl += price * quantity;
      totalPriceIncl += (price + taxPerItem) * quantity;
    }
    log.debug("Steuer: {}, Preis exkl. MwSt: {}, Preis inkl. MwSt: {}", totalTax, totalPriceExcl, totalPriceIncl);

    model.addAttribute("cart", cart);
    model.addAttribute("totalTax", totalTax);
    model.addAttribute("totalPriceExel", totalPriceExcl);
    model.addAttribute("totalPriceIncl", totalPriceIncl);

    if("true".equals(hxRequest))
    {
      return "fragments/cart-table :: cartTable";
    }
    return "redirect:/posx/sales";
  }

  @PostMapping("/posx/sales/clearCart")
  public String clearCart(@AuthenticationPrincipal DefaultOidcUser principal, HttpSession session)
  {
    log.debug("Warenkorb wird gelöscht.");
    session.removeAttribute("cart");
    return "redirect:/posx/sales";
  }

  @PostMapping("/posx/sales/checkout")
  public String checkout(@AuthenticationPrincipal DefaultOidcUser principal, HttpSession session)
  {
    log.debug("Checkout wird durchgeführt. Warenkorb wird gelöscht.");
    session.removeAttribute("cart");
    return "redirect:/posx/customer";
  }

}
