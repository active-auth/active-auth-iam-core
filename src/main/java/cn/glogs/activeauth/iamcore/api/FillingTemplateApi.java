package cn.glogs.activeauth.iamcore.api;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
import cn.glogs.activeauth.iamcore.exception.HTTP404Exception;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.service.FillingTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FillingTemplateApi {
    private final FillingTemplateService fillingTemplateService;

    public FillingTemplateApi(FillingTemplateService fillingTemplateService) {
        this.fillingTemplateService = fillingTemplateService;
    }

    @GetMapping("/filling-templates/{templateId}")
    public RestResultPacker<FillingTemplate.Vo> getTemplates(@PathVariable Long templateId) throws HTTP404Exception {
        try {
            return RestResultPacker.success(fillingTemplateService.getTemplate(templateId).vo());
        } catch (NotFoundException e) {
            throw new HTTP404Exception(e);
        }
    }
}
