package com.guts.Guts_IAM.user.service.admin;


import com.guts.Guts_IAM.auditlog.action.Action;
import com.guts.Guts_IAM.auditlog.action.AuditStatus;
import com.guts.Guts_IAM.auditlog.export.service.DownloadAuditLogService;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.service.AuditLogService;
import com.guts.Guts_IAM.common.exception.types.UserNameNotFoundException;
import com.guts.Guts_IAM.role.dto.RoleRequestDto;
import com.guts.Guts_IAM.role.dto.RoleResponseDto;
import com.guts.Guts_IAM.role.model.Role;
import com.guts.Guts_IAM.role.repository.RoleRepository;
import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import com.guts.Guts_IAM.user.repository.UserRepository;
import com.guts.Guts_IAM.user.dto.admin.AdminRequestDto;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.stats.AdminStats;
import com.guts.Guts_IAM.user.stats.AdminStatsService;
import com.guts.Guts_IAM.user.stats.UserStatsForAdmin;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {


    private final AdminStatsService adminStatsService;

    private final UserRepository userRepository;

    private final AuditRepository auditRepository;

    private final RoleRepository roleRepository;

    private final AuditLogService auditLogService;

    private final DownloadAuditLogService downloadAuditLogService;


    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllActiveUsers(Authentication authentication, Pageable pageable, HttpServletRequest request) {

        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){

            auditLogService.log(
                    null,
                    Action.VIEW_ALL_ACTIVE_USER,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User userExisting=userExisting1.get();

                Set<String> allowedSort=Set.of("userCreatedOn","userMail");

        pageable.getSort().forEach(order ->
        {
            if(!allowedSort.contains(order.getProperty())){
                auditLogService.log(
                        userExisting,
                        Action.VIEW_ALL_AUDIT_LOG,
                        "AUDIT_LOG",
                        userExisting.getUserId().toString(),
                        AuditStatus.FAILED,
                        "Invalid sort field: " + order.getProperty(),
                        request
                );
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort field: "+order.getProperty()
                );
            }
        });
        Page<User> activeUsers=userRepository.findByActiveTrue(pageable);




        auditLogService.log(
                userExisting,
                Action.VIEW_ALL_ACTIVE_USER,
                "USER",
                userExisting.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Viewed active users list",
                request
        );


        return activeUsers.map(UserResponseDto::new);
    }


    public UserResponseDto updateUserStatus(Integer userId, Authentication authentication, HttpServletRequest request) {


        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){

            auditLogService.log(
                    null,
                    Action.UPDATE_USER_STATUS,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }


        User loggedInAdmin=userExisting1.get();


        Optional<User> user=userRepository.findById(userId);

        if(user.isEmpty()){
            auditLogService.log(
                    null,
                    Action.UPDATE_USER_STATUS,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no user found",
                    request
            );

            throw new UserNameNotFoundException(userId+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User user1=user.get();

        if (loggedInAdmin.getUserId().equals(userId)) {
            auditLogService.log(
                    loggedInAdmin,
                    Action.UPDATE_USER_STATUS,
                    "USER",
                    userId.toString(),
                    AuditStatus.FAILED,
                    "Admin attempted to modify own account status",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own status"
            );
        }

        boolean wasActive = user1.isActive();

        user1.setActive(!user1.isActive());
        userRepository.save(user1);

        adminStatsService.updateUserStatusStats(wasActive,!wasActive);


        auditLogService.log(
                loggedInAdmin,
                Action.UPDATE_USER_STATUS,
                "USER",
                userId.toString(),
                AuditStatus.SUCCESS,
                "User " + user1.getUserMail() +
                        " status changed to " +
                        (user1.isActive() ? "ACTIVE" : "INACTIVE"),
                request);
        return new UserResponseDto(user1);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllAuditLog(Authentication authentication, Pageable pageable, HttpServletRequest request) {
        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){

            auditLogService.log(
                    null,
                    Action.VIEW_ALL_AUDIT_LOG,
                    "USER",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User loggedInAdmin=userExisting1.get();


                        Set<String> allowedSort=Set.of("auditedOn");

        pageable.getSort().forEach(order -> {
            if (!allowedSort.contains(order.getProperty())){
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid Sort Field "+order.getProperty()
                );
            }
        });

        Page<AuditLog> auditLogs=auditRepository.findAll(pageable);


        auditLogService.log(
                loggedInAdmin,
                Action.VIEW_ALL_AUDIT_LOG,
                "AUDIT_LOG",
                loggedInAdmin.getUserId().toString(),
                AuditStatus.SUCCESS,
                "logs viewed Successfully",
                request);

        return auditLogs.map(AuditLogDto::new);
    }

    @Transactional(readOnly = true)
    public UserResponseDto viewProfile(Authentication authentication, HttpServletRequest request) {
        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if(userExisting1.isEmpty()){
            auditLogService.log(
                    null,
                    Action.VIEW_PROFILE,
                    "ADMIN",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);

        }

        User loggedInAdmin=userExisting1.get();


        UserResponseDto userResponseDto=new UserResponseDto(loggedInAdmin);

        


        auditLogService.log(
                loggedInAdmin,
                Action.VIEW_PROFILE,
                "ADMIN",
                loggedInAdmin.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Profile Viewed Successfully",
                request
        );



        return userResponseDto;
    }

    public UserResponseDto updateProfile(AdminRequestDto adminRequestDto, Authentication authentication, HttpServletRequest request) {

        Optional<User> userExisting1 = userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting1.isEmpty()) {
            auditLogService.log(
                    null,
                    Action.UPDATE_PROFILE,
                    "ADMIN",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    request
            );

            throw new UserNameNotFoundException(authentication.getName() + " Not found", "NOT_FOUND", HttpStatus.NOT_FOUND);

        }

        User userExisting = userExisting1.get();
        String oldName = userExisting.getUserName();

        if(oldName.equals(adminRequestDto.getAdminName())) {

            auditLogService.log(
                    userExisting,
                    Action.UPDATE_PROFILE,
                    "ADMIN",
                    userExisting.getUserId().toString(),
                    AuditStatus.FAILED,
                    "New username matches existing username",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is unchanged"
            );
        }

        boolean updated = false;

        if (adminRequestDto.getAdminName() != null &&
                !adminRequestDto.getAdminName().isBlank()) {

            userExisting.setUserName(adminRequestDto.getAdminName());
            updated = true;
        }

        if (!updated) {

            auditLogService.log(
                    userExisting,
                    Action.UPDATE_PROFILE,
                    "ADMIN",
                    userExisting.getUserId().toString(),
                    AuditStatus.FAILED,
                    "No valid fields provided for update",
                    request
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No valid fields provided for update"
            );
        }

        userRepository.save(userExisting);

        auditLogService.log(
                userExisting,
                Action.UPDATE_PROFILE,
                "ADMIN",
                userExisting.getUserId().toString(),
                AuditStatus.SUCCESS,
                "UserName updated from "
                        + oldName
                        + " to "
                        + userExisting.getUserName(),
                request
        );

        UserResponseDto userResponseDto = new UserResponseDto(userExisting);

        return userResponseDto;
    }


    public RoleResponseDto addRoles(RoleRequestDto dto, HttpServletRequest httpServletRequest, Authentication authentication) {

        Optional<User> userExisting1=userRepository.findByUserMailAndActiveTrue(authentication.getName());



        if(userExisting1.isEmpty()){
            auditLogService.log(
                    null,
                    Action.ADDED_NEW_ROLE,
                    "ROLE",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "no admin found",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(authentication.getName()+" Not found","NOT_FOUND",HttpStatus.NOT_FOUND);
        }

        User admin=userExisting1.get();

        if(roleRepository.existsByName(dto.getRoleName().trim().toUpperCase())) {

            auditLogService.log(
                    admin,
                    Action.ADDED_NEW_ROLE,
                    "ROLE",
                    dto.getRoleName(),
                    AuditStatus.FAILED,
                    "Role already exists",
                    httpServletRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Role already exists"
            );
        }

        Role role=new Role();
        role.setName(dto.getRoleName());
        roleRepository.save(role);

        RoleResponseDto responseDto=new RoleResponseDto(role);



        auditLogService.log(
                admin,
                Action.ADDED_NEW_ROLE,
                "ROLE",
                role.getRoleId().toString(),
                AuditStatus.SUCCESS,
                "Role " + role.getName() + " created successfully",
                httpServletRequest
        );

        return responseDto;
    }

    public InputStreamResource downloadMyLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        Optional<User> userExisting =
                userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.DOWNLOAD_AUDIT_LOG,
                    "AUDIT_LOG",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "No profile found",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    authentication.getName() + " Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        ByteArrayInputStream file =
                downloadAuditLogService.downloadMyLogs(authentication.getName());

        User user=userExisting.get();

        auditLogService.log(
                user,
                Action.DOWNLOAD_AUDIT_LOG,
                "AUDIT_LOG",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Audit log downloaded successfully",
                httpServletRequest
        );

        return new InputStreamResource(file);
    }

    public InputStreamResource downloadAllLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        Optional<User> userExisting =
                userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting.isEmpty()) {

            auditLogService.log(
                    null,
                    Action.DOWNLOAD_ALL_AUDIT_LOG,
                    "AUDIT_LOG",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "No profile found",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    authentication.getName() + " Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        ByteArrayInputStream file =
                downloadAuditLogService.downloadAllLogs();

        User user=userExisting.get();

        auditLogService.log(
                user,
                Action.DOWNLOAD_ALL_AUDIT_LOG,
                "AUDIT_LOG",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Audit log downloaded successfully",
                httpServletRequest
        );

        return new InputStreamResource(file);
    }

    public UserStatsForAdmin getStats(Authentication authentication, HttpServletRequest httpServletRequest) {
        Optional<User> userExisting =
                userRepository.findByUserMailAndActiveTrue(authentication.getName());

        if (userExisting.isEmpty()) {
            auditLogService.log(
                    null,
                    Action.LOAD_STATS,
                    "STATS",
                    authentication.getName(),
                    AuditStatus.FAILED,
                    "No profile found",
                    httpServletRequest
            );

            throw new UserNameNotFoundException(
                    authentication.getName() + " Not found",
                    "NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        User user=userExisting.get();

        AdminStats stats=adminStatsService.getStatsSnapshot();
        auditLogService.log(
                user,
                Action.LOAD_STATS,
                "STATS",
                user.getUserId().toString(),
                AuditStatus.SUCCESS,
                "Admin fetched system user statistics",
                httpServletRequest
        );

        return new UserStatsForAdmin(stats);
    }
}
