package com.guts.Guts_IAM.sessionmanagement.service;


import com.guts.Guts_IAM.security.userdetails.CustomUserDetails;
import com.guts.Guts_IAM.sessionmanagement.dto.SessionDto;
import com.guts.Guts_IAM.sessionmanagement.model.UserSession;
import com.guts.Guts_IAM.sessionmanagement.repository.UserSessionRepository;
import com.guts.Guts_IAM.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;

    public void createSession(User user,
                              String jwt,
                              HttpServletRequest request) {

        String userAgentString =
                request.getHeader("User-Agent");

        UserAgentAnalyzer analyzer =
                UserAgentAnalyzer
                        .newBuilder()
                        .hideMatcherLoadStats()
                        .withCache(1000)
                        .build();

        UserAgent agent =
                analyzer.parse(userAgentString);

        String browser =
                agent.getValue("AgentName");

        String operatingSystem =
                agent.getValue("OperatingSystemName");

        UserSession session = new UserSession();

        session.setUser(user);
        session.setJwtToken(jwt);

        session.setBrowser(browser);
        session.setDevice(operatingSystem);

        session.setIpAddress(request.getRemoteAddr());

        session.setLoginTime(LocalDateTime.now());

        session.setLastActive(LocalDateTime.now());

        session.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        session.setRevoked(false);

        sessionRepository.save(session);
    }

    public List<SessionDto> getSessions(Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails)
                        authentication.getPrincipal();

        Integer userId =
                userDetails.getUserId();

        List<UserSession> sessionList= sessionRepository
                .findByUserUserIdAndRevokedFalse(userId);

        return sessionList.stream()
                .map(SessionDto::new)
                .toList();
    }

    }
