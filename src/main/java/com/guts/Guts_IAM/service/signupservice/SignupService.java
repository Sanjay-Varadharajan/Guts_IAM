package com.guts.Guts_IAM.service.signupservice;


import com.guts.Guts_IAM.enums.Roles;
import com.guts.Guts_IAM.exceptionhandling.exceptions.ConflictException;
import com.guts.Guts_IAM.model.audits.AuditLog;
import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.auditrepo.AuditRepository;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.SignUpDto;
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

    public SignUpDto signup(SignUpDto signUpDto) {
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
        Set<Roles> roles=new HashSet<>();
        roles.add(Roles.ROLE_USER);
        user.setUserRoles(roles);

        User signedUpUser= userRepository.save(user);

        AuditLog auditLog=new AuditLog();
        auditLog.setLogAction(user.getUserName()+" signed Up");
        Set<Roles> rolesSet=new HashSet<>();
        roles.add(Roles.ROLE_USER);
        auditLog.setUserRoles(rolesSet);
        auditLog.setUserMail(user.getUserMail());

        auditRepository.save(auditLog);

        SignUpDto signedUpUserDto=new SignUpDto(signedUpUser);

        return signedUpUserDto;
    }
}
