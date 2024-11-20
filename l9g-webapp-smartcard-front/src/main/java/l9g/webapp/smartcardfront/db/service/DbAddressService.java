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
package l9g.webapp.smartcardfront.db.service;

import java.util.List;
import l9g.webapp.smartcardfront.db.PosAddressesRepository;
import l9g.webapp.smartcardfront.db.model.PosAddress;
import l9g.webapp.smartcardfront.form.FormPosMapper;
import l9g.webapp.smartcardfront.form.model.FormAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DbAddressService
{

  private final DbUserService userService;

  private final PosAddressesRepository posAddressesRepository;

  public List<PosAddress> findAllAddresses(DefaultOidcUser principal)
  {

    if(userService.isAdmin(principal))
    {
      return posAddressesRepository.findAllByOrderByNameAsc();
    }
    else
    {
      throw new AccessDeniedException("Permission denied.");
    }
  }

  public PosAddress adminFindAddressById(String id, DefaultOidcUser principal)
  {
    PosAddress posAddress = null;

    if(userService.isAdmin(principal) && id != null)
    {
      posAddress = posAddressesRepository.findById(id).orElseThrow(()
        -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address not found"));
      log.debug("adminFindAddressById: {}", posAddress);
    }
    else
    {
      throw new AccessDeniedException("You are not allowed to create or update address.");
    }
    return posAddress;
  }
  
  public PosAddress adminCloneAddressById(String id, DefaultOidcUser principal)
  {
    PosAddress posAddress = null;

    if(userService.isAdmin(principal) && id != null)
    {
      posAddress = posAddressesRepository.findById(id).orElseThrow(()
        -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address not found"));
      log.debug("adminFindAddressById: {}", posAddress);
      PosAddress clone = new PosAddress(userService.gecosFromPrincipal(principal));
      clone.setName(clone.getId());
      FormPosMapper.INSTANCE.updatePosAddressFromPosAddress(posAddress, clone);
      posAddress = posAddressesRepository.saveAndFlush(clone);
    }
    else
    {
      throw new AccessDeniedException("You are not allowed to create or update address.");
    }
    return posAddress;
  }

  
  
  public PosAddress adminSaveAddress(String id, FormAddress formAddress, DefaultOidcUser principal)
  {
    PosAddress posAddress;


    if("add".equals(id))
    {
      log.debug("add new address");
      posAddress = new PosAddress(userService.gecosFromPrincipal(principal));
    }
    else
    {
      posAddress = adminFindAddressById(id, principal);
    }
    
    posAddress.setModifiedBy(userService.gecosFromPrincipal(principal));
    FormPosMapper.INSTANCE.updatePosAddressFromFormAddress(formAddress, posAddress);
    log.debug("posAddress = {}", posAddress);
    return posAddressesRepository.saveAndFlush(posAddress);
  }
   
  public PosAddress adminDeleteAddress(String id, DefaultOidcUser principal)
  {
    log.debug("delete tenant: {} with principal {}", id, principal.getName());
    PosAddress address = posAddressesRepository.findById(id).orElseThrow(
      () -> new AccessDeniedException("Unknown tenant id"));

    if(userService.isAdmin(principal))
    {
      posAddressesRepository.deleteById(id);
      posAddressesRepository.flush();
    }

    return address;
  }

}
