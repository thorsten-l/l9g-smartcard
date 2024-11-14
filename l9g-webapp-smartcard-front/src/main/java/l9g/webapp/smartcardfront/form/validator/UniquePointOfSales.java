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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Constraint(validatedBy = UniqueProductValidator.class)
@Target(
{
  ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePointOfSales
{

  String message() default "{error.PointOfSales.name.unique}";

  Class<?>[] groups() default 
  {
  };

  Class<? extends Payload>[] payload() default 
  {
  };
}
