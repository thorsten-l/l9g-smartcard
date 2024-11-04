/*
 * Copyright 2024 Thorsten Ludewig <t.ludewig@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package l9g.webapp.smartcardfront.db.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import java.util.UUID;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Slf4j
public class UniqueTenantValidator implements
  ConstraintValidator<UniqueTenant, PosTenant>
{
  @Autowired
  private PosTenantsRepository posTenantsRepository;

  private String uuid = UUID.randomUUID().toString();

  @Override
  public boolean isValid(PosTenant formTenant,
    ConstraintValidatorContext context)
  {
    log.debug("uuid={}", uuid);
    log.debug("formTenant={}", formTenant);
    log.debug("posTenantsRepository is available {}", (posTenantsRepository != null ? "true" : "false"));

    if(posTenantsRepository == null)
    {
      return true;
    }

    if(formTenant == null || formTenant.getName() == null
      || formTenant.getName().isEmpty())
    {
      return true;
    }

    Optional<PosTenant> optional = posTenantsRepository.findByName(formTenant.getName().trim());
    if( ! optional.isEmpty() &&  ! optional.get().getId().equals(formTenant.getId()))
    {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("name")
        .addConstraintViolation();
      return false;
    }

    optional = posTenantsRepository.findByShorthand(formTenant.getShorthand().trim());
    if( ! optional.isEmpty() &&  ! optional.get().getId().equals(formTenant.getId()))
    {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("shorthand")
        .addConstraintViolation();
      return false;
    }

    return true;
  }

}
