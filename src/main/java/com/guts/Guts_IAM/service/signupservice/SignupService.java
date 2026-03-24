package com.guts.Guts_IAM.service.signupservice;


import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.exceptionhandling.exceptions.ConflictException;
import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.Role;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.rolerepo.RoleRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.SignUpDto;
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

    public SignUpDto signup(SignUpDto signUpDto, HttpServletRequest httpServletRequest) {
        Optional<User> userExists= userRepository.findByUserMailAndActiveTrue(signUpDto.getUserMail());

        if(userExists.isPresent()){
            throw new ConflictException("User Already Exists",
                    "USER_EXISTS",
                    HttpStatus.CONFLICT);
        }

        User user=new User();

        user.setUserName(signUpDto.getUserName());
        user.setUserMail(signUpDto.getUserMail());
        user.setUserPassword(bCryptPasswordEncoder.encode(signUpDto.getUserPassword()));
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
        auditLog.setResourceId(user.getUserId().toString());
        auditLog.setResource("AUTH");
        auditLog.setIpAddress(httpServletRequest.getRemoteAddr());
        auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));

        auditRepository.save(auditLog);

        SignUpDto signedUpUserDto=new SignUpDto(signedUpUser);

        return signedUpUserDto;
    }
}
