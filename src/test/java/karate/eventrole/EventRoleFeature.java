package karate.eventrole;

import com.intuit.karate.junit5.Karate;
import fr.pmevent.PMEvent;
import fr.pmevent.configuration.TestSecurityConfig;
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
public class EventRoleFeature {

    @LocalServerPort
    private int port;

    @MockitoBean
    private MailService mailService;

    @Karate.Test
    Karate testEventRole() {
        return Karate.run("eventrole")
                .relativeTo(getClass())
                .systemProperty("server.port", String.valueOf(port));
    }

}
