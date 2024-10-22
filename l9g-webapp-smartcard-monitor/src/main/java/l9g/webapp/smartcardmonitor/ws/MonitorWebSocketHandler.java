package l9g.webapp.smartcardmonitor.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import l9g.smartcard.dto.DtoEvent;

/**
 * WebSocket handler to send HeartbeatDTO every 5 seconds.
 */
@Slf4j
public class MonitorWebSocketHandler implements WebSocketHandler
{
  private final Map<String, WebSocketSession> sessions = new HashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void afterConnectionEstablished(WebSocketSession session)
    throws Exception
  {
    log.debug("afterConnectionEstablished: session id = {}", session.getId());
    this.sessions.put(session.getId(), session);
  }

  @Override
  public void handleMessage(WebSocketSession session,
    org.springframework.web.socket.WebSocketMessage<?> message)
    throws Exception
  {
    log.debug("handleMessage ({}) message.payload={}",
      session.getId(), message.getPayload().toString());
  }

  @Override
  public void handleTransportError(WebSocketSession session,
    Throwable exception)
    throws Exception
  {
    log.error("handleTransportError: session id = {}, error: {}",
      session.getId(), exception.getMessage());
    session.close();
    sessions.remove(session.getId());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session,
    org.springframework.web.socket.CloseStatus closeStatus)
    throws Exception
  {
    log.debug("afterConnectionClosed {} status {}/{}",
      session.getId(), closeStatus.getCode(), closeStatus.getReason());
    sessions.remove(session.getId());
  }

  @Override
  public boolean supportsPartialMessages()
  {
    return false;
  }

  public void fireEvent(DtoEvent event)
    throws IOException
  {
    log.trace("fireEvent to {} sessions", sessions.size());

    sessions.forEach((id, session) ->
    {
      if(session == null ||  ! session.isOpen())
      {
        sessions.remove(id);
      }
    });

    for(WebSocketSession session : sessions.values())
    {
      if(session != null && session.isOpen())
      {
        String json = objectMapper.writeValueAsString(event);
        session.sendMessage(new TextMessage(json));
        log.debug("Sent text message: {}", json);
      }
    }
  }

}
