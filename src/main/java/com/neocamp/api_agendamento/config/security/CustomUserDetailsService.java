package com.neocamp.api_agendamento.config.security;

import com.neocamp.api_agendamento.domain.entities.Client;
import com.neocamp.api_agendamento.domain.entities.Provider;
import com.neocamp.api_agendamento.domain.entities.User;
import com.neocamp.api_agendamento.repository.ClientRepository;
import com.neocamp.api_agendamento.repository.ProviderRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    private final ProviderRepository providerRepository;

    public CustomUserDetailsService(ClientRepository clientRepository, ProviderRepository providerRepository) {
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return client.get();
        }

        Optional<Provider> provider = providerRepository.findByEmail(email);
        if (provider.isPresent()) {
            return provider.get();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public User findUserByEmail(String email) {
        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return client.get();
        }

        Optional<Provider> provider = providerRepository.findByEmail(email);
        if (provider.isPresent()) {
            return provider.get();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public String getUserType(String email) {
        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return "CLIENT";
        }

        Optional<Provider> provider = providerRepository.findByEmail(email);
        if (provider.isPresent()) {
            return "PROVIDER";
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
