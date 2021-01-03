package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
import cn.glogs.activeauth.iamcore.repository.FillingTemplateRepository;
import cn.glogs.activeauth.iamcore.service.FillingTemplateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class FillingTemplateServiceImpl implements FillingTemplateService {

    private final FillingTemplateRepository fillingTemplateRepository;

    public FillingTemplateServiceImpl(FillingTemplateRepository fillingTemplateRepository) {
        this.fillingTemplateRepository = fillingTemplateRepository;
    }

    @Override
    public Page<FillingTemplate> pagingAppDomains(int page, int size) {
        return fillingTemplateRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public FillingTemplate addAppDomain(FillingTemplate newAppDomain) {
        return fillingTemplateRepository.save(newAppDomain);
    }

    @Override
    public void updateAppDomain(Long toUpdateId, FillingTemplate updatedAppDomain) {
    }

    @Override
    public void deleteAppDomainById(Long toDeleteId) {
        fillingTemplateRepository.deleteById(toDeleteId);
    }
}
