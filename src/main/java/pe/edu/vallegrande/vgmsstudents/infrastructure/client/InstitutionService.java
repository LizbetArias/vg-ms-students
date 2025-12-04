package pe.edu.vallegrande.vgmsstudents.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.InstitutionDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.ApiResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class InstitutionService {

     private final WebClient institutionWebClient;

     public InstitutionService(@Qualifier("institutionWebClient") WebClient institutionWebClient) {
          this.institutionWebClient = institutionWebClient;
     }

     /**
      * Obtiene la información de una institución por su ID
      * 
      * @param institutionId ID de la institución
      * @return Mono con la información de la institución
      */
     public Mono<InstitutionDto> getInstitutionById(String institutionId) {
          log.info("Obteniendo información de la institución con ID: {}", institutionId);

          return institutionWebClient
                    .get()
                    .uri("/{id}", institutionId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .map(response -> {
                         // Extraer los datos de la respuesta
                         var data = response.getData();
                         if (data instanceof java.util.Map) {
                              @SuppressWarnings("unchecked")
                              java.util.Map<String, Object> institutionData = (java.util.Map<String, Object>) data;

                              InstitutionDto institutionDto = new InstitutionDto();
                              institutionDto.setInstitutionId((String) institutionData.get("institutionId"));

                              // Extraer el nombre de la institución desde institutionInformation
                              @SuppressWarnings("unchecked")
                              java.util.Map<String, Object> institutionInfo = (java.util.Map<String, Object>) institutionData
                                        .get("institutionInformation");

                              if (institutionInfo != null) {
                                   institutionDto.setInstitutionName((String) institutionInfo.get("institutionName"));
                              }

                              return institutionDto;
                         }
                         return new InstitutionDto();
                    })
                    .doOnSuccess(institution -> log.info("Institución obtenida exitosamente: {}",
                              institution.getInstitutionName()))
                    .doOnError(error -> log.error("Error al obtener la institución con ID {}: {}", institutionId,
                              error.getMessage()))
                    .onErrorResume(error -> {
                         log.warn("No se pudo obtener la institución con ID: {}, retornando institución vacía",
                                   institutionId);
                         return Mono.just(new InstitutionDto());
                    });
     }
}
