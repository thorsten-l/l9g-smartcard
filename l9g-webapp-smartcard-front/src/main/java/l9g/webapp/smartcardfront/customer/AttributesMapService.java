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
package l9g.webapp.smartcardfront.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Service
@Slf4j
@Getter
public class AttributesMapService
{
  private final Map<String, String> employeeTypesMap;

  private final Map<String, String> departmentsTypesMap;

  private final String employeeTypeAttributeName;

  private final String departmentAttributeName;

  public AttributesMapService(AttributesMapConfig attributesMapConfig)
  {
    this.employeeTypesMap = new HashMap<>();
    mappingListToMap(attributesMapConfig.getEmployeeType().getMappingList(),
      employeeTypesMap);
    this.employeeTypeAttributeName =
      attributesMapConfig.getEmployeeType().getLdapAttributeName();

    this.departmentsTypesMap = new HashMap<>();
    mappingListToMap(attributesMapConfig.getDepartment().getMappingList(),
      departmentsTypesMap);
    this.departmentAttributeName =
      attributesMapConfig.getDepartment().getLdapAttributeName();
  }

  private void mappingListToMap(List<String> mappingList, Map<String, String> map)
  {
    mappingList.forEach(entry ->
    {
      String[] keyValue = entry.split(",");
      if(keyValue.length == 2)
      {
        map.put(keyValue[0].trim(), keyValue[1].trim());
      }
    });
  }

}
