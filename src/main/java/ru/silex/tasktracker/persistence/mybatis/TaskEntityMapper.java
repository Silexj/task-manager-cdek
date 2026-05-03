package ru.silex.tasktracker.persistence.mybatis;

import org.apache.ibatis.annotations.Mapper;
import ru.silex.tasktracker.persistence.model.TaskEntity;

@Mapper
public interface TaskEntityMapper {

    int insert(TaskEntity task);

    int update(TaskEntity task);

    TaskEntity selectById(Long id);
}
