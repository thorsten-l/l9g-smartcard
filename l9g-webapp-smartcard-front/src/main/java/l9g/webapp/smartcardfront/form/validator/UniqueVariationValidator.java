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
import l9g.webapp.smartcardfront.db.PosVariationsRepository;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.form.model.FormVariation;
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
public class UniqueVariationValidator implements
  ConstraintValidator<UniqueVariation, FormVariation>
{
  private final PosVariationsRepository posVariationsRepository;

  @Override
  public boolean isValid(FormVariation formVariation,
    ConstraintValidatorContext context)
  {
    log.debug("Validaiton formVariation={}", formVariation);

    if(formVariation == null || formVariation.getName() == null
      || formVariation.getName().isEmpty())
    {
      return true;
    }

    Optional<PosVariation> optional =
      posVariationsRepository
        .findByProduct_IdAndName(formVariation.getProductId(), formVariation.getName());

    if(!optional.isEmpty() && !optional.get().getId().equals(formVariation.getId()))
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
