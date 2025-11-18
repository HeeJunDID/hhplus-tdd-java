package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryServiceImpl implements PointHistoryService {

    private PointHistoryRepository pointHistoryRepository;

    @Override
    public List<PointHistory> getPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }
}
