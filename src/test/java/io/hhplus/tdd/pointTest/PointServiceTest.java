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
    @Test
    @DisplayName("Green: 존재하지 않는 유저의 포인트 충전/이용 내역을 조회")
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
    @DisplayName("Green: 포인트 충전 실패")
    void chargePoint_validPoint_returnSuccessChargePoint() {
        // given
        long id = 1L;
        long nowAmount = 100L;
        long chargeAmount = 1L;
        TransactionType type = TransactionType.CHARGE;

        given(userPointRepository.selectById(id))
                .willReturn(new UserPoint(id, nowAmount, Instant.parse("2025-11-24T00:00:00Z").toEpochMilli()));
        given(userPointRepository.chargePointById(id, nowAmount + chargeAmount))
                .willReturn(new UserPoint(id, nowAmount + chargeAmount, Instant.now().toEpochMilli()));

        // when
        UserPoint pointCharge = pointService.chargeUserPoint(id, chargeAmount);


        // then
        assertThat(pointCharge.point()).isEqualTo(101L);
        then(pointHistoryRepository).should(times(1)).insertPointHistory(eq(id), eq(chargeAmount), eq(type), anyLong());
    }

}
