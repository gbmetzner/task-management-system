package com.gbm.taskapi.helper;

import com.gbm.taskapi.dto.response.ProjectSummary;
import com.gbm.taskapi.dto.response.TaskResponse;
import com.gbm.taskapi.dto.response.UserSummary;
import com.gbm.taskapi.dto.service.TaskResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    TaskResponse toTaskResponse(TaskResult result);

    ProjectSummary toProjectSummary(TaskResult.ProjectInfo projectInfo);

    UserSummary toUserSummary(TaskResult.UserInfo userInfo);
}
