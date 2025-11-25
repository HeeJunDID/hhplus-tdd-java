package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private static final Logger log = LoggerFactory.getLogger(PointService.class);
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 고객 id 기준으로 포인트를 조회한다.
     * @param id
     * @return
     */
    public UserPoint getUserPoint(long id) {
        return userPointRepository.selectById(id);
    }

    /**'
     * 고객 id 기준으로 포인트를 충전한다.
     * @param id
     * @param amount
     * @return UserPoint
     */
    public UserPoint chargeUserPoint(long id, long amount) {
        // id로 유저의 현재 포인트를 조회
        UserPoint nowUserPoint = getUserPoint(id);
        // 0보다 큰 포인트를 충전하려는지 확인
        validChargePoint(amount);
        UserPoint userPoint = userPointRepository.chargePointById(nowUserPoint.id(), nowUserPoint.point() + amount);
        //pointHistory 테이블에 업데이트
        insertPointHistory(id, amount, TransactionType.CHARGE, Instant.now().toEpochMilli());

        // id 기준으로 현재포인트 + 충전하고자 하는 포인트만큼 더해준다.
        return userPoint;

    }

    private void validChargePoint(long amount) {
        if (amount < PointPolicy.MINIMUM_CHARGE) {
            throw new RuntimeException("최소 충전 금액은 " + PointPolicy.MINIMUM_CHARGE + "이상 입니다.");
        }
    }

    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    public void insertPointHistory(long userId, long amount, TransactionType type, long updateMillis) {
        pointHistoryRepository.insertPointHistory(userId, amount, type, updateMillis);
    }
}

