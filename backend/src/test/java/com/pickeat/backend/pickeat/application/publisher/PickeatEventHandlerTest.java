package com.pickeat.backend.pickeat.application.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.pickeat.application.dto.event.PickeatCompletionEventRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import({PickeatEventHandler.class})
class PickeatEventHandlerTest extends DatabaseSliceTest {

    @Autowired
    private PickeatEventHandler pickeatEventHandler;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Nested
    class 픽잇_완료_이벤트_발행 {

        @Test
        void 픽잇_완료_이벤트를_발행한다() throws InterruptedException {
            // given
            String pickeatCode = "completion-test-code";
            PickeatResultResponse resultResponse = new PickeatResultResponse(
                    "res-123",
                    "매화양꼬치",
                    "중식",
                    "경기 성남시 분당구",
                    "http://place.map.kakao.com/1",
                    List.of("양꼬치", "맛집"),
                    "http://image.com/1"
            );

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
            String topicName = SseChannelTopic.PICKEAT_EVENT_TOPIC.getValue();
            redisTemplate.getConnectionFactory().getConnection()
                    .subscribe((message, pattern) -> {
                        messageQueue.add(new String(message.getBody()));
                    }, topicName.getBytes());

            PickeatCompletionEventRequest request = new PickeatCompletionEventRequest(pickeatCode, resultResponse);

            // when
            pickeatEventHandler.handlePickeatCompletionEvent(request);

            // then
            // Redis의 이벤트 발행은 비동기이므로 최대 5초 대기하며 메시지 추출
            String publishedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(publishedMessage).isNotNull(),
                    () -> assertThat(publishedMessage).contains("\"type\":\"PICKEAT_COMPLETION_EVENT\""),
                    () -> assertThat(publishedMessage).contains("\"pickeatCode\":\"" + pickeatCode + "\""),
                    () -> assertThat(publishedMessage).contains("\"restaurantCode\":\"res-123\""),
                    () -> assertThat(publishedMessage).contains("\"name\":\"매화양꼬치\""),
                    () -> assertThat(publishedMessage).contains("\"category\":\"중식\""),
                    () -> assertThat(publishedMessage).contains("\"tags\":[\"양꼬치\",\"맛집\"]")
            );
        }
    }
}
