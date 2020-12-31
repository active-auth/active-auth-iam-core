package cn.glogs.activeauth.iamcore.service;

import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
import org.springframework.data.domain.Page;

public interface FillingTemplateService {
    Page<FillingTemplate> pagingAppDomains(int page, int size);

    FillingTemplate addAppDomain(FillingTemplate newAppDomain);

    void updateAppDomain(Long toUpdateId, FillingTemplate updatedAppDomain);

    void deleteAppDomainById(Long toDeleteId);
}
