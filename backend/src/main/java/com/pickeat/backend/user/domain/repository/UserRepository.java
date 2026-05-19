package com.pickeat.backend.user.domain.repository;

import com.pickeat.backend.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByProviderIdAndProvider(Long providerId, String provider);

    boolean existsByNickname(String nickname);

    List<User> findAllByIdIn(List<Long> userIds);

    Optional<User> findByNickname(String nickname);

    List<User> findByNicknameStartsWith(String nickname);

    Optional<User> findByProviderIdAndProvider(Long providerId, String provider);

    @Query("SELECT u FROM RoomUser ru JOIN ru.user u WHERE ru.roomId = :roomId")
    List<User> findAllByRoomId(@Param("roomId") Long roomId);
}
