/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.controller;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.db.service.DbProductService;
import l9g.webapp.smartcardfront.db.service.DbPropertyService;
import l9g.webapp.smartcardfront.db.service.DbService;
import l9g.webapp.smartcardfront.db.service.DbVariationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author kevin
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class ReceiptController
{

  private final DbProductService dbProductService;

  private final DbVariationService dbVariationService;

  private final DbPropertyService dbPropertyService;

  @GetMapping("/posx/sales")
  public String receiptProducts(Model model, HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.info("Lade Produktliste für Benutzer: {}", principal.getName());
    List<PosProduct> products = dbProductService.ownerFindAllProducts(session, principal);
    model.addAttribute("products", products);
    return "posx/sales";
  }

  @GetMapping("/posx/sales/variations")
  @ResponseBody
  public List<PosVariation> getProductVariations(@RequestParam("productId") String productId,
    HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.info("Lade Variationen für Produkt: {}", productId);
    PosProduct product = dbProductService.ownerGetProductById(productId, session, principal);
    if(product != null)
    {
      List<PosVariation> variations = dbVariationService.productFindAllVariations(productId);
      return variations != null ? variations : List.of();
    }
    log.warn("Produkt nicht gefunden: {}", productId);
    return List.of();
  }

  @PostMapping("/posx/sales/addToCart")
  @ResponseBody
  public List<PosProduct> addToCart(@RequestParam("productId") String productId,
    @RequestParam(value = "variationId", required = false) String variationId,
    HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    log.info("Füge Produkt zum Warenkorb hinzu: {} (Variation: {})", productId, variationId);

    PosProduct product = dbProductService.ownerGetProductById(productId, session, principal);
    if(product == null)
    {
      log.warn("Produkt nicht gefunden: {}", productId);
      return getCart(session);
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
      else
      {
        log.warn("Variation nicht gefunden: {}", variationId);
        double defaultTax = Double.parseDouble(
          dbPropertyService.getPropertyValue(session, principal, DbService.KEY_DEFAULT_TAX));
        product.setTax(defaultTax);
      }
    }

    List<PosProduct> cart = getCart(session);
    cart.add(product);
    session.setAttribute("cart", cart);
    return cart;
  }

  private List<PosProduct> getCart(HttpSession session)
  {
    List<PosProduct> cart = (List<PosProduct>) session.getAttribute("cart");
    return cart != null ? cart : new ArrayList<>();
  }

}
