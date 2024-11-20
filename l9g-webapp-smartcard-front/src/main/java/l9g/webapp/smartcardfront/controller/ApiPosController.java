/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package l9g.webapp.smartcardfront.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.json.View;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import l9g.webapp.smartcardfront.db.PosPointsOfSaleRepository;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestController
@RequestMapping(path = "/api/v1/pos",
                produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ApiPosController
{
  private final PosPointsOfSaleRepository posPointsOfSalesRepository;
  
  @JsonView(View.PointsOfSale.class)
  @GetMapping("/{name}")
  public PosPointOfSale findByName(@PathVariable String name,
    HttpSession session)
  {
    PosPointOfSale result = null;
    log.debug("name = {}", name);
    
    session.setAttribute("POINT_OF_SALES_NAME", name);
    
    Optional<PosPointOfSale> optional =
      posPointsOfSalesRepository.findByName(name);
    
    if(optional.isPresent())
    {
      result = optional.get();
    }
    
    if(result != null)
    {
      log.info("point of sales = {}", result.getName());
      if ( result.getTenant() != null )
      {
         log.info("  - tenant = {}", result.getTenant().getName());
      }
    }
    else
    {
      log.warn("Point of sales '{}' not found.", name);
    }
    return result;
  }
  
}
