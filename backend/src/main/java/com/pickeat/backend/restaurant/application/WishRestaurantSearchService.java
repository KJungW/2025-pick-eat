package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.wish.domain.Wish;
import com.pickeat.backend.wish.domain.repository.WishRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishRestaurantSearchService {

    private final WishRepository wishRepository;

    public List<RestaurantInfoDto> searchByWish(WishRestaurantRequest request) {
        List<Wish> wishes = wishRepository.findAllByRoomId(request.roomId());
        validateWishesNotEmpty(wishes);
        return RestaurantInfoDto.fromWishes(wishes);
    }

    private void validateWishesNotEmpty(List<Wish> wishes) {
        if (wishes.isEmpty()) {
            throw new ClientException(ClientErrorCode.ROOM_HAS_NO_WISHES);
        }
    }
}
