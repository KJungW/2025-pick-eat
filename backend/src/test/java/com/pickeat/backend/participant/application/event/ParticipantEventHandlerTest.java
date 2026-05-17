package com.pickeat.backend.participant.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.configuration.sse.SseChannelTopic;
import com.pickeat.backend.participant.application.dto.event.ParticipantUpdateEventRequest;
import com.pickeat.backend.participant.domain.Participant;
import com.pickeat.backend.participant.domain.storage.ParticipantStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import({ParticipantEventHandler.class, ParticipantStorage.class})
class ParticipantEventHandlerTest extends DatabaseSliceTest {

    @Autowired
    private ParticipantEventHandler participantEventHandler;

    @Autowired
    private ParticipantStorage participantStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Nested
    class 참가자_업데이트_이벤트_발행 {

        @Test
        void 참가자_업데이트_이벤트를_발행한다() throws InterruptedException {
            // given
            String pickeatCode = "event-content-test";
            Participant participant = new Participant("테스터");
            participantStorage.setupAboutParticipant(pickeatCode, participant);

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
            String topicName = SseChannelTopic.PARTICIPANT_EVENT_TOPIC.getValue();

            redisTemplate.getConnectionFactory().getConnection()
                    .subscribe((message, pattern) -> {
                        messageQueue.add(new String(message.getBody()));
                    }, topicName.getBytes());

            ParticipantUpdateEventRequest request = new ParticipantUpdateEventRequest(pickeatCode);

            // when
            participantEventHandler.handleParticipantUpdate(request);

            // then
            // Redis의 이벤트 발행은 비동기이므로 최대 5초 대기하며 메시지 추출
            String publishedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(publishedMessage).contains("\"group\":\"PARTICIPANT\""),
                    () -> assertThat(publishedMessage).contains("\"groupSequence\":1"),
                    () -> assertThat(publishedMessage).contains("\"action\":\"PARTICIPANT_UPDATE_EVENT\""),
                    () -> assertThat(publishedMessage).contains("\"pickeatCode\":\"" + pickeatCode + "\""),
                    () -> assertThat(publishedMessage).contains("\"completionState\""),
                    () -> assertThat(publishedMessage).contains("\"" + participant.getCode() + "\":false")
            );
        }
    }
}
