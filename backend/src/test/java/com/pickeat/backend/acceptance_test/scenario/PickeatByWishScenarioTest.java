package com.pickeat.backend.acceptance_test.scenario;

import static com.pickeat.backend.acceptance_test.piece.login.LoginPieceTest.회원가입;
import static com.pickeat.backend.acceptance_test.piece.login.LoginPieceTest.회원가입을_위한_코드_처리;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.모든_참가자_메타데이터_조회;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.모든_참가자_상태_조회;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.자신의_참가자_코드_조회;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.참가자_생성;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.참가자_선택_완료_표시;
import static com.pickeat.backend.acceptance_test.piece.participant.ParticipantPieceTest.참가자_선택_완료_표시_취소;
import static com.pickeat.backend.acceptance_test.piece.pickeat.PickeatPieceTest.방_내부용_픽잇_생성;
import static com.pickeat.backend.acceptance_test.piece.pickeat.PickeatPieceTest.픽잇_결과_조회;
import static com.pickeat.backend.acceptance_test.piece.pickeat.PickeatPieceTest.픽잇_메타데이터_조회;
import static com.pickeat.backend.acceptance_test.piece.pickeat.PickeatPieceTest.픽잇_상태_조회;
import static com.pickeat.backend.acceptance_test.piece.pickeat.PickeatPieceTest.픽잇_종료;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.식당_메타데이터_조회;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.식당_상태_조회;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.식당_제외;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.식당_좋아요;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.식당_좋아요_취소;
import static com.pickeat.backend.acceptance_test.piece.restaurant.RestaurantPieceTest.위시리스트_기반_식당_후보_생성;
import static com.pickeat.backend.acceptance_test.piece.room.RoomPieceTest.방_단일_조회;
import static com.pickeat.backend.acceptance_test.piece.room.RoomPieceTest.방_생성;
import static com.pickeat.backend.acceptance_test.piece.room.RoomPieceTest.방_초대;
import static com.pickeat.backend.acceptance_test.piece.user.UserPieceTest.유저_검색;
import static com.pickeat.backend.acceptance_test.piece.wish.WishPieceTest.위시_목록_조회;
import static com.pickeat.backend.acceptance_test.piece.wish.WishPieceTest.위시_생성;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.login.application.dto.request.AuthCodeRequest;
import com.pickeat.backend.login.application.dto.request.SignupRequest;
import com.pickeat.backend.login.application.dto.response.TokenResponse;
import com.pickeat.backend.participant.application.dto.request.ParticipantRequest;
import com.pickeat.backend.participant.application.dto.response.MyParticipantCodeResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantResponse;
import com.pickeat.backend.participant.application.dto.response.ParticipantStateResponse;
import com.pickeat.backend.pickeat.application.dto.request.PickeatRequest;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatResultResponse;
import com.pickeat.backend.pickeat.application.dto.response.PickeatStateResponse;
import com.pickeat.backend.restaurant.application.dto.request.RestaurantExcludeRequest;
import com.pickeat.backend.restaurant.application.dto.request.WishRestaurantRequest;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantResponse;
import com.pickeat.backend.restaurant.application.dto.response.RestaurantStateResponse;
import com.pickeat.backend.restaurant.domain.FoodCategory;
import com.pickeat.backend.room.application.dto.request.RoomInvitationRequest;
import com.pickeat.backend.room.application.dto.request.RoomRequest;
import com.pickeat.backend.room.application.dto.response.RoomResponse;
import com.pickeat.backend.support.AcceptanceTest;
import com.pickeat.backend.user.application.dto.response.UserResponse;
import com.pickeat.backend.wish.application.dto.request.WishRequest;
import com.pickeat.backend.wish.application.dto.response.WishResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PickeatByWishScenarioTest extends AcceptanceTest {

    @Test
    void 위시_기반_픽잇_플로우() {

        // 회원 가입
        TokenResponse readerProviderToken = 회원가입을_위한_코드_처리(new AuthCodeRequest("test_code1", "kakao", "url"));
        TokenResponse readerAuthToken = 회원가입(new SignupRequest("reader"), readerProviderToken.token());
        TokenResponse follower1ProviderToken = 회원가입을_위한_코드_처리(new AuthCodeRequest("test_code2", "kakao", "url"));
        TokenResponse follower1AuthToken = 회원가입(new SignupRequest("follwer1"), follower1ProviderToken.token());
        TokenResponse follower2ProviderToken = 회원가입을_위한_코드_처리(new AuthCodeRequest("test_code3", "kakao", "url"));
        TokenResponse follower2AuthToken = 회원가입(new SignupRequest("follwer2"), follower2ProviderToken.token());

        // 방 생성
        RoomResponse room = 방_생성(new RoomRequest("점심크루 모임"), readerAuthToken.token());

        // 방 초대
        List<UserResponse> searchedUsers = 유저_검색("follwer");
        List<Long> invitedUserIds = searchedUsers.stream().map(UserResponse::id).toList();
        방_초대(room.id(), new RoomInvitationRequest(invitedUserIds), readerAuthToken.token());

        // 방 정보 조회
        RoomResponse roomInformation = 방_단일_조회(room.id(), readerAuthToken.token());
        checkUserCountInRoom(roomInformation, 3);

        // 방의 위시리스트 생성
        위시_생성(room.id(),
                new WishRequest("식당1", FoodCategory.KOREAN.getName(), "도로명", List.of("태그1"), "url1"),
                readerAuthToken.token());
        위시_생성(room.id(),
                new WishRequest("식당2", FoodCategory.KOREAN.getName(), "도로명", List.of("태그1"), "url1"),
                readerAuthToken.token());
        위시_생성(room.id(),
                new WishRequest("식당3", FoodCategory.KOREAN.getName(), "도로명", List.of("태그1"), "url1"),
                readerAuthToken.token());
        위시_생성(room.id(),
                new WishRequest("식당4", FoodCategory.KOREAN.getName(), "도로명", List.of("태그1"), "url1"),
                readerAuthToken.token());
        위시_생성(room.id(),
                new WishRequest("식당5", FoodCategory.KOREAN.getName(), "도로명", List.of("태그1"), "url1"),
                readerAuthToken.token());

        // 위시 조회
        List<WishResponse> createdWish = 위시_목록_조회(room.id(), readerAuthToken.token());
        checkWishSizeInRoom(createdWish, 5);

        // 픽잇 생성
        PickeatResponse createdPickeat = 방_내부용_픽잇_생성(
                room.id(),
                readerAuthToken.token(),
                new PickeatRequest("우테코 점심 픽잇"));
        위시리스트_기반_식당_후보_생성(createdPickeat.code(), new WishRestaurantRequest(room.id()));

        // 참여자 생성
        TokenResponse participant1Token = 참가자_생성(new ParticipantRequest("참여자1", createdPickeat.code()));
        TokenResponse participant2Token = 참가자_생성(new ParticipantRequest("참여자2", createdPickeat.code()));
        TokenResponse participant3Token = 참가자_생성(new ParticipantRequest("참여자3", createdPickeat.code()));

        // 초기 픽잇 정보 조회
        PickeatResponse pickeatMeta = 픽잇_메타데이터_조회(participant1Token.token());
        checkPickeatMeta(pickeatMeta, createdPickeat.code(), "우테코 점심 픽잇");

        PickeatStateResponse initialPickeatState = 픽잇_상태_조회(participant1Token.token());
        checkInitialPickeatState(initialPickeatState);

        // 참가자 정보 조회
        MyParticipantCodeResponse participant1Code = 자신의_참가자_코드_조회(participant1Token.token());
        MyParticipantCodeResponse participant2Code = 자신의_참가자_코드_조회(participant2Token.token());
        MyParticipantCodeResponse participant3Code = 자신의_참가자_코드_조회(participant3Token.token());
        List<String> allParticipantCodes = List.of(
                participant1Code.participantCode(),
                participant2Code.participantCode(),
                participant3Code.participantCode());

        List<ParticipantResponse> allParticipantMeta = 모든_참가자_메타데이터_조회(participant1Token.token());
        checkAllParticipantMeta(allParticipantMeta, allParticipantCodes);

        ParticipantStateResponse initialParticipantState = 모든_참가자_상태_조회(participant1Token.token());
        checkInitialParticipantState(initialParticipantState, allParticipantCodes);

        // 초기 식당 정보 조회
        List<RestaurantResponse> restaurantMeta = 식당_메타데이터_조회(participant1Token.token());
        checkRestaurantMeta(restaurantMeta);

        RestaurantStateResponse initialRestaurantStates = 식당_상태_조회(participant1Token.token());
        List<String> allRestaurantCodes = restaurantMeta.stream().map(RestaurantResponse::code).toList();
        checkInitialRestaurantStates(initialRestaurantStates, allRestaurantCodes);

        // 참여자들의 식당 소거
        List<String> excludeRestaurantCodes = allRestaurantCodes.subList(0, 3);
        식당_제외(participant1Token.token(), new RestaurantExcludeRequest(excludeRestaurantCodes.subList(0, 1)));
        식당_제외(participant2Token.token(), new RestaurantExcludeRequest(excludeRestaurantCodes.subList(1, 2)));
        식당_제외(participant3Token.token(), new RestaurantExcludeRequest(excludeRestaurantCodes.subList(2, 3)));

        RestaurantStateResponse restaurantStateAfterExclude = 식당_상태_조회(participant1Token.token());
        checkRestaurantExcludedSuccess(restaurantStateAfterExclude.aliveRestaurantCode(), excludeRestaurantCodes);

        // 참여자들의 식당 좋아요
        List<String> aliveRestaurantCodes = allRestaurantCodes.subList(3, allRestaurantCodes.size());
        식당_좋아요(participant1Token.token(), aliveRestaurantCodes.get(0));
        식당_좋아요(participant2Token.token(), aliveRestaurantCodes.get(1));
        식당_좋아요(participant3Token.token(), aliveRestaurantCodes.get(1));

        RestaurantStateResponse restaurantStatesAfterLike = 식당_상태_조회(participant1Token.token());
        Map<String, Integer> expectedLikeCountAfterLike = Map.of(
                aliveRestaurantCodes.get(0), 1,
                aliveRestaurantCodes.get(1), 2);
        checkRestaurantLikeCount(restaurantStatesAfterLike.likeCountByRestaurant(), expectedLikeCountAfterLike);

        // 참여자들의 식당 좋아요 취소
        식당_좋아요_취소(participant1Token.token(), aliveRestaurantCodes.get(0));

        RestaurantStateResponse restaurantStatesAfterLikeCancel = 식당_상태_조회(participant1Token.token());
        Map<String, Integer> expectedLikeCountAfterLikeCancel = Map.of(
                aliveRestaurantCodes.get(0), 0,
                aliveRestaurantCodes.get(1), 2);
        checkRestaurantLikeCount(restaurantStatesAfterLikeCancel.likeCountByRestaurant(),
                expectedLikeCountAfterLikeCancel);

        // 참여자들의 선택 완료
        참가자_선택_완료_표시(participant1Token.token());
        참가자_선택_완료_표시(participant2Token.token());
        참가자_선택_완료_표시(participant3Token.token());

        ParticipantStateResponse participantStateAfterCompletion = 모든_참가자_상태_조회(participant1Token.token());
        Map<String, Boolean> expectedParticipantStateAfterCompletion = Map.of(
                participant1Code.participantCode(), true,
                participant2Code.participantCode(), true,
                participant3Code.participantCode(), true);
        checkParticipantCompletion(participantStateAfterCompletion.completion(),
                expectedParticipantStateAfterCompletion);

        // 참여자들의 선택 완료 취소
        참가자_선택_완료_표시_취소(participant1Token.token());

        ParticipantStateResponse participantStateAfterCompletionCancel = 모든_참가자_상태_조회(participant1Token.token());
        Map<String, Boolean> expectedParticipantStateAfterCompletionCancel = Map.of(
                participant1Code.participantCode(), false,
                participant2Code.participantCode(), true,
                participant3Code.participantCode(), true);
        checkParticipantCompletion(participantStateAfterCompletionCancel.completion(),
                expectedParticipantStateAfterCompletionCancel);

        // 픽잇 종료
        픽잇_종료(participant1Token.token());

        PickeatStateResponse pickeatStateAfterCompletion = 픽잇_상태_조회(participant1Token.token());
        assertThat(pickeatStateAfterCompletion.isComplete()).isTrue();

        PickeatResultResponse pickeatResult = 픽잇_결과_조회(participant1Token.token());
        assertThat(pickeatResult.code()).isEqualTo(aliveRestaurantCodes.get(1));
    }

    private void checkUserCountInRoom(RoomResponse room, int expectedCount) {
        assertThat(room.userCount()).isEqualTo(expectedCount);
    }

    private void checkWishSizeInRoom(List<WishResponse> wishes, int size) {
        assertThat(wishes).hasSize(size);
    }

    private void checkPickeatMeta(PickeatResponse response, String pickeatCode, String pickeatName) {
        assertAll(
                () -> assertThat(response.code()).isEqualTo(pickeatCode),
                () -> assertThat(response.name()).isEqualTo(pickeatName)
        );
    }

    private void checkInitialPickeatState(PickeatStateResponse response) {
        assertThat(response.isComplete()).isFalse();
    }

    private void checkAllParticipantMeta(List<ParticipantResponse> response, List<String> participantCodes) {
        assertThat(response)
                .as("모든 참가자가 올바르게 생성되어야 합니다.")
                .extracting(ParticipantResponse::participantCode)
                .containsAnyElementsOf(participantCodes);
    }

    private void checkInitialParticipantState(
            ParticipantStateResponse response,
            List<String> participantCodes
    ) {
        assertAll(
                () -> assertThat(response.completion().keySet())
                        .as("모든 참가자에 대한 초기 상태값이 존재해야 합니다.")
                        .containsExactlyInAnyOrderElementsOf(participantCodes),
                () -> assertThat(response.completion().values())
                        .as("모든 참가자의 초기 완료 상태는 false여야 합니다.")
                        .containsOnly(false)
        );
    }

    private void checkRestaurantMeta(List<RestaurantResponse> response) {
        assertThat(response).isNotEmpty();
    }

    private void checkInitialRestaurantStates(
            RestaurantStateResponse response,
            List<String> restaurantCodes
    ) {
        assertAll(
                () -> assertThat(response.aliveRestaurantCode())
                        .as("모든 식당이 소거되지 않고 살아있어야 합니다.")
                        .containsExactlyInAnyOrderElementsOf(restaurantCodes),
                () -> assertThat(response.likeCountByRestaurant().keySet())
                        .as("모든 식당에 대한 초기 좋아요 수가 존재해야 합니다.")
                        .containsExactlyInAnyOrderElementsOf(restaurantCodes),
                () -> assertThat(response.likeCountByRestaurant())
                        .as("모든 생존 식당의 좋아요 수는 0이어야 합니다.")
                        .extractingFromEntries(Entry::getKey)
                        .extracting(code -> response.likeCountByRestaurant().get(code))
                        .containsOnly(0)
        );
    }

    private void checkRestaurantExcludedSuccess(
            Set<String> aliveRestaurantCodes,
            List<String> excludeRestaurantCodes
    ) {
        assertThat(aliveRestaurantCodes).doesNotContainAnyElementsOf(excludeRestaurantCodes);
    }

    private void checkRestaurantLikeCount(
            Map<String, Integer> allLikeCount,
            Map<String, Integer> expectedLikeCount
    ) {
        assertThat(allLikeCount)
                .as("기대하는 식당별 좋아요 수가 모두 포함되어 있어야 합니다.")
                .containsAllEntriesOf(expectedLikeCount);
    }

    private void checkParticipantCompletion(
            Map<String, Boolean> allCompletion,
            Map<String, Boolean> expectedCompletion
    ) {
        assertThat(allCompletion)
                .as("참가자들의 완료 상태가 기대값과 일치해야 합니다.")
                .containsAllEntriesOf(expectedCompletion);
    }
}
