package org.senai.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.senai.exception.exceptions.RegisterNotFoundException;
import org.senai.model.User;
import org.senai.dtos.UserUpdateResponseDTO;
import org.senai.repositories.UserRepository;
import org.senai.security.JWTTokenProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    JWTTokenProvider jwtTokenProvider;

    public List<User> getAll() {
        return userRepository.listAll();
    }

    public User getById(Long id) {
        User user = userRepository.findById(id);

        if (user == null) {
            throw new RegisterNotFoundException("Usuário não encontrado");
        }

        return user;
    }

    public User getUserByEmail(String email) {
        User user = userRepository.find("email", email).firstResult();

        if (user == null) {
            throw new RegisterNotFoundException("Usuário não encontrado");
        }

        return user;
    }

    @Transactional
    public User register(User user) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());

        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    @Transactional
    public UserUpdateResponseDTO update(Long id, User updatedUser) {
        User user = getById(id);

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().equals(user.getPassword())) {
            String hashedPassword = BCrypt.withDefaults()
                    .hashToString(12, updatedUser.getPassword().toCharArray());
            user.setPassword(hashedPassword);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        userRepository.save(user);
        return new UserUpdateResponseDTO(user, accessToken, refreshToken);
    }

    @Transactional
    public void delete(Long id) {
        User user = getById(id);
        userRepository.deleteById(id);
    }
}