package com.gbm.taskapi.helper;

import com.gbm.taskapi.dto.TokenInfoDto;
import com.gbm.taskapi.dto.UserCacheDto;
import com.gbm.taskapi.dto.request.RegisterRequest;
import com.gbm.taskapi.dto.response.AuthResponse;
import com.gbm.taskapi.dto.service.AuthResult;
import com.gbm.taskapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserCacheDto toCacheableDto(User user);

    User toUser(RegisterRequest request);

    @Mapping(target = "type", constant = "Bearer")
    AuthResponse toAuthResponse(AuthResult result);

    AuthResult toAuthResult(String accessToken, String refreshToken, User user);

    TokenInfoDto toTokenInfoDto(User user);
}
