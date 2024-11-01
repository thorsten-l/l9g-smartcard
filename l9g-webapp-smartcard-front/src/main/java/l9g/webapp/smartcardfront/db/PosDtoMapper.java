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
package l9g.webapp.smartcardfront.db;

import java.util.List;
import l9g.webapp.smartcardfront.admin.DtoTenantEntry;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Mapper
public interface PosDtoMapper
{
  public PosDtoMapper INSTANCE = Mappers.getMapper(PosDtoMapper.class);

  @Mapping(source = "name", target = "text")
  @Mapping(target = "selected", constant = "false")
  public DtoTenantEntry posTenantToDtoTenantEntry(PosTenant posTenant);

  public List<DtoTenantEntry> posTenantsListToDtoTenantEntryList(
    List<PosTenant> posTenants);
}
