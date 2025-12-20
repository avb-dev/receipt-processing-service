package application.jobs;

import application.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Auth {

    private final MainService mainService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
            mainService.authentification();
    }
}
