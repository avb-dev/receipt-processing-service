package application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthService {

    private final MainService mainService;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
            mainService.authentification();
    }
}
