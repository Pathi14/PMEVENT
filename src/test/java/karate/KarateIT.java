package karate;

import com.intuit.karate.junit5.Karate;
import fr.pmevent.PMEvent;
import fr.pmevent.config.TestSecurityConfig;
import fr.pmevent.service.MailService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                PMEvent.class,
                TestSecurityConfig.class
        },
        properties = "spring.profiles.active=test"
)
public class KarateIT {
    @LocalServerPort
    private int port;

    @MockitoBean
    private MailService mailService;

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate")
                .relativeTo(getClass())
                .systemProperty("server.port", String.valueOf(port));
    }

}
