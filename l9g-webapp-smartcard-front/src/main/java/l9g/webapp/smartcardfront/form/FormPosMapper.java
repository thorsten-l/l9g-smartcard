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
import l9g.webapp.smartcardfront.db.model.PosCategory;
import l9g.webapp.smartcardfront.db.model.PosPointOfSale;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import l9g.webapp.smartcardfront.db.model.PosProperty;
import l9g.webapp.smartcardfront.db.model.PosTenant;
import l9g.webapp.smartcardfront.db.model.PosUser;
import l9g.webapp.smartcardfront.db.model.PosVariation;
import l9g.webapp.smartcardfront.form.model.FormAddress;
import l9g.webapp.smartcardfront.form.model.FormCategory;
import l9g.webapp.smartcardfront.form.model.FormPointOfSale;
import l9g.webapp.smartcardfront.form.model.FormProduct;
import l9g.webapp.smartcardfront.form.model.FormProperty;
import l9g.webapp.smartcardfront.form.model.FormTenant;
import l9g.webapp.smartcardfront.form.model.FormUser;
import l9g.webapp.smartcardfront.form.model.FormVariation;
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

  @Mapping(target = "tenantId", expression = "java(posUser.getTenant() != null ? posUser.getTenant().getId() : null)")
  FormUser posUserToFormUser(PosUser posUser);

  FormAddress posAddressToFormAddress(PosAddress posAddress);

  @Mapping(target = "tenantId", expression = "java(posCategory.getTenant() != null ? posCategory.getTenant().getId() : null)")
  FormCategory posCategoryToFormCategory(PosCategory posCategory);

  @Mapping(target = "categoryId", expression = "java(posProduct.getCategory() != null ? posProduct.getCategory().getId() : null)")
  @Mapping(target = "variations", expression = "java(posProduct.getVariations() != null ? posProduct.getVariations().size() : 0)")
  FormProduct posProductToFormProduct(PosProduct posProduct);

  @Mapping(target = "productId", expression = "java(posVariation.getProduct() != null ? posVariation.getProduct().getId() : null)")
  FormVariation posVariationToFormVariation(PosVariation posVariation);

  @Mapping(target = "tenantId", expression = "java(posPointOfSale.getTenant() != null ? posPointOfSale.getTenant().getId() : null)")
  @Mapping(target = "addressId", expression = "java(posPointOfSale.getAddress() != null ? posPointOfSale.getAddress().getId() : null)")
  FormPointOfSale posPointOfSaleToFormPointOfSale(PosPointOfSale posPointOfSale);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTimestamp", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updatePosAddressFromFormAddress(FormAddress source, @MappingTarget PosAddress target);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTimestamp", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "variations", ignore = true)
  void updatePosProductFromFormProduct(FormProduct source, @MappingTarget PosProduct target);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTimestamp", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "product", ignore = true)
  void updatePosVariationFromFormVariation(FormVariation source, @MappingTarget PosVariation target);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTimestamp", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "tenant", ignore = true)
  @Mapping(target = "address", ignore = true)
  @Mapping(target = "sumupReaderName", ignore = true)
  void updatePostPointOfSaleFromFormPointOfSale(FormPointOfSale source, @MappingTarget PosPointOfSale target);

}
