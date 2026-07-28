package com.guts.Guts_IAM.user.controller.user;


import com.guts.Guts_IAM.common.response.ApiResponse;
import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.user.dto.user.UserRequestDto;
import com.guts.Guts_IAM.user.dto.user.UserResponseDto;
import com.guts.Guts_IAM.user.export.ProfileExportService;
import com.guts.Guts_IAM.user.service.user.UserService;
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

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ProfileExportService profileExportService;


    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> viewProfile(Authentication authentication, HttpServletRequest request){

        UserResponseDto profile=userService.viewProfile(authentication,request);

        ApiResponse<UserResponseDto> response=new ApiResponse<>(
                true,
                authentication.getName()+" PROFILE_FETCHED",
                profile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(@RequestBody UserRequestDto userRequestDto, Authentication authentication,HttpServletRequest request){

        UserResponseDto updatedProfile=userService.updateProfile(userRequestDto,authentication,request);

        ApiResponse<UserResponseDto> apiResponse=new ApiResponse<>(
                true,
                "PROFILE_UPDATED",
                updatedProfile,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/me/logs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<AuditLogDtoForUser>>> viewLogs(Authentication authentication,
                                                                          @PageableDefault(
                                                                                  page = 0,
                                                                          size= 10,
                                                                          sort ="auditedOn",
                                                                          direction = Sort.Direction.DESC)
                                                                          Pageable pageable
    ,HttpServletRequest request){

        Page<AuditLogDtoForUser> auditResponse=userService.viewLogs(authentication,pageable,request);

        ApiResponse<Page<AuditLogDtoForUser>> response=new ApiResponse<>(
                true,
                "USER_AUDIT_LOG",
                auditResponse,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/my-logs/download")
    public ResponseEntity<InputStreamResource> downloadMyLogs(
            Authentication authentication,
            HttpServletRequest httpServletRequest)
            throws IOException {

        InputStreamResource resource =
                userService.downloadMyLogs(
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

    @GetMapping("/me/profile/download")
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

    @PatchMapping("me/status")
    public ResponseEntity<ApiResponse<UserResponseDto>> toggleUserStatus(Authentication authentication,HttpServletRequest httpServletRequest){
        UserResponseDto responseDto=userService.toggleUserStatus(authentication,httpServletRequest);

        ApiResponse<UserResponseDto> apiResponse=new ApiResponse<>(
                true,
                "STATUS UPDATED",
                responseDto,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/apiKey/revoke")
    public ResponseEntity<ApiResponse<Void>>  revokeApiKey(@RequestParam String apiKey,Authentication authentication,HttpServletRequest httpServletRequest){
        userService.revokeApiKey(apiKey,authentication,httpServletRequest);

        ApiResponse<Void> apiResponse=new ApiResponse<>(
                true,
                "API_KEY_REVOKED",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(apiResponse);
    }
}