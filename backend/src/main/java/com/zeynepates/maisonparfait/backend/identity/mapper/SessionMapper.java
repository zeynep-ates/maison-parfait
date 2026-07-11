package com.zeynepates.maisonparfait.backend.identity.mapper;

import com.zeynepates.maisonparfait.backend.identity.RefreshToken;
import com.zeynepates.maisonparfait.backend.identity.dto.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "current", source = "current")
    SessionResponse toResponse(RefreshToken token, boolean current);
}
