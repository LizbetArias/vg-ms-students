package pe.edu.vallegrande.vgmsstudents.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.ApiResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ClassroomService {

     private final WebClient classroomWebClient;

     public ClassroomService(@Qualifier("classroomWebClient") WebClient classroomWebClient) {
          this.classroomWebClient = classroomWebClient;
     }

     /**
      * Obtiene la información de un aula por su ID
      * 
      * @param classroomId ID del aula
      * @return Mono con la información del aula
      */
     public Mono<ClassroomDto> getClassroomById(String classroomId) {
          log.info("Obteniendo información del aula con ID: {}", classroomId);

          return classroomWebClient
                    .get()
                    .uri("/{id}", classroomId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .map(response -> {
                         // Extraer los datos de la respuesta
                         var data = response.getData();
                         if (data instanceof java.util.Map) {
                              @SuppressWarnings("unchecked")
                              java.util.Map<String, Object> classroomData = (java.util.Map<String, Object>) data;

                              ClassroomDto classroomDto = new ClassroomDto();
                              classroomDto.setClassroomId((String) classroomData.get("classroomId"));
                              classroomDto.setClassroomName((String) classroomData.get("classroomName"));
                              classroomDto.setLevelName((String) classroomData.get("classroomAge"));
                              classroomDto.setGrade((String) classroomData.get("gradeLevel"));
                              classroomDto.setSection((String) classroomData.get("section"));

                              return classroomDto;
                         }
                         return new ClassroomDto();
                    })
                    .doOnSuccess(classroom -> log.info("Aula obtenida exitosamente: {}", classroom.getClassroomName()))
                    .doOnError(error -> log.error("Error al obtener el aula con ID {}: {}", classroomId,
                              error.getMessage()))
                    .onErrorResume(error -> {
                         log.warn("No se pudo obtener el aula con ID: {}, retornando aula vacía", classroomId);
                         return Mono.just(new ClassroomDto());
                    });
     }
}
