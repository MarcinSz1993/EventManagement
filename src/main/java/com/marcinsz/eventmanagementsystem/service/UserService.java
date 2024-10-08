package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.AuthenticationResponse;
import com.marcinsz.eventmanagementsystem.model.CreateUserResponse;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest createUserRequest){
        if(createUserRequest == null){
            throw new NullPointerException("CreateUserRequest cannot be null!");
        }
        User newUser = UserMapper.convertCreateUserRequestToUser(createUserRequest);
        newUser.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        userRepository.save(newUser);
        UserDto userDto = UserMapper.convertUserToUserDto(newUser);
        String token = jwtService.generateToken(newUser);

        return new CreateUserResponse(userDto,token);
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
       try {
           authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(
                           authenticationRequest.getUsername(),
                           authenticationRequest.getPassword()
                   )
           );
       } catch (AuthenticationException exception){
           throw new BadCredentialsException();
       }
        User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> new UserNotFoundException(authenticationRequest.getUsername()));
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }
}
