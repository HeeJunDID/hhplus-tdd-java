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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    PointService pointService = new PointServiceImpl();
    @InjectMocks
    PointHistoryService pointHistoryService = new PointHistoryServiceImpl();
    @Mock
    UserPointRepository userPointRepository;
    @Mock
    PointHistoryRepository pointHistoryRepository;

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
        List<PointHistory> pointHistoryList = pointHistoryService.getPointHistory(id);

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
        List<PointHistory> result = pointHistoryService.getPointHistory(id);
        //then
        assertThat(result).isEqualTo(pointHistoryList);
        assertThat(result.get(0).userId()).isEqualTo(1L);
        assertThat(result.get(1).userId()).isEqualTo(1L);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
        assertThat(result.get(1).amount()).isEqualTo(200L);
    }
}
