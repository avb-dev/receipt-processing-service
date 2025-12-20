package application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.loolzaaa.nalog.mytax.client.MyTaxClient;
import ru.loolzaaa.nalog.mytax.client.MyTaxClientConfig;

@Configuration
public class MyTaxClientBean {

    @Bean
    public MyTaxClient myTaxClient(MyTaxClientProperties props) {
        MyTaxClientConfig config = new MyTaxClientConfig();
        config.setPrefix(props.getPrefix());
        config.setZoneOffset(props.getZone());
        config.setApiPath(props.getApiPath());
        config.setRefererHeader(props.getReferer());

        return new MyTaxClient(config);
    }
}
