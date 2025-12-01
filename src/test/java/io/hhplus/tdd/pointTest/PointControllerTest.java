package io.hhplus.tdd.pointTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.exception.PointServiceException;
import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PatchMapping;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PointService pointService;

    @Test
    @DisplayName("GET point/{id} 특정 유저의 포인트를 조회하는 기능")
    void getPoint_returnSucces() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 100L, Instant.parse("2025-11-29T00:00:00Z").toEpochMilli());
        when(pointService.getUserPoint(1L)).thenReturn(userPoint);

        mockMvc.perform(get("/point/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(100L))
                .andExpect(jsonPath("$.updateMillis").value(Instant.parse("2025-11-29T00:00:00Z").toEpochMilli()));
    }

    /**
     * 포인트 충전/이용내역 조회
     */
    @Test
    @DisplayName("GET point/{id}/histories 특정 유저의 포인트 내역을 조회하는 기능")
    void getPointHistoryById_returnSuccess() throws Exception {
        long id = 1L;
        List<PointHistory> pointHistoryList = List.of(
                new PointHistory(1L, id, 300L, TransactionType.CHARGE, Instant.parse("2025-11-18T00:00:00Z").toEpochMilli()),
                new PointHistory(2L, id, 200L, TransactionType.USE, Instant.parse("2025-11-18T00:00:00Z").toEpochMilli())
        );
        when(pointService.getPointHistory(id)).thenReturn(pointHistoryList);

        mockMvc.perform(get("/point/{id}/histories", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(300L))
                .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.toString()))
                .andExpect(jsonPath("$[1].amount").value(200L))
                .andExpect(jsonPath("$[1].type").value(TransactionType.USE.toString()));
    }

    /**
     * 포인트 충전 성공
     */
    @Test
    @DisplayName("PATCH /point/{id}/charge 특정 유저의 포인트를 충전 테스트")
    void chargeUserPoint_returnSuccess() throws Exception {
        //given
        long id = 1L;
        UserPoint userPoint = new UserPoint(id, 100L, Instant.parse("2025-11-30T00:00:00Z").toEpochMilli());
        //when
        when(pointService.chargeUserPoint(id, 100L)).thenReturn(userPoint);
        //then
        mockMvc.perform(
                        patch("/point/{id}/charge", 1L)
                                .content("100")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(100L));
    }

    /**
     * 포인트 충전 실패
     */
    @Test
    @DisplayName("PATCH /point/{id}/charge 특정 유저의 포인트를 충전 테스트")
    void chargeUserPoint_returnFail() throws Exception {
        //given
        long id = 1L;
        long wrongAmount = -1L;

        //when
        when(pointService.chargeUserPoint(id, wrongAmount)).thenThrow(new PointServiceException("최소 충전 금액은 " + PointPolicy.MINIMUM_CHARGE + "이상 입니다."));

        //then
        mockMvc.perform(
                        patch("/point/{id}/charge", 1L)
                                .content(String.valueOf(wrongAmount))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").value("최소 충전 금액은 " + PointPolicy.MINIMUM_CHARGE + "이상 입니다."));
    }

    /**
     * 포인트 정상 사용
     */
    @Test
    @DisplayName("PATCH /point/{id}/use")
    void usePoint_returnSuccess() throws Exception {
        // given
        long id = 1L;
        long useAmount = 10L;
        long remainingPoint = 90L;
        UserPoint userPoint = new UserPoint(id, remainingPoint, Instant.parse("2025-11-30T00:00:00Z").toEpochMilli());
        // when
        when(pointService.useUserPoint(id, useAmount)).thenReturn(userPoint);
        // then
        mockMvc.perform(
                        patch("/point/{id}/use", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.valueOf(useAmount))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(90L));
    }
}
