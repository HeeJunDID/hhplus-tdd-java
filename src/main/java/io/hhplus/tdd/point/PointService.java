package io.hhplus.tdd.point;

public interface PointService {

    /**
     * 고객의 id 기준으로 포인트를 조회한다.
     * @return UserPoint
     */
    public UserPoint getUserPoint(long id);

}
