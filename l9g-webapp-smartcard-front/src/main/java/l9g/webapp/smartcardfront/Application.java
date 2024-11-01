package l9g.webapp.smartcardfront;

import l9g.webapp.smartcardfront.customer.AttributesMapConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AttributesMapConfig.class)
public class Application
{
  public static void main(String[] args)
  {
    SpringApplication.run(Application.class, args);
  }
}
