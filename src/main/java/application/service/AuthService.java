package application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthService implements ApplicationRunner {

    private final MainService mainService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        mainService.authentification();
    }
}
