package com.guts.Guts_IAM.common;

import com.guts.Guts_IAM.common.exception.types.ResourceNotFoundException;
import com.guts.Guts_IAM.common.exception.types.UnauthorizedException;
import com.guts.Guts_IAM.security.jwt.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testResourceNotFoundException() throws Exception {

        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("resource not found"))
                .andExpect(jsonPath("$.statusCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.Exception").value("Resource Not Found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void testUnauthorizedException() throws Exception {

        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.Exception").value("Unauthorized Access"))
                .andExpect(jsonPath("$.path").value("/test/unauthorized"));
    }

    @Test
    void testGenericException() throws Exception {

        mockMvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.Exception").exists())
                .andExpect(jsonPath("$.path").value("/test/generic"));
    }
}