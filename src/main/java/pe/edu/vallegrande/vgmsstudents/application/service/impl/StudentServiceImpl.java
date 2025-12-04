package pe.edu.vallegrande.vgmsstudents.application.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsstudents.application.service.StudentService;
import pe.edu.vallegrande.vgmsstudents.domain.enums.StudentStatus;
import pe.edu.vallegrande.vgmsstudents.domain.model.Student;
import pe.edu.vallegrande.vgmsstudents.domain.model.vo.Guardian;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.ClassroomService;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.InstitutionService;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.UserService;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.InstitutionDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto.UserDto;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.request.CreateStudentRequest;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.request.UpdateStudentRequest;
import pe.edu.vallegrande.vgmsstudents.infrastructure.dto.response.StudentWithInstitutionResponse;
import pe.edu.vallegrande.vgmsstudents.infrastructure.repository.StudentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private UserService userService;

    @Override
    public Flux<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Mono<Student> createStudent(CreateStudentRequest request) {
        // 1. Crear los usuarios (tutores/guardianes) en el MS de Users
        List<Mono<UserDto>> userCreationMonos = new ArrayList<>();

        for (Guardian guardian : request.getGuardians()) {
            String userRole;
            switch (guardian.getRelationship().toUpperCase()) {
                case "PADRE":
                    userRole = "PADRE";
                    break;
                case "MADRE":
                    userRole = "MADRE";
                    break;
                case "TUTOR":
                    userRole = "TUTOR";
                    break;
                case "OTRO":
                    userRole = "TUTOR"; // OTRO se mapea a TUTOR
                    break;
                default:
                    userRole = "TUTOR"; // Por defecto TUTOR
                    break;
            }

            // Construir el UserDto para cada tutor
            UserDto userDto = UserDto.builder()
                    .institutionId(request.getInstitutionId())
                    .firstName(guardian.getNames())
                    .lastName(guardian.getLastNames())
                    .documentType(guardian.getDocumentType())
                    .documentNumber(guardian.getDocumentNumber())
                    .phone(guardian.getPhone())
                    .address(request.getAddress() != null ? request.getAddress() : "")
                    .email(guardian.getDocumentNumber() + "@guardian.local")
                    .userName(guardian.getDocumentNumber())
                    .role(userRole) // PADRE, MADRE o TUTOR (valores del enum UserRole)
                    .status("ACTIVE") // ACTIVE (valor del enum UserStatus)
                    .build();

            // Agregar la llamada al servicio de users
            userCreationMonos.add(userService.createUser(userDto));
        }

        // 2. Ejecutar todas las creaciones de usuarios en paralelo
        return Flux.concat(userCreationMonos)
                .collectList()
                .flatMap(createdUsers -> {
                    // 3. Actualizar los guardians con los userIds generados
                    List<Guardian> updatedGuardians = new ArrayList<>();
                    for (int i = 0; i < request.getGuardians().size(); i++) {
                        Guardian guardian = request.getGuardians().get(i);
                        UserDto createdUser = createdUsers.get(i);

                        // Asignar el userId generado al guardian
                        guardian.setUserId(createdUser.getUserId());
                        updatedGuardians.add(guardian);
                    }

                    // 4. Crear el estudiante con los guardians actualizados
                    Student student = Student.builder()
                            .cui(request.getCui())
                            .personalInfo(request.getPersonalInfo())
                            .dateOfBirth(request.getDateOfBirth())
                            .address(request.getAddress())
                            .photoPerfil(request.getPhotoPerfil())
                            .status(StudentStatus.ACTIVE.name())
                            .institutionId(request.getInstitutionId())
                            .classroomId(request.getClassroomId())
                            .developmentInfo(request.getDevelopmentInfo())
                            .guardians(updatedGuardians) // Guardians con userIds
                            .healthInfo(request.getHealthInfo())
                            .build();

                    // 5. Guardar el estudiante en MongoDB
                    return studentRepository.save(student);
                })
                .onErrorResume(error -> {
                    // Manejar errores en la creación de usuarios o estudiante
                    return Mono.error(
                            new RuntimeException("Error al crear estudiante y sus tutores: " + error.getMessage()));
                });
    }

    @Override
    public Mono<Student> updateStudent(String id, UpdateStudentRequest request) {
        return studentRepository.findById(id)
                .flatMap(existingStudent -> {
                    // Actualizar solo los campos que no son null
                    if (request.getPersonalInfo() != null) {
                        existingStudent.setPersonalInfo(request.getPersonalInfo());
                    }
                    if (request.getDateOfBirth() != null) {
                        existingStudent.setDateOfBirth(request.getDateOfBirth());
                    }
                    if (request.getAddress() != null) {
                        existingStudent.setAddress(request.getAddress());
                    }
                    if (request.getPhotoPerfil() != null) {
                        existingStudent.setPhotoPerfil(request.getPhotoPerfil());
                    }
                    if (request.getClassroomId() != null) {
                        existingStudent.setClassroomId(request.getClassroomId());
                    }
                    if (request.getDevelopmentInfo() != null) {
                        existingStudent.setDevelopmentInfo(request.getDevelopmentInfo());
                    }
                    if (request.getGuardians() != null) {
                        existingStudent.setGuardians(request.getGuardians());
                    }
                    if (request.getHealthInfo() != null) {
                        existingStudent.setHealthInfo(request.getHealthInfo());
                    }
                    if (request.getStatus() != 0) {
                        existingStudent.setStatus(String.valueOf(request.getStatus()));
                    }

                    return studentRepository.save(existingStudent);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con ID: " + id)));
    }

    @Override
    public Mono<Student> findById(String id) {
        return studentRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con ID: " + id)));
    }

    @Override
    public Mono<StudentWithInstitutionResponse> findByIdWithInstitution(String id) {
        return studentRepository.findById(id)
                .flatMap(student -> {
                    // Obtener información de la institución, aula y guardianes en paralelo
                    Mono<InstitutionDto> institutionMono = institutionService
                            .getInstitutionById(student.getInstitutionId());

                    Mono<ClassroomDto> classroomMono = classroomService
                            .getClassroomById(student.getClassroomId());

                    // Obtener información completa de cada guardian desde MS Users
                    List<Mono<UserDto>> guardianMonos = new ArrayList<>();
                    for (Guardian guardian : student.getGuardians()) {
                        if (guardian.getUserId() != null && !guardian.getUserId().isEmpty()) {
                            guardianMonos.add(userService.getUserById(guardian.getUserId()));
                        }
                    }

                    // Combinar todas las llamadas
                    Mono<List<UserDto>> guardiansListMono = guardianMonos.isEmpty()
                            ? Mono.just(new ArrayList<>())
                            : Flux.concat(guardianMonos).collectList();

                    // Combinar los resultados de institución, aula y guardianes
                    return Mono.zip(institutionMono, classroomMono, guardiansListMono)
                            .map(tuple -> StudentWithInstitutionResponse.builder()
                                    .student(student)
                                    .institution(tuple.getT1())
                                    .classroom(tuple.getT2())
                                    .guardians(tuple.getT3())
                                    .build());
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con ID: " + id)));
    }

    @Override
    public Mono<Student> findByCui(String cui) {
        return studentRepository.findByCui(cui)
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con CUI: " + cui)));
    }

    @Override
    public Flux<Student> findByClassroom(String classroomId) {
        return studentRepository.findByClassroomId(classroomId);
    }

    @Override
    public Flux<Student> findByInstitution(String institutionId) {
        return studentRepository.findByInstitutionId(institutionId);
    }

    @Override
    public Mono<Student> restoreStudent(String id) {
        return studentRepository.findById(id)
                .flatMap(existingStudent -> {
                    existingStudent.setStatus(StudentStatus.ACTIVE.name());
                    return studentRepository.save(existingStudent);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con ID: " + id)));
    }

    @Override
    public Mono<Student> deleteStudent(String id) {
        return studentRepository.findById(id)
                .flatMap(existingStudent -> {
                    existingStudent.setStatus(StudentStatus.INACTIVE.name());
                    return studentRepository.save(existingStudent);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado con ID: " + id)));
    }
}
