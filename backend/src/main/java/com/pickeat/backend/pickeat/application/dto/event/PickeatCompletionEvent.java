package com.pickeat.backend.pickeat.application.dto.event;

import com.pickeat.backend.pickeat.domain.PickeatResult;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public class PickeatCompletionEvent {

    private final PickeatEventType type;
    private final String pickeatCode;
    private final String code;
    private final String name;
    private final String category;
    private final String roadAddressName;
    private final String placeUrl;
    private final List<String> tags;
    private final String pictureUrl;

    public PickeatCompletionEvent(String pickeatCode, PickeatResult pickeatResult) {
        this.type = PickeatEventType.PICKEAT_COMPLETION_EVENT;
        this.pickeatCode = pickeatCode;
        this.code = pickeatResult.getCode();
        this.name = pickeatResult.getName();
        this.category = pickeatResult.getFoodCategory().getName();
        this.roadAddressName = pickeatResult.getRoadAddressName();
        this.placeUrl = pickeatResult.getPlaceUrl();
        this.tags = parseTags(pickeatResult.getTags());
        this.pictureUrl = pickeatResult.getPictureUrl();
    }

    private static List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}
