package com.pickeat.backend.restaurant.application.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.sse.SseChannelTopic;
import com.pickeat.backend.pickeat.domain.store.PickeatStorage;
import com.pickeat.backend.restaurant.application.RestaurantService;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantExcludeEventRequest;
import com.pickeat.backend.restaurant.application.dto.event.RestaurantLikeEventRequest;
import com.pickeat.backend.restaurant.domain.Restaurants;
import com.pickeat.backend.restaurant.domain.storage.RestaurantsStorage;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.RestaurantFixture;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@Import({RestaurantEventHandler.class, RestaurantService.class, RestaurantsStorage.class, PickeatStorage.class})
class RestaurantEventHandlerTest extends DatabaseSliceTest {

    @Autowired
    private RestaurantEventHandler restaurantEventHandler;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantsStorage restaurantsStorage;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Nested
    class 식당_소거_업데이트_이벤트_발행 {

        @Test
        void 참가자_업데이트_이벤트를_발행한다() throws InterruptedException {
            // given
            String pickeatCode = "exclude-test-code";
            setupInitialRestaurants(pickeatCode);

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
            String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
            redisTemplate.getConnectionFactory().getConnection()
                    .subscribe((message, pattern) -> {
                        messageQueue.add(new String(message.getBody()));
                    }, topicName.getBytes());

            RestaurantExcludeEventRequest request = new RestaurantExcludeEventRequest(pickeatCode);

            // when
            restaurantEventHandler.handleRestaurantExclude(request);

            // then
            String publishedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(publishedMessage).isNotNull(),
                    () -> assertThat(publishedMessage).contains("\"group\":\"RESTAURANT\""),
                    () -> assertThat(publishedMessage).contains("\"groupSequence\":1"),
                    () -> assertThat(publishedMessage).contains("\"action\":\"RESTAURANT_EXCLUDE_EVENT\""),
                    () -> assertThat(publishedMessage).contains("\"pickeatCode\":\"" + pickeatCode + "\""),
                    () -> assertThat(publishedMessage).contains("\"aliveRestaurantIds\"")
            );
        }
    }

    @Nested
    class 참가자_업데이트_이벤트_발행 {

        @Test
        void 참가자_업데이트_이벤트를_발행한다() throws InterruptedException {
            // given
            String pickeatCode = "like-test-code";
            setupInitialRestaurants(pickeatCode);

            BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
            String topicName = SseChannelTopic.RESTAURANT_EVENT_TOPIC.getValue();
            redisTemplate.getConnectionFactory().getConnection()
                    .subscribe((message, pattern) -> {
                        messageQueue.add(new String(message.getBody()));
                    }, topicName.getBytes());

            RestaurantLikeEventRequest request = new RestaurantLikeEventRequest(pickeatCode);

            // when
            restaurantEventHandler.handleRestaurantLike(request);

            // then
            String publishedMessage = messageQueue.poll(5, TimeUnit.SECONDS);
            assertAll(
                    () -> assertThat(publishedMessage).isNotNull(),
                    () -> assertThat(publishedMessage).contains("\"group\":\"RESTAURANT\""),
                    () -> assertThat(publishedMessage).contains("\"groupSequence\":1"),
                    () -> assertThat(publishedMessage).contains("\"action\":\"RESTAURANT_LIKE_EVENT\""),
                    () -> assertThat(publishedMessage).contains("\"pickeatCode\":\"" + pickeatCode + "\""),
                    () -> assertThat(publishedMessage).contains("\"likeCountByRestaurant\"")
            );
        }
    }

    private void setupInitialRestaurants(String pickeatCode) {
        Restaurants restaurants = new Restaurants(List.of(
                RestaurantFixture.create("마라탕"),
                RestaurantFixture.create("김치찌개")
        ));
        restaurantsStorage.setupRestaurants(pickeatCode, restaurants);
    }
}
