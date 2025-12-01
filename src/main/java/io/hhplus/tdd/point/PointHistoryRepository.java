package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;
    public List<PointHistory> selectAllByUserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    public PointHistory insertPointHistory(long userId, long amount, TransactionType type, long updateMillis) {
        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }
}
