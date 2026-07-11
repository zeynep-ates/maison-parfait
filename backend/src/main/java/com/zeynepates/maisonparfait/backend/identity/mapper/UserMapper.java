package com.zeynepates.maisonparfait.backend.identity.mapper;

import com.zeynepates.maisonparfait.backend.identity.User;
import com.zeynepates.maisonparfait.backend.identity.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "emailVerified", expression = "java(user.getEmailVerifiedAt() != null)")
    UserResponse toResponse(User user);
}
