package com.pickeat.backend.user.application;

import com.pickeat.backend.global.auth.principal.ProviderPrincipal;
import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.login.application.dto.request.SignupRequest;
import com.pickeat.backend.room.domain.repository.RoomUserRepository;
import com.pickeat.backend.user.application.dto.UserResponse;
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
    private final RoomUserRepository roomUserRepository;

    public boolean isUserExist(Long providerId, String provider) {
        return userRepository.existsByProviderIdAndProvider(providerId, provider);
    }

    @Transactional
    public UserResponse createUser(SignupRequest request, ProviderPrincipal providerPrincipal) {
        validateDuplicateNickname(request.nickname());
        User user = new User(request.nickname(), providerPrincipal.providerId(), providerPrincipal.provider());
        saveUser(user);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        userRepository.delete(user);
    }

    public UserResponse findByNickName(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    public UserResponse getById(Long userId) {
        User user = getUser(userId);
        return UserResponse.from(user);
    }

    public List<UserResponse> searchByNickname(String nickname) {
        List<User> users = userRepository.findByNicknameStartsWith(nickname);

        // 정확히 일치하는 닉네임을 맨 앞에 정렬
        users.sort(Comparator.comparing(user -> !user.getNickname().equals(nickname)));

        return UserResponse.from(users);
    }

    public List<UserResponse> getByRoomId(Long roomId) {
        List<Long> userIds = roomUserRepository.getAllUserIdsByRoomId(roomId);
        List<User> users = userRepository.findAllByIdIn(userIds);

        return UserResponse.from(users);
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.ALREADY_NICKNAME_EXISTS);
        }
    }

    private void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ALREADY_NICKNAME_EXISTS);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
