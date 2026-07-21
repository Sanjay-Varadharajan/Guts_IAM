package com.guts.Guts_IAM.user.controller.admin;


import com.guts.Guts_IAM.apikey.dto.CreationStat;
import com.guts.Guts_IAM.auditlog.dto.SecurityStats;
import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.passwordtracking.dto.TrackDto;
import com.guts.Guts_IAM.role.dto.RoleRequestDto;
import com.guts.Guts_IAM.role.dto.RoleResponseDto;
import com.guts.Guts_IAM.user.dto.admin.AdminRequestDto;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.export.ProfileExportService;
import com.guts.Guts_IAM.user.service.admin.AdminService;
import com.guts.Guts_IAM.user.stats.UserStatsForAdmin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@RestController
public class AdminController {

    private final AdminService adminService;

    private final ProfileExportService profileExportService;

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllActiveUsers(Authentication authentication, @PageableDefault(
            page = 0,
            size = 10,
            sort = "userCreatedOn",
            direction = Sort.Direction.DESC)
                                        Pageable pageable, HttpServletRequest request
    )
    {

        Page<UserResponseDto> response=adminService.getAllActiveUsers(authentication,pageable,request);

        ApiResponse<Page<UserResponseDto>> apiResponse=new ApiResponse<>(
                true,
                "Active Users",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PatchMapping("/user/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(@PathVariable Integer userId, Authentication authentication, HttpServletRequest request){
        UserResponseDto response=adminService.updateUserStatus(userId,authentication,request);

        ApiResponse<UserResponseDto> apiResponse=new ApiResponse<>(
                true,
                "",
                response,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/logs/viewall")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAllAuditLog(Authentication authentication,
                                                                         @PageableDefault(
                                                                              page = 0,
                                                                              size = 10,
                                                                              sort = "auditedOn",
                                                                              direction = Sort.Direction.DESC)
                                                                      Pageable pageable,
                                                                         HttpServletRequest request
                                                                      ){

        Page<AuditLogDto> dtoResponse=adminService.getAllAuditLog(authentication,pageable,request);

        ApiResponse<Page<AuditLogDto>> response=new ApiResponse<>(
                true,
                "AUDIT_LOG_FETCHED",
                dtoResponse,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> viewProfile(Authentication authentication, HttpServletRequest request){

        UserResponseDto profile=adminService.viewProfile(authentication,request);

        ApiResponse<UserResponseDto> response=new ApiResponse<>(
                true,
                " PROFILE_FETCHED",
                profile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/me/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(@RequestBody AdminRequestDto adminRequestDto, Authentication authentication, HttpServletRequest request){

        UserResponseDto updatedProfile=adminService.updateProfile(adminRequestDto,authentication,request);

        ApiResponse<UserResponseDto> apiResponse=new ApiResponse<>(
                true,
                "PROFILE_UPDATED",
                updatedProfile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/role/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> addRoles(@RequestBody RoleRequestDto dto, HttpServletRequest httpServletRequest, Authentication authentication){

        RoleResponseDto postResponse=adminService.addRoles(dto,httpServletRequest,authentication);


        ApiResponse<RoleResponseDto> apiResponse=new ApiResponse<>(
                true,
                dto.getRoleName()+"ROLE_ADDED",
                postResponse,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/my-logs/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadMyLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        InputStreamResource resource =
                adminService.downloadMyLogs(
                        authentication,
                        httpServletRequest
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=my_audit_logs.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(resource);
    }

    @GetMapping("/all-logs/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadAllLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        InputStreamResource resource =
                adminService.downloadAllLogs(
                        authentication,
                        httpServletRequest
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=all_audit_logs.xlsx"
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(resource);
    }

    @GetMapping("/me/profile/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadProfile(
            Authentication authentication
            ,HttpServletRequest httpServletRequest
    ) throws Exception {

        String json =
                profileExportService.downloadProfile(authentication,httpServletRequest);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=profile.json"
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        json.getBytes(StandardCharsets.UTF_8)
                );
    }

    @GetMapping("stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserStatsForAdmin>> getStats(Authentication authentication,HttpServletRequest httpServletRequest){
        UserStatsForAdmin stats=adminService.getStats(authentication,httpServletRequest);

        ApiResponse<UserStatsForAdmin> response=new ApiResponse<>(
                true,
                "STATS_LOADED",
                stats,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs/passwordTracking/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TrackDto>>> allPasswordChange(Authentication authentication
                                                                                , HttpServletRequest httpServletRequest,
                                                                         @PageableDefault(
                                                                                        page = 0,
                                                                                        size = 10,
                                                                                        sort = "passwordChangedAt",
                                                                                        direction = Sort.Direction.DESC
                                                                                )Pageable pageable
                                                                                ){

        Page<TrackDto> passwordTrackers=adminService.allPasswordChange(authentication,httpServletRequest,pageable);
        ApiResponse<Page<TrackDto>> apiResponse=
                new ApiResponse<>(
                        true,
                        "FETCHED_PASSWORD_TRACKING_LOGS",
                        passwordTrackers,
                        LocalDateTime.now()
                );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/logs/passwordTracking/{userId}/view")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<TrackDto>>> viewPasswordTrackById(@PathVariable long userId,
                                                                                    Authentication authentication
            , HttpServletRequest httpServletRequest,
                                                                                    @PageableDefault(
                                                                                            page = 0,
                                                                                            size = 10,
                                                                                            sort = "passwordChangedAt",
                                                                                            direction = Sort.Direction.DESC
                                                                                    )Pageable pageable
                                                                                    ){

        Page<TrackDto> passwordTrackers=adminService.viewPasswordTrackById(userId,authentication,httpServletRequest,pageable);

        ApiResponse<Page<TrackDto>> apiResponse=
                new ApiResponse<>(
                        true,
                        "FETCHED_PASSWORD_TRACKING_LOGS",
                        passwordTrackers,
                        LocalDateTime.now()
                );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/get/key/createOn")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreationStat>> keyCreatedOn(@RequestParam String apiKey,Authentication authentication,
                                                                  HttpServletRequest httpServletRequest) {

        CreationStat creationStat = adminService.getCreatedOn(apiKey, authentication, httpServletRequest);

        ApiResponse<CreationStat> apiResponse = new ApiResponse<>(
                true,
                "FETCHED_KEY_CREATION_DATE",
                creationStat,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/security/stats/view")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SecurityStats>> getSecurityStats(Authentication authentication,HttpServletRequest httpServletRequest,
    @RequestParam String userMail){

        SecurityStats securityStats=adminService.getSecurityStats(authentication,httpServletRequest,userMail);

        ApiResponse<SecurityStats> securityStatsApiResponse=new ApiResponse<>(
                true,
                "FETCHED_SECURITY_STATS",
                securityStats,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(securityStatsApiResponse);
    }
}
