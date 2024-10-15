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
package l9g.webapp.smartcardmonitor.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Configuration
@EnableWebSocket
@Slf4j
public class MonitorWebSocketConfig implements WebSocketConfigurer
{
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
  {
    log.debug("registerWebSocketHandlers");

    registry
      .addHandler(webSocketHandler(), "/scmon")
      .setAllowedOrigins("*");
  }

  @Bean
  MonitorWebSocketHandler webSocketHandler()
  {
    log.debug("webSocketHandler");
    return new MonitorWebSocketHandler();
  }

}
