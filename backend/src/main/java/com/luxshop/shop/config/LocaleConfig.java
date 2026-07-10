package com.luxshop.shop.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Internationalization wiring: the request locale is taken from the
 * Accept-Language header (default English), and Bean Validation messages are
 * resolved through the Spring MessageSource so they are localized per request.
 */
@Configuration
public class LocaleConfig {

    // 18 supported languages. Missing translations fall back to English via the MessageSource.
    public static final List<String> SUPPORTED_LANGUAGES = List.of(
            "en", "ka", "ru", "de", "fr", "es", "it", "pt",
            "tr", "az", "uk", "pl", "nl", "ar", "zh", "ja", "ko", "hi");

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(SUPPORTED_LANGUAGES.stream().map(Locale::forLanguageTag).toList());
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.ENGLISH);
        return source;
    }

    /**
     * Make the validator resolve {code} messages through the MessageSource, using
     * the current request locale (set by the LocaleResolver).
     */
    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
