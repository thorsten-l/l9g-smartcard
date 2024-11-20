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
package l9g.webapp.smartcardfront.form;

import java.text.NumberFormat;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StringToDoubleConverter implements Converter<String, Double>
{

  @Override
  public Double convert(String source)
  {
    try
    {
      Locale locale = LocaleContextHolder.getLocale();
      log.debug("locale = {}", locale.toLanguageTag());
      String cleaned = source.replaceAll("[^\\d,.-]", "").trim();
      log.debug("'{}' -> '{}'", source, cleaned);
      NumberFormat format = NumberFormat.getInstance(locale);
      Number number = format.parse(cleaned);
      return number.doubleValue();
    }
    catch(Exception e)
    {
      throw new IllegalArgumentException("Invalid number format: " + source, e);
    }
  }

}
