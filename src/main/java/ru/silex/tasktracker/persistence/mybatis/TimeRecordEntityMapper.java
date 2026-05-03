package ru.silex.tasktracker.persistence.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ru.silex.tasktracker.persistence.model.TimeRecordEntity;

import java.time.Instant;
import java.util.List;

@Mapper
public interface TimeRecordEntityMapper {

    int insert(TimeRecordEntity entity);

    List<TimeRecordEntity> selectByEmployeeAndStartedAtBetween(
            @Param("employeeId") Long employeeId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toInclusive") Instant toInclusive);
}
