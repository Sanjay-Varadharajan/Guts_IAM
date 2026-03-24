        package com.guts.Guts_IAM.exceptionhandling.globalhandler;


        import com.guts.Guts_IAM.exceptionhandling.apierrorresponse.ApiErrorResponse;
        import com.guts.Guts_IAM.exceptionhandling.exceptions.*;
        import jakarta.servlet.http.HttpServletRequest;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.MethodArgumentNotValidException;
        import org.springframework.web.bind.annotation.ExceptionHandler;
        import org.springframework.web.bind.annotation.RestControllerAdvice;

        import java.time.LocalDateTime;
        import java.util.HashMap;
        import java.util.Map;

        @RestControllerAdvice
        public class GlobalExceptionHandler {


            @ExceptionHandler(MethodArgumentNotValidException.class)
            public ResponseEntity<ApiErrorResponse> handleInvalidErrors(MethodArgumentNotValidException exception,
                                                                        HttpServletRequest request) {

                Map<String, String> fieldError = new HashMap<>();

                exception.getBindingResult()
                        .getFieldErrors()
                        .forEach(error ->
                                fieldError.put(error.getField(), error.getDefaultMessage()));

                ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                        "Input Validation Failed",
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_FAILED",
                        fieldError,
                        LocalDateTime.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiErrorResponse);
            }

            @ExceptionHandler(ApiException.class)
            public ResponseEntity<ApiErrorResponse> handleApiExceptions(ApiException apiException,
                                                                        HttpServletRequest httpServletRequest) {

                Map<String, String> exceptionBody = new HashMap<>();

                exceptionBody.put("Exception", "Api_Exception");

                ApiErrorResponse apiExceptionBody = new ApiErrorResponse(
                        "Something went wrong internally, please try again later",
                        apiException.getStatus(),
                        "INTERNAL_SERVER_ERROR",
                        exceptionBody,
                        LocalDateTime.now(),
                        httpServletRequest.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiExceptionBody);
            }

            @ExceptionHandler(ResourceNotFoundException.class)
            public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException resourceNotFoundException
                    , HttpServletRequest httpServletRequest) {


                Map<String, String> exceptionBody = new HashMap<>();

                exceptionBody.put("Exception", "Resource Not Found");

                ApiErrorResponse resourceNotFound = new ApiErrorResponse(
                        resourceNotFoundException.getMessage(),
                        resourceNotFoundException.getStatus(),
                        "NOT_FOUND",
                        exceptionBody,
                        LocalDateTime.now(),
                        httpServletRequest.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resourceNotFound);
            }

            @ExceptionHandler(UnauthorizedException.class)
            public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException ex,
                                                                                HttpServletRequest request) {

                Map<String,String> exceptionBody = new HashMap<>();
                exceptionBody.put("Exception", "Unauthorized Access");

                ApiErrorResponse response = new ApiErrorResponse(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        "ACCESS_UNAUTHORIZED",
                        exceptionBody,
                        LocalDateTime.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            @ExceptionHandler(ConflictException.class)
            public ResponseEntity<ApiErrorResponse> handleConflictException(ConflictException conflictException,
                                                                            HttpServletRequest httpServletRequest) {

                Map<String, String> exceptionBody = new HashMap<>();

                exceptionBody.put("Exception", "Conflict Occurred");

                ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                        "Some Conflict is Occurred",
                        HttpStatus.CONFLICT,
                        conflictException.getErrorCode(),
                        exceptionBody,
                        LocalDateTime.now(),
                        httpServletRequest.getRequestURI()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiErrorResponse);
            }

            @ExceptionHandler(ForbiddenException.class)
            public ResponseEntity<ApiErrorResponse> handleForbiddenException(ForbiddenException forbiddenException,
                                                                             HttpServletRequest httpServletRequest){

                Map<String,String> exceptionBody=new HashMap<>();

                exceptionBody.put("Exception","you are authenticate,but not allowed to perform this action");

                ApiErrorResponse apiErrorResponse=new ApiErrorResponse(
                        "You are not Allowed to perform this action",
                        HttpStatus.FORBIDDEN,
                        forbiddenException.getErrorCode(),
                        exceptionBody,
                        LocalDateTime.now(),
                        httpServletRequest.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiErrorResponse);
            }

            @ExceptionHandler(Exception.class)
            public ResponseEntity<ApiErrorResponse> handleAllOtherExceptions(Exception ex,
                                                                             HttpServletRequest request) {

                Map<String,String> exceptionBody = new HashMap<>();
                exceptionBody.put("Exception", ex.getClass().getSimpleName());

                ApiErrorResponse response = new ApiErrorResponse(
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_SERVER_ERROR",
                        exceptionBody,
                        LocalDateTime.now(),
                        request.getRequestURI()
                );

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
