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

  /**
   * Invoked after a new WebSocket connection has been established.
   *
   * @param session the WebSocket session that has been established
   * @throws Exception if an error occurs during the establishment of the connection
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session)
    throws Exception
  {
    log.debug("afterConnectionEstablished: session id = {}", session.getId());
    this.sessions.put(session.getId(), session);
  }

  /**
   * Handles incoming WebSocket messages.
   *
   * @param session the WebSocket session associated with the message
   * @param message the WebSocket message received
   * @throws Exception if an error occurs while handling the message
   */
  @Override
  public void handleMessage(WebSocketSession session,
    org.springframework.web.socket.WebSocketMessage<?> message)
    throws Exception
  {
    log.debug("handleMessage ({}) message.payload={}",
      session.getId(), message.getPayload().toString());
  }

  /**
   * Handles transport errors that occur during WebSocket communication.
   *
   * @param session the WebSocket session where the error occurred
   * @param exception the exception that was thrown
   * @throws Exception if an error occurs while handling the transport error
   */
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

  /**
   * Invoked after a WebSocket connection has been closed.
   *
   * @param session the WebSocket session that was closed
   * @param closeStatus the status object containing the code and reason for the closure
   * @throws Exception if any error occurs during the handling of the closed connection
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session,
    org.springframework.web.socket.CloseStatus closeStatus)
    throws Exception
  {
    log.debug("afterConnectionClosed {} status {}/{}",
      session.getId(), closeStatus.getCode(), closeStatus.getReason());
    sessions.remove(session.getId());
  }

  /**
   * Indicates whether this WebSocket handler supports partial messages.
   * 
   * @return false, indicating that partial messages are not supported.
   */
  @Override
  public boolean supportsPartialMessages()
  {
    return false;
  }

  /**
   * Fires an event to all open WebSocket sessions.
   * 
   * @param event the event to be sent to the WebSocket sessions
   * @throws IOException if an I/O error occurs while sending the message
   */
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
