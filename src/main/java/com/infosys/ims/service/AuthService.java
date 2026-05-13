package com.infosys.ims.service;

import com.infosys.ims.dtos.request.AuthRequest;
import com.infosys.ims.dtos.response.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
}