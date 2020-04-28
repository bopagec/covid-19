package com.blackpawsys.api.covid19;

import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableScheduling
@EnableSwagger2
public class Covid19ApiApplication {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    SpringApplication.run(Covid19ApiApplication.class, args);
  }

  @Bean
  public Docket swaggerConfiguration(){
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .paths(PathSelectors.ant("/covid-19/report/**"))
        .apis(RequestHandlerSelectors.basePackage("com.blackpawsys"))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo(){
    return new ApiInfo(
        "COVID-19  T  R  A  C  K  E  R - API",
        "COVID-19 Tracker 2020",
        "1.0",
        "Free to use",
        new Contact("Pohodhika Bopage", "http://www.blackpawsys.co.uk", "blackpawsys@gmail.com"),
        "API Licence",
        "http://www.blackpawsys.co.uk",
        Collections.emptyList()
        );
  }

}
