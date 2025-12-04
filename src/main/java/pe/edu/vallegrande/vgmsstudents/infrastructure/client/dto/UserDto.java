package pe.edu.vallegrande.vgmsstudents.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
     private String userId;
     private String institutionId;
     private String firstName;
     private String lastName;
     private String documentType;
     private String documentNumber;
     private String phone;
     private String address;
     private String email;
     private String userName;
     private String role;
     private String status;
}
