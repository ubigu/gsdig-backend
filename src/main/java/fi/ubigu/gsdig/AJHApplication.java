package fi.ubigu.gsdig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AJHApplication {

    public static void main(String[] args) {
        SpringApplication.run(AJHApplication.class, args);
    }
    
}
