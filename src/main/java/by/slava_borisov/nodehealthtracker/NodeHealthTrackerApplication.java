package by.slava_borisov.nodehealthtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NodeHealthTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeHealthTrackerApplication.class, args);
    }
}