package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.PointServiceException;
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
        // id 기준으로 현재포인트 + 충전하고자 하는 포인트만큼 더해준다.
        UserPoint userPoint = userPointRepository.useOrChargePointById(nowUserPoint.id(), nowUserPoint.point() + amount);
        //pointHistory 테이블에 업데이트
        insertPointHistory(id, amount, TransactionType.CHARGE, Instant.now().toEpochMilli());

        return userPoint;

    }

    /**
     * 포인트 사용
     * @param id
     * @param amount
     * @return
     */
    public UserPoint useUserPoint(long id, long amount) {
        //id로 유저의 현재 포인트를 조회
        UserPoint nowUserPoint = getUserPoint(id);
        // 0이상의 포인트를 사용하려는지 확인
        validUsePoint(amount);
        // 보유하고 있는 포인트보다 많은 포인트를 사용하려는지 확인
        validUseUserPoint(nowUserPoint, amount);
        // 포인트 사용
        UserPoint userPoint = userPointRepository.useOrChargePointById(nowUserPoint.id(), nowUserPoint.point() - amount);
        // pointHistory 테이블에 업데이트
        insertPointHistory(id, amount, TransactionType.USE, Instant.now().toEpochMilli());

        return userPoint;
    }

    /**
     * 포인트 히스토리 내역 리스트 조회
     * @param id
     * @return
     */
    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    /**
     * 포인트 내역 테이블에 업데이트
     * @param userId
     * @param amount
     * @param type
     * @param updateMillis
     */
    public void insertPointHistory(long userId, long amount, TransactionType type, long updateMillis) {
        pointHistoryRepository.insertPointHistory(userId, amount, type, updateMillis);
    }

    /**
     * 최소 충전 금액 이상 충전하는지 여부 확인
     * @param amount
     */
    private void validChargePoint(long amount) {
        if (amount < PointPolicy.MINIMUM_CHARGE) {
            throw new PointServiceException("최소 충전 금액은 " + PointPolicy.MINIMUM_CHARGE + "이상 입니다.");
        }
    }

    /**
     * 최소 사용 금액 체크
     * @param amount
     */
    private void validUsePoint(long amount) {
        if (amount <= 0) {
            throw new PointServiceException("최소 사용 금액은 0원 이상입니다.");
        }
    }

    /**
     * 사용금액이 보유하고 있는 포인트 보다 큰지 여부 확인
     * @param userPoint
     * @param amount
     */
    private void validUseUserPoint(UserPoint userPoint, long amount) {
        if (userPoint.point() < amount) {
            throw new PointServiceException("사용하려는 포인트는 보유하고 있는 포인트보다 작아야 합니다.");
        }

    }
}

