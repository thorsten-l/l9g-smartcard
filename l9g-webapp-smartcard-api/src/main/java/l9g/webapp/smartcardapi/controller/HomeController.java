/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
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
package l9g.webapp.smartcardapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This class represents the HomeController which is responsible for handling
 * requests related to the home page.
 * It redirects the user to the Swagger UI documentation page.
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController
{
  /**
   * Redirects to the API documentation.
   * This method handles HTTP GET requests to the root URL ("/") and redirects
   * the user to the API documentation page.
   *
   * @return a String representing the redirection URL to the API documentation.
   */
  @GetMapping("/")
  public String redirectToApiDocs()
  {
    return "redirect:/swagger-ui/index.html";
  }
}
