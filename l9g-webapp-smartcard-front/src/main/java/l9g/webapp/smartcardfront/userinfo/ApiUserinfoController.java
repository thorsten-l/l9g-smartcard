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
package l9g.webapp.smartcardfront.userinfo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code39Writer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import l9g.webapp.smartcardfront.client.ApiClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestController
@RequestMapping(path = "/api/v1/userinfo",
                produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ApiUserinfoController
{
  private final ApiClientService apiClientService;

  private final Cache<Long, Map<String, String>> accountCache;

  public ApiUserinfoController(ApiClientService apiClientService)
  {
    this.apiClientService = apiClientService;
    this.accountCache = Caffeine.newBuilder()
      .expireAfterWrite(accountCacheExpireAfterWrite, TimeUnit.MINUTES)
      .build();
  }

  @GetMapping("/term")
  public DtoUserinfoResult findByTerm(
    @RequestParam(required = false, defaultValue = "1") int page,
    @RequestParam(required = false, defaultValue = "") String term,
    @RequestParam(name = "_type", required = false, defaultValue = "") String type
  )
  {
    log.debug("findByTerm page={} term='{}' type='{}'", page, term, type);

    ArrayList<DtoUserinfoEntry> entries = new ArrayList<>();

    if( ! term.isBlank() && term.length() > 4)
    {
      entries.add(new DtoUserinfoEntry("123", "Tanja Test", false));
    }

    DtoUserinfoPagination pagination =
      new DtoUserinfoPagination(entries.size() == 10);
    return new DtoUserinfoResult(entries, pagination);
  }

  @GetMapping("/serial/{serial}")
  public Map<String, String> findBySerial(@PathVariable long serial)
  {
    log.debug("serial = {}", serial);

    Map<String, String> result = accountCache.get(serial, apiClientService :: findBySerial);

    if(barcodeEnabled && result.containsKey(barcodeAttributeName))
    {
      String barcodeNumber = result.get(barcodeAttributeName);
      result.put("barcodeEnabled", Boolean.toString(barcodeEnabled));
      result.put("barcodeNumber", barcodeNumber);
      result.put("barcodePNG", generateBarcodeBase64(barcodeNumber));
    }

    if(customerNumberEnabled && result.containsKey(customerNumberAttributeName))
    {
      String customerNumber = result.get(customerNumberAttributeName);
      result.put("customerNumberEnabled", Boolean.toString(customerNumberEnabled));
      result.put("customerNumber", customerNumber);
    }

    result.put("userId", result.get(userIdAttributeName));
    return result;
  }

  private String generateBarcodeBase64(String barcodeText)
  {
    log.debug("barcode {} {}/{}", barcodeImageType, barcodeWidth, barcodeHeight);
    String base64Encoded = null;

    Code39Writer barcodeWriter = new Code39Writer();
    BitMatrix bitMatrix = barcodeWriter.encode(
      barcodeText, BarcodeFormat.CODE_39, barcodeWidth, barcodeHeight);
    BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try
    {
      ImageIO.write(barcodeImage, barcodeImageType, outputStream);
      byte[] imageData = outputStream.toByteArray();
      base64Encoded = Base64.getEncoder().encodeToString(imageData);
    }
    catch(IOException ex)
    {
      log.error("image writer: {}", ex);
    }
    return base64Encoded;
  }

  @Value("${app.account-cache.expire-after-write}")
  private int accountCacheExpireAfterWrite;

  @Value("${app.barcode.attribute-name}")
  private String barcodeAttributeName;

  @Value("${app.barcode.image-type}")
  private String barcodeImageType;

  @Value("${app.barcode.enabled}")
  private boolean barcodeEnabled;

  @Value("${app.barcode.width}")
  private int barcodeWidth;

  @Value("${app.barcode.height}")
  private int barcodeHeight;

  @Value("${app.customer-number.enabled}")
  private boolean customerNumberEnabled;

  @Value("${app.customer-number.attribute-name}")
  private String customerNumberAttributeName;

  @Value("${app.user-id.attribute-name}")
  private String userIdAttributeName;

}
