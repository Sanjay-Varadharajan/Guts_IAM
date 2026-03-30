package com.guts.Guts_IAM.service.userservice;

import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import com.guts.Guts_IAM.security.signup.UserRequestDto;
import com.guts.Guts_IAM.security.signup.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    public UserResponseDto viewProfile(Principal principal) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException("Login and Try Again")
        );

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        return userResponseDto;
    }


    public UserResponseDto updateProfile(UserRequestDto userRequestDto, Principal principal) {

        User userExisting=userRepository.findByUserMailAndActiveTrue(principal.getName()).orElseThrow(
                ()->new UsernameNotFoundException(principal.getName()+" not found ,Login and try")
        );

        if(userRequestDto.getUserName()!=null){
            userExisting.setUserName(userRequestDto.getUserName());
        }

        userRepository.save(userExisting);

        UserResponseDto userResponseDto=new UserResponseDto(userExisting);

        return userResponseDto;
    }
}
