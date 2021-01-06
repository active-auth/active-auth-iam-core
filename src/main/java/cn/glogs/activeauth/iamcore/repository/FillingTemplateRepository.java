package cn.glogs.activeauth.iamcore.repository;

import cn.glogs.activeauth.iamcore.domain.FillingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FillingTemplateRepository extends JpaRepository<FillingTemplate, Long> {
    Optional<FillingTemplate> findByServiceCode(String serviceCode);
}
