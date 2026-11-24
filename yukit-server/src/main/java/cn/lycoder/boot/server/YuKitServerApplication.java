package cn.lycoder.boot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.lycoder.boot")
public class YuKitServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuKitServerApplication.class, args);
    }
}
