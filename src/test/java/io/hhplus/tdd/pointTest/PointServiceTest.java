package io.hhplus.tdd.pointTest;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

//    @InjectMocks
//    PointHistoryService pointHistoryService;

    @InjectMocks
    PointService pointService;

    /**
     * 작성 이유 : 유효하지 않은 고객의 포인트 조회 요청 테스트
     */
    @Test
    @DisplayName("Red:존재하지 않는 고객으로 포인트 조회")
    void getPoint_invalidId_ReturnDefaultUserPoint() {

        //given
        long id = 99L;
        UserPoint emptyResult = UserPoint.empty(id);
        given(userPointRepository.selectById(id)).willReturn(emptyResult);

        //when
        UserPoint userPoint = pointService.getUserPoint(id);

        //then
        // 존재하지 않는 고객으로 조회 시
        assertThat(userPoint).isEqualTo(emptyResult);
        assertThat(userPoint.point()).isEqualTo(emptyResult.point());
        assertThat(userPoint.updateMillis()).isEqualTo(emptyResult.updateMillis());
    }

    /**
     * 작성이유 : 유효한 고객의 포인트 조회 요청 테스트
     */
    @Test
    @DisplayName("Green:존재하는 고객으로 포인트 조회")
    void getPoint_validId_ReturnsValidUserPoint() {

        //given
        long id = 1L;
        UserPoint expectedResult = new UserPoint(1L, 1000L, Instant.parse("2025-11-17T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(id)).willReturn(expectedResult);

        //when
        UserPoint userPoint = pointService.getUserPoint(id);

        //then
        //유효한 고객으로 조회
        assertThat(userPoint).isEqualTo(expectedResult);
        assertThat(userPoint.id()).isEqualTo(expectedResult.id());
        assertThat(userPoint.point()).isEqualTo(expectedResult.point());
        assertThat(userPoint.updateMillis()).isEqualTo(expectedResult.updateMillis());
    }

    /**
     * 작성 이유 : 유효하지 않은 유저의 포인트 충전/이용 내역을 조회 테스트
     */
    @Test
    @DisplayName("Red: 존재하지 않는 유저의 포인트 충전/이용 내역을 조회")
    public void getPointHistory_nonHistoryUser_returnDefaultPointHistory() {
        //given
        long id = 99L;
        List<PointHistory> emptyPointHistoryList = List.of();
        given(pointHistoryRepository.selectAllByUserId(id)).willReturn(emptyPointHistoryList);

        //when
        List<PointHistory> pointHistoryList = pointService.getPointHistory(id);

        //then
        assertThat(pointHistoryList).isEqualTo(emptyPointHistoryList);
        assertThat(pointHistoryList).isEmpty();
    }

    /**
     * 작성이유 : 유효한 유저의 포인트 충전/이용 내역을 조회하는경우 테스트
     */
    @Test
    @DisplayName("Green: 유저의 포인트 충전/이용 내역을 조회")
    public void getPointHistory_invalidUser_returnDefaultPointHistory() {
        //given
        long id = 1L;
        List<PointHistory> pointHistoryList = List.of(
                new PointHistory(1L, id, 300L, TransactionType.CHARGE, Instant.parse("2025-11-18T00:00:00Z").toEpochMilli()),
                new PointHistory(2L, id, 200L, TransactionType.USE, Instant.parse("2025-11-18T00:00:00Z").toEpochMilli())
                );
        given(pointHistoryRepository.selectAllByUserId(id)).willReturn(pointHistoryList);
        //when
        List<PointHistory> result = pointService.getPointHistory(id);
        //then
        assertThat(result).isEqualTo(pointHistoryList);
        assertThat(result.get(0).userId()).isEqualTo(1L);
        assertThat(result.get(1).userId()).isEqualTo(1L);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(result.get(1).amount()).isEqualTo(200L);
    }

    /**
     * 작성 이유 : 고객이 최소 충전금액(1원) 미만으로 충전 시도 대비
     */
    @Test
    @DisplayName("Red: 포인트 충전 실패")
    void chargePoint_invalidPoint_returnFailChargePoint() {
        //given
        long id = 1L;
        long nowAmount = 100L;
        long chargeAmount = -100L;
        long now = Instant.now().toEpochMilli();
        TransactionType type = TransactionType.CHARGE;
        UserPoint nowUserPoint = new UserPoint(id, nowAmount, now);
        given(userPointRepository.selectById(id)).willReturn(nowUserPoint);

        //when

        //then
        assertThatThrownBy(() -> pointService.chargeUserPoint(id, chargeAmount)).isInstanceOf(RuntimeException.class)
          .hasMessage("최소 충전 금액은 " + PointPolicy.MINIMUM_CHARGE + "이상 입니다.");

        then(pointHistoryRepository).shouldHaveNoInteractions();

    }

    /**
     * 작성 이유 : 고객이 최소충전금액(1원) 이상 충전 시에 정상 충전 여부 검토
     */
    @Test
    @DisplayName("Green: 포인트 충전 성공")
    void chargePoint_validPoint_returnSuccessChargePoint() {
        // given
        long id = 1L;
        long nowAmount = 100L;
        long chargeAmount = 1L;
        TransactionType type = TransactionType.CHARGE;

        given(userPointRepository.selectById(id))
                .willReturn(new UserPoint(id, nowAmount, Instant.parse("2025-11-24T00:00:00Z").toEpochMilli()));
        given(userPointRepository.useOrChargePointById(id, nowAmount + chargeAmount))
                .willReturn(new UserPoint(id, nowAmount + chargeAmount, Instant.now().toEpochMilli()));

        // when
        UserPoint pointCharge = pointService.chargeUserPoint(id, chargeAmount);


        // then
        assertThat(pointCharge.point()).isEqualTo(101L);
        then(pointHistoryRepository).should(times(1)).insertPointHistory(eq(id), eq(chargeAmount), eq(type), anyLong());
    }

    /**
     * 작성 이유 : 고객이 유효하지 않은 포인트금액(0원 이하)을 사용하려 할때 테스트
     */
    @Test
    @DisplayName("RED: 유효하지않은 포인트 금액으로 사용 요청")
    void usePoint_invalidPoint_returnFailToUsePoint() {
        //given
        long id = 1L;
        long largeAmount = -1L;
        UserPoint userPoint = new UserPoint(id, 99L, Instant.parse("2025-11-26T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(1L)).willReturn(userPoint);
        //when

        //then
        // 고객이 사용하려는 포인트가(100L) 가지고있는 포인트(99L)보다 클때
        assertThatThrownBy(() -> {
            pointService.useUserPoint(id, largeAmount);
        }).isInstanceOf(RuntimeException.class).hasMessage("최소 사용 금액은 0원 이상입니다.");

        then(pointHistoryRepository).shouldHaveNoInteractions();
        then(userPointRepository).shouldHaveNoMoreInteractions();

    }

    /**
     * 작성 이유 : 고객이 보유한 포인트보다 큰 금액으로 사용을 요청할때 테스트
     */
    @Test
    @DisplayName("RED: 보유 포인트 보다 큰 금액으로 사용요청")
    void useMorePoint_havePoint_returnFailToUsePoint() {
        //given
        long id = 1L;
        long largeAmount = 100L;
        UserPoint userPoint = new UserPoint(id, 99L, Instant.parse("2025-11-26T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(1L)).willReturn(userPoint);
        //when

        //then
        // 고객이 사용하려는 포인트가(100L) 가지고있는 포인트(99L)보다 클때
        assertThatThrownBy(() -> {
            pointService.useUserPoint(id, largeAmount);
        }).isInstanceOf(RuntimeException.class).hasMessage("사용하려는 포인트는 보유하고 있는 포인트보다 작아야 합니다.");

        then(pointHistoryRepository).shouldHaveNoInteractions();
        then(userPointRepository).shouldHaveNoMoreInteractions();

    }

    /**
     * 작성 이유 : 고객이 유효한 값으로 보유한도내에서 사용을 요청할때 테스트
     */
    @Test
    @DisplayName("Green: 보유 포인트내 정상 사용 테스트")
    void usePoint_havePoint_returnSuccessToUsePoint() {
        //given
        long id = 1L;
        long nowAmount = 100L;
        long useAmount = 99L;
        UserPoint userPoint = new UserPoint(id, nowAmount, Instant.parse("2025-11-26T00:00:00Z").toEpochMilli());
        given(userPointRepository.selectById(id)).willReturn(userPoint);
        given(userPointRepository.useOrChargePointById(id, nowAmount - useAmount))
                .willReturn(new UserPoint(id, nowAmount - useAmount, Instant.now().toEpochMilli()));
        //when
        UserPoint result = pointService.useUserPoint(id, useAmount);

        //then
        assertThat(result.point()).isEqualTo(1L);
        then(pointHistoryRepository).should(times(1)).insertPointHistory(eq(id), eq(useAmount), eq(TransactionType.USE), anyLong());


    }
}
