package com.zeynepates.maisonparfait.backend.identity.mapper;

import com.zeynepates.maisonparfait.backend.identity.dto.UserResponse;
import com.zeynepates.maisonparfait.backend.modules.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "emailVerified", expression = "java(user.getEmailVerifiedAt() != null)")
    UserResponse toResponse(User user);
}
