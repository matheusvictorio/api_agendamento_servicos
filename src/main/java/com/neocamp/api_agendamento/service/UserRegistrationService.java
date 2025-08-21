package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.config.security.CustomUserDetailsService;
import com.neocamp.api_agendamento.config.security.JwtUtil;
import com.neocamp.api_agendamento.domain.dto.request.LoginRequestDTO;
import com.neocamp.api_agendamento.domain.dto.request.RegisterRequestDTO;
import com.neocamp.api_agendamento.domain.dto.response.LoginResponseDTO;
import com.neocamp.api_agendamento.domain.dto.response.LogoutResponseDTO;
import com.neocamp.api_agendamento.domain.dto.response.RegisterResponseDTO;
import com.neocamp.api_agendamento.domain.entities.Client;
import com.neocamp.api_agendamento.domain.entities.Provider;
import com.neocamp.api_agendamento.domain.entities.User;
import com.neocamp.api_agendamento.repository.ClientRepository;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {

    private final ClientRepository clientRepository;

    private final ProviderRepository providerRepository;

    private final PasswordEncoder passwordEncoder;

    private final ViaCepService viaCepService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    public UserRegistrationService(
        ClientRepository clientRepository,
        ProviderRepository providerRepository,
        PasswordEncoder passwordEncoder,
        ViaCepService viaCepService,
        AuthenticationManager authenticationManager,
        JwtUtil jwtUtil,
        CustomUserDetailsService userDetailsService
    ) {
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
        this.passwordEncoder = passwordEncoder;
        this.viaCepService = viaCepService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    //refatorar para separar as validacoes
    public RegisterResponseDTO registerUser(RegisterRequestDTO registerRequest) {

        if (clientRepository.findByEmail(registerRequest.email()).isPresent() ||
            providerRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new RuntimeException("Email já está em uso!");
        }

        if ("PROVIDER".equals(registerRequest.userType()) && registerRequest.specialty() == null) {
            throw new RuntimeException("Especialidade é obrigatória para prestadores de serviço!");
        }

        var address = viaCepService.getAddressByCep(
            registerRequest.cep(), 
            registerRequest.number(), 
            registerRequest.complement()
        );

        String encodedPassword = passwordEncoder.encode(registerRequest.password());

        if ("CLIENT".equals(registerRequest.userType())) {
            Client client = new Client(
                registerRequest.name(),
                registerRequest.email(),
                encodedPassword,
                registerRequest.phone(),
                address
            );
            
            Client savedClient = clientRepository.save(client);
            
            return new RegisterResponseDTO(
                savedClient.getId(),
                savedClient.getName(),
                savedClient.getEmail(),
                "CLIENT",
                "Cliente registrado com sucesso!"
            );
            
        } else if ("PROVIDER".equals(registerRequest.userType())) {
            Provider provider = new Provider(
                registerRequest.name(),
                registerRequest.email(),
                encodedPassword,
                registerRequest.phone(),
                address,
                registerRequest.specialty()
            );
            
            Provider savedProvider = providerRepository.save(provider);
            
            return new RegisterResponseDTO(
                savedProvider.getId(),
                savedProvider.getName(),
                savedProvider.getEmail(),
                "PROVIDER",
                "Prestador de serviço registrado com sucesso!"
            );
        }

        throw new RuntimeException("Tipo de usuário inválido!");
    }

    public LoginResponseDTO loginUser(LoginRequestDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());
            User user = userDetailsService.findUserByEmail(loginRequest.email());
            String userType = userDetailsService.getUserType(loginRequest.email());

            String token = jwtUtil.generateToken(userDetails, user.getId(), userType);

            return new LoginResponseDTO(
                    token,
                    userType,
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );
        } catch (Exception e) {
            throw new RuntimeException("Credenciais inválidas");
        }
    }

    public LogoutResponseDTO logoutUser() {
        // O token deve ser removido no front
        return new LogoutResponseDTO("Logout realizado com sucesso!");
    }
}
