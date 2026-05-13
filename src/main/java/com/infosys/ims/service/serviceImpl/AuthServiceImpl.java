package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.AuthRequest;
import com.infosys.ims.dtos.response.AuthResponse;
import com.infosys.ims.entity.Users;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.repository.UserRepository;
import com.infosys.ims.service.AuthService;
import com.infosys.ims.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getUserCode(),
                user.getRole().name()
        );
    }
}