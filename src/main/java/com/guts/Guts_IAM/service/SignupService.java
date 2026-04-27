package com.guts.Guts_IAM.service;


import com.guts.Guts_IAM.auth.dto.SignupRequest;
import com.guts.Guts_IAM.role.enums.Roles;
import com.guts.Guts_IAM.common.exception.types.ConflictException;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.role.repository.RoleRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AuditRepository auditRepository;

    private final RoleRepository roleRepository;



    public SignupRequest signup(SignupRequest signUpRequest, HttpServletRequest httpServletRequest) {
        Optional<User> userExists= userRepository.findByUserMailAndActiveTrue(signUpRequest.getUserMail());

        if(userExists.isPresent()){
            throw new ConflictException("User Already Exists",
                    "USER_EXISTS",
                    HttpStatus.CONFLICT);
        }

        User user=new User();

        user.setUserName(signUpRequest.getUserName());
        user.setUserMail(signUpRequest.getUserMail());
        user.setUserPassword(bCryptPasswordEncoder.encode(signUpRequest.getUserPassword()));
        Role userRole = roleRepository.findByName(Roles.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));


        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user.setRoles(roles);
        User signedUpUser= userRepository.save(user);

        AuditLog auditLog=new AuditLog();
        Set<Roles> rolesSet=new HashSet<>();
        rolesSet.add(Roles.ROLE_USER);
        auditLog.setLogAction("SIGN_UP");
        auditLog.setRoleName(rolesSet.toString());
        auditLog.setUserMail(user.getUserMail());
        if (signedUpUser.getUserId() == null) {
            throw new IllegalStateException("User ID not generated after save");
        }
        auditLog.setResourceId(signedUpUser.getUserId().toString());
        auditLog.setResource("AUTH");
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        auditRepository.save(auditLog);

        SignupRequest signedUpUserDto=new SignupRequest(signedUpUser);

        return signedUpUserDto;
    }
}
