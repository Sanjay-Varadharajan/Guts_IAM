package com.guts.Guts_IAM.security.userdetails;

import com.guts.Guts_IAM.model.user.User;
import com.guts.Guts_IAM.repo.userrepo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {


    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userMail) throws UsernameNotFoundException{
        User user=userRepository.findByUserMailAndActiveTrue(userMail).orElseThrow(()->
                new UsernameNotFoundException("User Not Found with this email"+userMail));


        if(user!=null){
            return new CustomUserDetails(
                    user,
                    user.getUserMail(),
                    user.getUserPassword(),
                    user.isActive(),
                    user.getRoles()
                            .stream()
                            .map(role->new SimpleGrantedAuthority(role.toString()))
                            .toList()
                    );
        }
        throw new UsernameNotFoundException("User not Found with email: "+userMail);
    }
}
