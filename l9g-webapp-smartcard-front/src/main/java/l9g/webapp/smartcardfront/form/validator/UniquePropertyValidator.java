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
import l9g.webapp.smartcardfront.db.PosPropertiesRepository;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.form.model.FormProperty;
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
public class UniquePropertyValidator implements
  ConstraintValidator<UniqueProperty, FormProperty>
{
  private final PosPropertiesRepository posPropertiesRepository;

  @Override
  public boolean isValid(FormProperty formProperty,
    ConstraintValidatorContext context)
  {
    log.debug("formProperty={}", formProperty);
    
    if(formProperty == null || formProperty.getKey() == null
      || formProperty.getKey().isEmpty())
    {
      return true;
    }

    Optional<PosProperty> optional 
      = posPropertiesRepository
        .findByTenant_IdAndKey(formProperty.getTenantId(), formProperty.getKey());
    
    if( ! optional.isEmpty() &&  ! optional.get().getId().equals(formProperty.getId()))
    {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("key")
        .addConstraintViolation();
      return false;
    }

    return true;
  }

}
