package pe.edu.vallegrande.vgmsstudents.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.UserDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.ApiResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

     private final WebClient userWebClient;

     public UserService(@Qualifier("userWebClient") WebClient userWebClient) {
          this.userWebClient = userWebClient;
     }

     /**
      * Crea un nuevo usuario (tutor/guardian) en el microservicio de users
      * 
      * @param userDto Datos del usuario a crear
      * @return Mono con el usuario creado incluyendo su ID
      */
     public Mono<UserDto> createUser(UserDto userDto) {
          log.info("Creando usuario en MS Users: {} {}", userDto.getFirstName(), userDto.getLastName());

          return userWebClient
                    .post()
                    .uri("")
                    .bodyValue(userDto)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .map(response -> {
                         // Extraer los datos de la respuesta
                         var data = response.getData();
                         if (data instanceof java.util.Map) {
                              @SuppressWarnings("unchecked")
                              java.util.Map<String, Object> userData = (java.util.Map<String, Object>) data;

                              return UserDto.builder()
                                        .userId((String) userData.get("userId"))
                                        .institutionId((String) userData.get("institutionId"))
                                        .firstName((String) userData.get("firstName"))
                                        .lastName((String) userData.get("lastName"))
                                        .documentType((String) userData.get("documentType"))
                                        .documentNumber((String) userData.get("documentNumber"))
                                        .phone((String) userData.get("phone"))
                                        .address((String) userData.get("address"))
                                        .email((String) userData.get("email"))
                                        .userName((String) userData.get("userName"))
                                        .role((String) userData.get("role"))
                                        .status((String) userData.get("status"))
                                        .build();
                         }
                         return new UserDto();
                    })
                    .doOnSuccess(user -> log.info("Usuario creado exitosamente con ID: {}", user.getUserId()))
                    .doOnError(error -> log.error("Error al crear usuario: {}", error.getMessage()));
     }

     /**
      * Obtiene un usuario por su ID
      * 
      * @param userId ID del usuario
      * @return Mono con los datos del usuario
      */
     public Mono<UserDto> getUserById(String userId) {
          log.info("Obteniendo usuario por ID: {}", userId);

          return userWebClient
                    .get()
                    .uri("/{id}", userId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .map(response -> {
                         var data = response.getData();
                         if (data instanceof java.util.Map) {
                              @SuppressWarnings("unchecked")
                              java.util.Map<String, Object> userData = (java.util.Map<String, Object>) data;

                              return UserDto.builder()
                                        .userId((String) userData.get("userId"))
                                        .institutionId((String) userData.get("institutionId"))
                                        .firstName((String) userData.get("firstName"))
                                        .lastName((String) userData.get("lastName"))
                                        .documentType((String) userData.get("documentType"))
                                        .documentNumber((String) userData.get("documentNumber"))
                                        .phone((String) userData.get("phone"))
                                        .address((String) userData.get("address"))
                                        .email((String) userData.get("email"))
                                        .userName((String) userData.get("userName"))
                                        .role((String) userData.get("role"))
                                        .status((String) userData.get("status"))
                                        .build();
                         }
                         return new UserDto();
                    })
                    .doOnSuccess(user -> log.info("Usuario obtenido exitosamente: {}", user.getUserId()))
                    .doOnError(error -> log.error("Error al obtener usuario: {}", error.getMessage()))
                    .onErrorResume(error -> {
                         log.warn("No se pudo obtener el usuario con ID: {}, retornando usuario vacío", userId);
                         return Mono.just(new UserDto());
                    });
     }
}
