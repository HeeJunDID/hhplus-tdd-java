package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointServiceImpl implements PointService{
    private static final Logger log = LoggerFactory.getLogger(PointServiceImpl.class);
    private UserPointRepository userPointRepository;

    /**
     * 고객 id 기준으로 포인트를 조회한다.
     * @param id
     * @return
     */
    @Override
    public UserPoint getUserPoint(long id) {
        return userPointRepository.selectById(id);
    }

}
