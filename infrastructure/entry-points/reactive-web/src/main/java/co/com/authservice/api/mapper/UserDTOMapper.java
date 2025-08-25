package co.com.authservice.api.mapper;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.response.UserResponseDTO;
import co.com.authservice.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {
    User toModel(CreateUserDTO createUserDTO);
    UserResponseDTO toResponse(User user);
}
