package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepository {
    private UserPointTable userPointTable;

    public UserPoint selectById(long id) {
        return userPointTable.selectById(id);
    }


}
