package pe.edu.vallegrande.vgmsstudents.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsstudents.domain.model.Student;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.InstitutionDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.UserDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentWithInstitutionResponse {

     // Información del estudiante
     private Student student;

     // Información de la institución
     private InstitutionDto institution;

     // Información del aula
     private ClassroomDto classroom;

     // Información completa de los tutores/guardianes
     private List<UserDto> guardians;
}
