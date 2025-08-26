/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.controller;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import l9g.webapp.smartcardfront.client.ApiClientService;
import l9g.webapp.smartcardfront.db.PosPointsOfSaleRepository;
import l9g.webapp.smartcardfront.db.model.PosCartItem;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import l9g.webapp.smartcardfront.db.model.PosTransactionProduct;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.db.service.DbProductService;
import l9g.webapp.smartcardfront.db.service.DbTransactionsService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

  private final PosPointsOfSaleRepository posPointsOfSaleRepository;

  private final ApiClientService apiClientService;

  private final DbTransactionsService dbTransactionsService;

  // -------------------------------------------------------------
  // Warenkorb / Übersicht
  // -------------------------------------------------------------
  @GetMapping("/posx/sales")
  public String receiptProducts(Model model, HttpSession session,
    @AuthenticationPrincipal DefaultOidcUser principal)
  {
    if(principal != null)
    {
      log.debug("Benutzerrollen: {}", principal.getAuthorities());
      log.debug("preferred_username={}", principal.getPreferredUsername());

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

  // -------------------------------------------------------------
  // Produkt in den Warenkorb
  // -------------------------------------------------------------
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

  // -------------------------------------------------------------
  // Checkout
  // -------------------------------------------------------------
  @PostMapping("/posx/sales/checkout")
  public String checkout(
    @AuthenticationPrincipal DefaultOidcUser principal,
    HttpSession session,
    @RequestParam("paymentType") String paymentType,
    @RequestParam("givenAmount") String givenAmountStr,
    @RequestParam("change") String changeStr,
    @RequestParam(value = "purpose", required = false) String purpose,
    @RequestParam(value = "cardSerial", required = false) String cardSerial,
    RedirectAttributes redirectAttributes
  )
  {
    double givenAmount = 0.0;
    try
    {
      givenAmount = Double.parseDouble(givenAmountStr.replace(",", "."));
    }
    catch(NumberFormatException e)
    {
      log.error("Fehler beim Parsen von givenAmount: {}", givenAmountStr, e);
    }
    
    Map<String, PosCartItem> cart = (Map<String, PosCartItem>) session.getAttribute("cart");

    if(cart == null || cart.isEmpty())
    {
      redirectAttributes.addFlashAttribute("error", "Der Warenkorb ist leer.");
      return "redirect:/posx/sales";
    }

    //Point Of Sales und Tenant holen
    String posName = (String) session.getAttribute("POINT_OF_SALES_NAME");
    Optional<PosPointOfSale> pointOfSaleOpt = posPointsOfSaleRepository.findByName(posName);

    if(pointOfSaleOpt.isEmpty())
    {
      log.error("Point Of Sale ‘{}‘ wirde nicht in der Datenbank gefunden", posName);
      redirectAttributes.addFlashAttribute("error", "Klassen-Konfiguration ist ungültig");
    }

    PosPointOfSale pointOfSale = pointOfSaleOpt.get();
    PosTenant tenant = pointOfSale.getTenant();

    //Transaktion erstellen
    PosTransaction transaction = new PosTransaction(principal.getPreferredUsername(), tenant, pointOfSale);
    transaction.setAmount(totalPriceIncl(cart));
    transaction.setAmountReceived(givenAmount);

    if(purpose != null && !purpose.isBlank())
    {
      transaction.setPurpose(purpose);
    }
    try
    {
      double changeAmount = Double.parseDouble(changeStr.replace(",", "."));
      transaction.setAmountRefundet(changeAmount);
    }
    catch(NumberFormatException e)
    {
      log.error("Fehler beim Parsen von change: {}", changeStr, e);
      transaction.setAmountRefundet(0.0);
    }
    String currency = getTenantProperty(tenant, "currency", "EUR");
    transaction.setCurrency(currency);

    transaction.setPaymentType(paymentType);
    transaction.setStatus("COMPLETED");
    transaction.setCashier(principal.getPreferredUsername());
    transaction.setPurpose(purpose);

    //Kundendaten von Karte holen
    @SuppressWarnings("unchecked")
    Map<String, String> customerData = (Map<String, String>) session.getAttribute("activeCustomer");
    if(customerData != null)
    {
      log.debug("Kundendaten für Transaktion gefunden: {}", customerData);
      transaction.setCustomerName(customerData.getOrDefault("sn", ""));
      transaction.setCustomerEmail(customerData.getOrDefault("mail", ""));
      transaction.setCustomerNumber(customerData.getOrDefault("soniaCustomerNumber", ""));
      transaction.setCustomerCardSerial(cardSerial);
    }
    else
    {
      log.debug("Keine Kundendaten für Kartenserial {} gefunden", cardSerial);
    }

    //Transaktion und Produkte speichern
    try
    {
      dbTransactionsService.saveTransaction(transaction);
      log.info("Transaktion {} erfolgreich gespeichert", transaction.getId());

      for(PosCartItem item : cart.values())
      {
        PosTransactionProduct txProduct = new PosTransactionProduct(principal.getPreferredUsername(),
          transaction, item.getProduct().getName());
        txProduct.setDescription(item.getProduct().getDescription());
        txProduct.setTax(item.getProduct().getTax());
        txProduct.setPrice(item.getProduct().getPrice() * item.getQuantity());
        dbTransactionsService.saveTransactionProduct(txProduct);
      }
      log.info("{} Produkte für Transaktion {} gespeichert.", cart.size(), transaction.getId());
      session.removeAttribute("cart");
      session.removeAttribute("activeCustomer");
      log.info("Warenkorb wurde geleert");
      redirectAttributes.addFlashAttribute("success", "Checkout erfolgreicht");
      return "redirect:/posx/home";
    }
    catch(Exception e)
    {
      log.error("Fehler beim Speichern der Transatkion.", e);
      redirectAttributes.addFlashAttribute("error", "Transaktion konnte nicht gespeichert werden");
      return "redirect:/posx/sales";
    }

  }

  // -------------------------------------------------------------
  // Warenkorb leeren
  // -------------------------------------------------------------
  @PostMapping("/posx/sales/clearCart")
  public String clearCart(HttpSession session)
  {
    session.removeAttribute("cart");
    log.info("Warenkorb wurde geleert.");

    return "redirect:/posx/sales";
  }

  // -------------------------------------------------------------
  // Hilfsmethode für Gesamtsumme
  // -------------------------------------------------------------
  private double totalPriceIncl(Map<String, PosCartItem> cart)
  {
    double total = 0.0;
    for(PosCartItem item : cart.values())
    {
      double price = item.getProduct().getPrice();
      double taxRate = item.getProduct().getTax();
      int quantity = item.getQuantity();
      double taxPerItem = price * taxRate / 100.0;
      total += (price + taxPerItem) * quantity;
    }
    return total;
  }

  // -------------------------------------------------------------
  // Hilfsmethode um eine Eigenschaft des Mandanten sicher abzugreifen
  // -------------------------------------------------------------
  private String getTenantProperty(PosTenant tenant, String key, String defaultValue)
  {
    if(tenant != null && tenant.getProperties() != null)
    {
      for(PosProperty prop : tenant.getProperties())
      {
        if(key.equalsIgnoreCase(prop.getKey()))
        {
          return prop.getValue();
        }
      }
    }
    return defaultValue;
  }

}
