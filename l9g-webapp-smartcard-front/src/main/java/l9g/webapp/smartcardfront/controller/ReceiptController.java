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
import org.springframework.web.bind.annotation.PathVariable;
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
public class ReceiptController {
    
    private final DbProductService dbProductService;
    private final DbVariationService dbVariationService;

    // Zeigt alle Produkte und den Warenkorb an
    @GetMapping("/posx/sales")
    public String receiptProducts(Model model, HttpSession session, @AuthenticationPrincipal DefaultOidcUser principal) {
        List<PosProduct> products = dbProductService.ownerFindAllProducts(session, principal);
        List<PosProduct> cart = (List<PosProduct>) session.getAttribute("cart");

        model.addAttribute("products", products);
        model.addAttribute("cart", cart != null ? cart : new ArrayList<>());
        return "posx/sales";
    }

    // Fügt ein Produkt in den Warenkorb hinzu, nutzt @PathVariable für die Produkt- und Variations-IDs
    @PostMapping("/posx/sales/addToCart/{productId}/{variationId}")
    public String addToCart(
        @PathVariable("productId") String productId,
        @PathVariable(value = "variationId", required = false) String variationId,
        HttpSession session,
        @AuthenticationPrincipal DefaultOidcUser principal) {
        
        log.debug("Füge Produkt mit ID: {} und Variation mit ID: {} zum Warenkorb hinzu", productId, variationId);

        // Produkt aus der Datenbank holen
        PosProduct product = dbProductService.ownerGetProductById(productId, session, principal);
        if (product == null) {
            return "redirect:/posx/sales";  // Produkt nicht gefunden, zurück zur Verkaufsseite
        }

        // Wenn eine Variation angegeben ist, diese hinzufügen
        if (variationId != null) {
            PosVariation variation = dbVariationService.ownerGetVariationById(variationId, session, principal);
            if (variation != null) {
                product.setName(product.getName() + " - " + variation.getName());
                product.setPrice(variation.getPrice());
                product.setTax(variation.getTax());
            }
        }

        // Warenkorb aus der Session holen
        List<PosProduct> cart = (List<PosProduct>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);  // Wenn der Warenkorb leer ist, erstelle einen neuen
        }
        
        cart.add(product);  // Produkt zum Warenkorb hinzufügen
        return "redirect:/posx/sales";  // Zurück zur Verkaufsseite
    }

    // Leert den Warenkorb
    @PostMapping("/posx/sales/clearCart")
    public String clearCart(HttpSession session) {
        session.removeAttribute("cart");
        return "redirect:/posx/sales";  // Zurück zur Verkaufsseite
    }

    // Checkout-Prozess, entfernt den Warenkorb und leitet zur Kunden-Seite weiter
    @PostMapping("/posx/sales/checkout")
    public String checkout(HttpSession session) {
        session.removeAttribute("cart");  // Warenkorb leeren
        return "redirect:/posx/customer";  // Weiterleitung zur Kunden-Seite
    }
}
