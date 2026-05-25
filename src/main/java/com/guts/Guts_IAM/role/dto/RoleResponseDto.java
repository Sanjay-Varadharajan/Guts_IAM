package com.guts.Guts_IAM.role.dto;


import com.guts.Guts_IAM.role.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponseDto {

    private Integer roleId;

    private String roleName;

    public RoleResponseDto(Role role) {
        this.roleId=role.getRoleId();
        this.roleName=role.getName();
    }
}
