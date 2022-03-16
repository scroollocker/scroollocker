package kg.wedevs.advert_bot;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class AdvertBotApplication {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(AdvertBotApplication.class, args);
    }

}
