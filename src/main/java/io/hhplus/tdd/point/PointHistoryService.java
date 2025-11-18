package io.hhplus.tdd.point;

import java.util.List;

public interface PointHistoryService {
    List<PointHistory> getPointHistory(long id);
}
