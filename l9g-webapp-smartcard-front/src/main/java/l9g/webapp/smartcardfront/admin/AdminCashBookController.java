/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package l9g.webapp.smartcardfront.admin;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosTransaction;
import l9g.webapp.smartcardfront.db.service.DbPointOfSaleService;
import l9g.webapp.smartcardfront.db.service.DbTenantService;
import l9g.webapp.smartcardfront.db.service.DbTransactionsService;
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


/**
 *
 * @author kevin
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminCashBookController
{

  private final DbTransactionsService dbTransactionService;

  private final DbPointOfSaleService dbPointOfSaleService;
  
  private final DbTenantService dbTenantService;
  
  private final AdminService adminService;
  
  private static final String ACTIVE_PAGES = "cashbook";

  @GetMapping("/admin/cashbook")
  public String showAllCashbooks(@AuthenticationPrincipal DefaultOidcUser principal,
    Model model, HttpSession session)
  {
    adminService.generalModel(principal, model, session, ACTIVE_PAGES);
    
    if(principal != null)
    {
      log.debug("Benutzerrollen: {}", principal.getAuthorities());
    }
    
    List<PosTenant> tenant = dbTenantService.getAllTenants(session, principal);
    model.addAttribute("tenant", tenant);
    
    return "admin/salesbookSelection";
  }
  
  
  @GetMapping("/admin/cashbook/{id}")
  public String showCashbook(
  @RequestParam(required = false) String quarter, 
  @RequestParam(required = false) String pointOfSaleId,
  @PathVariable ("id") String tenantId,
  Model model, HttpSession session, 
  @AuthenticationPrincipal DefaultOidcUser principal)
  
  {
    adminService.generalModel(principal, model, session, "cashbook");
    if(principal != null)
    {
      log.debug("Benutzerrollen: {}", principal.getAuthorities());
    }

    List<PosTransaction> transactions = dbTransactionService.findAllTransactionsByTenant(tenantId);    
    List<PosPointOfSale> allPointOfSales = dbPointOfSaleService.findAllPointsOfSale(session, principal);

    log.debug("Alle Verkaufsstellen: {}", allPointOfSales);

      // Filter: Verkaufsstelle
    if(pointOfSaleId != null && !"all".equalsIgnoreCase(pointOfSaleId))
    {
      transactions = transactions.stream()
        .filter(t -> t.getPointOfSales() != null
        && pointOfSaleId.equalsIgnoreCase(t.getPointOfSales().getName()))
        .collect(Collectors.toList());
    }

    //Filter: Quartal
    if(quarter != null && !"in_total".equals(quarter))
    {
      try
      {
        int q = Integer.parseInt(quarter);
        transactions = transactions.stream()
          .filter(t ->
          {
            if(t.getTenant() == null || t.getTenant().getCreateTimestamp() == null)
            {
              return false;
            }

            int month = t.getTenant().getCreateTimestamp()
              .toInstant()
              .atZone(java.time.ZoneId.systemDefault())
              .getMonthValue();

            return (q == 1 && month <= 3)
              || (q == 2 && month >= 4 && month <= 6)
              || (q == 3 && month >= 7 && month <= 9)
              || (q == 4 && month >= 10 && month <= 12);
          })
          .collect(Collectors.toList());
      }
      catch(NumberFormatException e)
      {
        log.warn("Ungültiger Quartalswert: {}", quarter);
      }
    }

    model.addAttribute("transactions", transactions);
    model.addAttribute("pointOfSales", allPointOfSales);
    model.addAttribute("selectedQuarter", quarter);
    model.addAttribute("selectedPointOfSaleId", pointOfSaleId);
    return "admin/salesbook";
  }
  
  

}

