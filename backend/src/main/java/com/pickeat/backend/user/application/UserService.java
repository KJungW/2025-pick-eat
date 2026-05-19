package com.pickeat.backend.user.application;

import com.pickeat.backend.global.argument.principal.OAuthProviderPrincipal;
import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.login.application.dto.request.SignupRequest;
import com.pickeat.backend.user.application.dto.response.UserResponse;
import com.pickeat.backend.user.domain.User;
import com.pickeat.backend.user.domain.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public boolean isUserExist(Long providerId, String provider) {
        return userRepository.existsByProviderIdAndProvider(providerId, provider);
    }

    @Transactional
    public UserResponse createUser(SignupRequest request, OAuthProviderPrincipal OAuthProviderPrincipal) {
        validateDuplicateNickname(request.nickname());

        User user = new User(
                request.nickname(),
                OAuthProviderPrincipal.providerId(),
                OAuthProviderPrincipal.provider()
        );
        saveUser(user);

        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getById(userId);
        userRepository.delete(user);
    }

    public UserResponse findById(Long userId) {
        User user = getById(userId);
        return UserResponse.from(user);
    }

    public UserResponse findByNickName(String nickname) {
        User user = getByNickname(nickname);
        return UserResponse.from(user);
    }

    public List<UserResponse> searchByNickname(String startWith) {
        List<User> users = userRepository.findByNicknameStartsWith(startWith);

        // 정확히 일치하는 닉네임을 맨 앞에 정렬
        users.sort(Comparator.comparing(user -> !user.getNickname().equals(startWith)));
        return UserResponse.from(users);
    }

    public List<UserResponse> getByRoomId(Long roomId) {
        List<User> users = userRepository.findAllByRoomId(roomId);
        return UserResponse.from(users);
    }

    private User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ClientException(ClientErrorCode.USER_NOT_FOUND));
    }

    private User getByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new ClientException(ClientErrorCode.USER_NOT_FOUND));
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new ClientException(ClientErrorCode.ALREADY_NICKNAME_EXISTS);
        }
    }

    private void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ClientException(ClientErrorCode.ALREADY_NICKNAME_EXISTS);
        }
    }
}
