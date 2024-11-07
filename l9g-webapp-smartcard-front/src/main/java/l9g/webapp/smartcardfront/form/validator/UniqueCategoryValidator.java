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
import l9g.webapp.smartcardfront.db.PosCategoriesRepository;
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.form.model.FormCategory;
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
public class UniqueCategoryValidator implements
  ConstraintValidator<UniqueCategory, FormCategory>
{
  private final PosCategoriesRepository posCategoriesRepository;

  @Override
  public boolean isValid(FormCategory formCategory,
    ConstraintValidatorContext context)
  {
    log.debug("formCategory={}", formCategory);
    
    if(formCategory == null || formCategory.getName() == null
      || formCategory.getName().isEmpty())
    {
      return true;
    }

    Optional<PosCategory> optional 
      = posCategoriesRepository
        .findByTenant_IdAndName(formCategory.getTenantId(), formCategory.getName());
    
    if( ! optional.isEmpty() &&  ! optional.get().getId().equals(formCategory.getId()))
    {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
        context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("name")
        .addConstraintViolation();
      return false;
    }

    return true;
  }

}
