package com.gbm.taskapi.helper;

import com.gbm.taskapi.dto.UserCacheDto;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
import com.gbm.taskapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserCacheDto toCacheableDto(User user);

    User toUser(RegisterRequest request);

    AuthResponse toAuth(String token, User savedUser);
}
