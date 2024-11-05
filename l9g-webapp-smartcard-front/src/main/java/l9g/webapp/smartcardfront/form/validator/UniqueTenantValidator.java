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
package l9g.webapp.smartcardfront.form.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import l9g.webapp.smartcardfront.db.PosTenantsRepository;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.form.model.FormTenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniqueTenantValidator implements
  ConstraintValidator<UniqueTenant, FormTenant>
{
  private final PosTenantsRepository posTenantsRepository;

  @Override
  public boolean isValid(FormTenant formTenant,
    ConstraintValidatorContext context)
  {
    log.debug("formTenant={}", formTenant);

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
