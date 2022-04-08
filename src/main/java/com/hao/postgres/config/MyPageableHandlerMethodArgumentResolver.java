package com.hao.postgres.config;

import com.hao.postgres.annotation.ClassMapping;
import com.hao.postgres.util.web.PageableSortConverter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class MyPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        log.info("Resolving argument for {} ...", methodParameter);
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        if (Optional.ofNullable(pageable).map(Pageable::getSort).isEmpty() || pageable.getSort().isUnsorted()) {
            log.info("Return default pageable");
            return pageable;
        }

        try {
            log.info("Going to return customized pageable");
            return PageableSortConverter.convert(pageable, methodParameter.getMethodAnnotation(ClassMapping.class));
        } catch (Exception ex) {
            log.error("failed to convert sort fields", ex);
            return pageable;
        }
    }


}
