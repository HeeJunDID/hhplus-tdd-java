package io.hhplus.tdd.pointTest;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.PointServiceImpl;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointRepository;
import org.apache.catalina.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    PointService pointService = new PointServiceImpl();
    @Mock
    UserPointRepository userPointRepository;

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
}
