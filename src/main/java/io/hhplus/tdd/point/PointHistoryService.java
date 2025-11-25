package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;

    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    public void insertPointHistory(long userId, long amount, TransactionType type, long updateMillis) {
        pointHistoryRepository.insertPointHistory(userId, amount, type, updateMillis);
    }
}
