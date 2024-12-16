package com.mind.ocr;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
//@PropertySource("file:C:\\bpo_ocr_home\\application.properties") //remove this as per eDocs
@SpringBootApplication
public class OcrApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OcrApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OcrApplication.class);
    }
}
