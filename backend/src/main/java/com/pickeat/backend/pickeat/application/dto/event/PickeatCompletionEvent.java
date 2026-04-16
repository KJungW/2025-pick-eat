package com.pickeat.backend.pickeat.application.dto.event;

import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class PickeatCompletionEvent {

    private final PickeatEventType type;
    private final String pickeatCode;
    private final String restaurantCode;
    private final String name;
    private final String category;
    private final String roadAddressName;
    private final String placeUrl;
    private final List<String> tags;
    private final String pictureUrl;

    public PickeatCompletionEvent(String pickeatCode, PickeatResultResponse pickeatResultResponse) {
        this.type = PickeatEventType.PICKEAT_COMPLETION_EVENT;
        this.pickeatCode = pickeatCode;
        this.restaurantCode = pickeatResultResponse.code();
        this.name = pickeatResultResponse.name();
        this.category = pickeatResultResponse.category();
        this.roadAddressName = pickeatResultResponse.roadAddressName();
        this.placeUrl = pickeatResultResponse.placeUrl();
        this.tags = pickeatResultResponse.tags();
        this.pictureUrl = pickeatResultResponse.pictureUrl();
    }
}
