package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import org.springframework.data.domain.Page;

public interface FillingTemplateService {
    FillingTemplate getTemplate(Long id) throws NotFoundException;

    FillingTemplate getTemplate(String serviceCode) throws NotFoundException;

    Page<FillingTemplate> pagingAppDomains(int page, int size);

    FillingTemplate addAppDomain(FillingTemplate newAppDomain);

    void updateAppDomain(Long toUpdateId, FillingTemplate updatedAppDomain);

    void deleteAppDomainById(Long toDeleteId);
}
