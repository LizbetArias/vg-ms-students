package pe.edu.vallegrande.vgmsstudents.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

     @Value("${microservices.institution.url}")
     private String institutionServiceUrl;

     @Value("${microservices.classroom.url}")
     private String classroomServiceUrl;

     @Value("${microservices.user.url}")
     private String userServiceUrl;

     @Bean(name = "institutionWebClient")
     public WebClient institutionWebClient(WebClient.Builder builder) {
          return builder
                    .baseUrl(institutionServiceUrl)
                    .build();
     }

     @Bean(name = "classroomWebClient")
     public WebClient classroomWebClient(WebClient.Builder builder) {
          return builder
                    .baseUrl(classroomServiceUrl)
                    .build();
     }

     @Bean(name = "userWebClient")
     public WebClient userWebClient(WebClient.Builder builder) {
          return builder
                    .baseUrl(userServiceUrl)
                    .build();
     }
}
