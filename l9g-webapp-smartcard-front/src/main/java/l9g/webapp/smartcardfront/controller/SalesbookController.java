/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import l9g.webapp.smartcardfront.db.service.DbPointOfSaleService;
import l9g.webapp.smartcardfront.db.service.DbTransactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *
 * @author kevin
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class SalesbookController
{

  private final DbTransactionsService dbTransactionService;

  private final DbPointOfSaleService dbPointOfSaleService;

  @GetMapping("/posx/salesbook")
  public String receiptProducts(Model model, HttpSession session, @AuthenticationPrincipal DefaultOidcUser principal)
  {
    if(principal != null)
    {
      log.debug("Benutzerrollen: {}", principal.getAuthorities());
    }
    List<PosTransaction> transactions = dbTransactionService.ownerFindAllTransactions(session, principal);
    List<PosPointOfSale> allPointOfSales = dbPointOfSaleService.findAllPointsOfSale(session, principal);

    model.addAttribute("transactions", transactions);
    model.addAttribute("pointOfSales", allPointOfSales);
    return "posx/salesbook";
  }

}
