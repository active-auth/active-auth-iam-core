package cn.glogs.activeauth.iamcore.domain.mapper;

import cn.glogs.activeauth.iamcore.domain.AuthorizationPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AuthorizationPolicy$Form_AuthorizationPolicy_Mapper {
    @Mappings({
            @Mapping(target = "name", source = "source.name"),
            @Mapping(target = "effect", source = "source.effect"),
            @Mapping(target = "actions", source = "source.actions"),
            @Mapping(target = "resources", source = "source.resources")
    })
    AuthorizationPolicy sourceToDestination(AuthorizationPolicy.Form source);

    @Mappings({
            @Mapping(target = "name", source = "destination.name"),
            @Mapping(target = "effect", source = "destination.effect"),
            @Mapping(target = "actions", source = "destination.actions"),
            @Mapping(target = "resources", source = "destination.resources")
    })
    AuthorizationPolicy.Form destinationToSource(AuthorizationPolicy destination);
}
