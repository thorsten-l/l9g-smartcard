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
package l9g.webapp.smartcardfront.db;

import java.util.List;
import java.util.Optional;
import l9g.webapp.smartcardfront.db.model.PosProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Thorsten Ludewig <t.ludewig@gmail.com>
 */
@Repository
public interface PosProductsRepository extends
  JpaRepository<PosProduct, String>
{
  Optional<PosProduct> findByCategory_IdAndName(String categoryId, String name);

  List<PosProduct> findAllByOrderByNameAsc();

  @Query("SELECT p FROM PosProduct p WHERE p.category.tenant.id = :tenantId ORDER BY p.category.name, p.name ASC")
  List<PosProduct> findAllByTenantId(@Param("tenantId") String tenantId);
  
}
