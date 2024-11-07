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

import l9g.webapp.smartcardfront.db.model.PosAddress;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.form.model.FormAddress;
import l9g.webapp.smartcardfront.form.model.FormProperty;
import l9g.webapp.smartcardfront.form.model.FormTenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Mapper
public interface FormPosMapper
{
  public FormPosMapper INSTANCE = Mappers.getMapper(FormPosMapper.class);

  @Mapping(target = "addressId", expression = "java(posTenant.getAddress() != null ? posTenant.getAddress().getId() : null)")
  FormTenant posTenantToFormTenant(PosTenant posTenant);

  @Mapping(target = "tenantId", expression = "java(posProperty.getTenant() != null ? posProperty.getTenant().getId() : null)")
  FormProperty posPropertyToFormProperty(PosProperty posProperty);

  FormAddress posAddressToFormAddress(PosAddress posAddress);
  
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTimestamp", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updatePosAddressFromFormAddress(FormAddress source, @MappingTarget PosAddress target);
   
}
