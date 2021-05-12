package com.rest.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static org.hibernate.internal.util.StringHelper.isBlank;

import com.rest.demo.domain.User;
import com.rest.demo.domain.UserPrincipal;
import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent())
            return new UserPrincipal(user.get());
        else
            throw new UsernameNotFoundException("User " + username + " not found!");
    }

    public ServiceResponse<User> getCurrentUser(UserPrincipal userPrincipal) {
        return new ServiceResponse<>("success", "Current user.",
                userPrincipal.getUser());
    }

    @Transactional
    public ServiceResponse<User> createUser(UserPrincipal userPrincipal, User userToCreate) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            if (!isBlank(userToCreate.getName()) && !isBlank(userToCreate.getUsername()) && !isBlank(userToCreate.getPassword())
                    && !isBlank(userToCreate.getAuthorities()) && !isBlank(userToCreate.getUnit())) {

                Optional<User> foundUser = userRepository.findByUsername(userToCreate.getUsername());

                if (!foundUser.isPresent()) {
                    userToCreate.setPassword(passwordEncoder.encode(userToCreate.getPassword()));
                    User savedUser = userRepository.save(userToCreate);

                    return new ServiceResponse<>("success", "User successfully created.", savedUser);
                }
                return new ServiceResponse<>("error", "User already exists!", null);
            }
            return new ServiceResponse<>("error", "User credentials has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<User> editUser(UserPrincipal userPrincipal, User editedUser) {

        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            if (!isBlank(editedUser.getName()) && !isBlank(editedUser.getUsername()) && !isBlank(editedUser.getPassword())
                    && !isBlank(editedUser.getAuthorities()) && !isBlank(editedUser.getUnit())) {

                if (editedUser.getId() != null && editedUser.getId() != 0) {

                    Optional<User> foundUser = userRepository.findById(editedUser.getId());

                    if (foundUser.isPresent()) {

                        if (foundUser.get().getUsername().equals(editedUser.getUsername())) {

                            foundUser.get().setName(editedUser.getName());
                            foundUser.get().setPassword(passwordEncoder.encode(editedUser.getPassword()));
                            foundUser.get().setAuthorities(editedUser.getAuthorities());
                            foundUser.get().setUnit(editedUser.getUnit());

                            User savedUser = userRepository.save(foundUser.get());

                            return new ServiceResponse<>("success", "User credentials successfully changed.", savedUser);
                        }
                    }
                }
                return new ServiceResponse<>("error", "User not found!", null);
            }
            return new ServiceResponse<>("error", "User credentials has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<User> deleteUser(UserPrincipal userPrincipal, int userId) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            if (userId != 0) {

                Optional<User> foundUser = userRepository.findById(userId);

                if (foundUser.isPresent()) {
                    userRepository.delete(foundUser.get());
                    return new ServiceResponse<>("success", "User successfully deleted.", foundUser.get());
                }
            }
            return new ServiceResponse<>("error", "User not found!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<List<User>> getUsersList(UserPrincipal userPrincipal) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            List<User> usersList = userRepository.findAll();
            return new ServiceResponse<>("success", "Users list.", usersList);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

}
