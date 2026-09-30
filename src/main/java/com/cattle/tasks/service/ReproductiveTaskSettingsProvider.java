package com.cattle.tasks.service;

import com.cattle.services.SiteSettingResolver;
import com.cattle.tasks.planner.ReproductiveTaskSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Parámetros vigentes del ciclo reproductivo. D4: las settings viven por sitio y las
 * tareas por finca; sin selector de finca/sitio, el sitio se fija por propiedad.
 */
@Service
public class ReproductiveTaskSettingsProvider {

    private final SiteSettingResolver resolver;
    private final String siteId;

    public ReproductiveTaskSettingsProvider(SiteSettingResolver resolver,
                                            @Value("${reproductive-tasks.site-id:001}") String siteId) {
        this.resolver = resolver;
        this.siteId = siteId;
    }

    public ReproductiveTaskSettings current() {
        return ReproductiveTaskSettings.from(resolver.resolveAll(siteId));
    }
}
