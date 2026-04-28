package com.guts.Guts_IAM.security.userdetails;

import com.guts.Guts_IAM.user.model.User;
import com.guts.Guts_IAM.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userMail)
            throws UsernameNotFoundException {

        User user = userRepository.findByUserMailAndActiveTrue(userMail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User Not Found with email: " + userMail
                        )
                );

        List<SimpleGrantedAuthority> authorities =
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toList();

        return new CustomUserDetails(
                user.getUserMail(),
                user.getUserPassword(),
                user.isActive(),
                authorities
        );
    }
}