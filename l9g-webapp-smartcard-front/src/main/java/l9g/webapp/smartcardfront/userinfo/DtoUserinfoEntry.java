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
package l9g.webapp.smartcardfront.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
@Getter
@RequiredArgsConstructor
public class DtoUserinfoEntry implements Comparable<DtoUserinfoEntry>
{
  @Override
  public int compareTo(DtoUserinfoEntry o)
  {
    return this.text.compareTo(o.text);
  }
  
  private final String id;

  private final String text;

  private final boolean selected;

}