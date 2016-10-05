/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.mylasta.direction;

import javax.annotation.Resource;

import org.docksidestage.mylasta.direction.sponsor.OnparadeActionAdjustmentProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeApiFailureHook;
import org.docksidestage.mylasta.direction.sponsor.OnparadeCookieResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeCurtainBeforeHook;
import org.docksidestage.mylasta.direction.sponsor.OnparadeJsonResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeListedClassificationProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeMailDeliveryDepartmentCreator;
import org.docksidestage.mylasta.direction.sponsor.OnparadeSecurityResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeTimeResourceProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeUserLocaleProcessProvider;
import org.docksidestage.mylasta.direction.sponsor.OnparadeUserTimeZoneProcessProvider;
import org.lastaflute.core.direction.CachedFwAssistantDirector;
import org.lastaflute.core.direction.FwAssistDirection;
import org.lastaflute.core.direction.FwCoreDirection;
import org.lastaflute.core.security.InvertibleCryptographer;
import org.lastaflute.core.security.OneWayCryptographer;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.direction.FwDbDirection;
import org.lastaflute.thymeleaf.ThymeleafRenderingProvider;
import org.lastaflute.web.direction.FwWebDirection;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;
import org.lastaflute.web.servlet.filter.cors.CorsHook;

/**
 * @author jflute
 */
public class OnparadeFwAssistantDirector extends CachedFwAssistantDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private OnparadeConfig config;

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    @Override
    protected void prepareAssistDirection(FwAssistDirection direction) {
        direction.directConfig(nameList -> nameList.add("onparade_config.properties"), "onparade_env.properties");
    }

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    @Override
    protected void prepareCoreDirection(FwCoreDirection direction) {
        // this configuration is on onparade_env.properties because this is true only when development
        direction.directDevelopmentHere(config.isDevelopmentHere());

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(config.getDomainTitle(), config.getEnvironmentTitle());

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(config.isFrameworkDebug()); // basically false

        // you can add your own process when your application is booting
        direction.directCurtainBefore(createCurtainBeforeHook());

        direction.directSecurity(createSecurityResourceProvider());
        direction.directTime(createTimeResourceProvider());
        direction.directJson(createJsonResourceProvider());
        direction.directMail(createMailDeliveryDepartmentCreator().create());
    }

    protected OnparadeCurtainBeforeHook createCurtainBeforeHook() {
        return new OnparadeCurtainBeforeHook();
    }

    protected OnparadeSecurityResourceProvider createSecurityResourceProvider() { // #change_it_first
        final InvertibleCryptographer inver = InvertibleCryptographer.createAesCipher("onparade:dockside:");
        final OneWayCryptographer oneWay = OneWayCryptographer.createSha256Cryptographer();
        return new OnparadeSecurityResourceProvider(inver, oneWay);
    }

    protected OnparadeTimeResourceProvider createTimeResourceProvider() {
        return new OnparadeTimeResourceProvider(config);
    }

    protected OnparadeJsonResourceProvider createJsonResourceProvider() {
        return new OnparadeJsonResourceProvider();
    }

    protected OnparadeMailDeliveryDepartmentCreator createMailDeliveryDepartmentCreator() {
        return new OnparadeMailDeliveryDepartmentCreator(config);
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    @Override
    protected void prepareDbDirection(FwDbDirection direction) {
        direction.directClassification(createListedClassificationProvider());
    }

    protected ListedClassificationProvider createListedClassificationProvider() {
        return new OnparadeListedClassificationProvider();
    }

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    @Override
    protected void prepareWebDirection(FwWebDirection direction) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider());
        direction.directCookie(createCookieResourceProvider());
        direction.directAdjustment(createActionAdjustmentProvider());
        direction.directMessage(nameList -> nameList.add("onparade_message"), "onparade_label");
        direction.directApiCall(createApiFailureHook());
        direction.directCors(new CorsHook("http://localhost:5000")); // #change_it
        direction.directHtmlRendering(createHtmlRenderingProvider());
    }

    protected OnparadeUserLocaleProcessProvider createUserLocaleProcessProvider() {
        return new OnparadeUserLocaleProcessProvider();
    }

    protected OnparadeUserTimeZoneProcessProvider createUserTimeZoneProcessProvider() {
        return new OnparadeUserTimeZoneProcessProvider();
    }

    protected OnparadeCookieResourceProvider createCookieResourceProvider() { // #change_it_first
        final InvertibleCryptographer cr = InvertibleCryptographer.createAesCipher("dockside:onparade:");
        return new OnparadeCookieResourceProvider(config, cr);
    }

    protected OnparadeActionAdjustmentProvider createActionAdjustmentProvider() {
        return new OnparadeActionAdjustmentProvider();
    }

    protected OnparadeApiFailureHook createApiFailureHook() {
        return new OnparadeApiFailureHook();
    }

    protected HtmlRenderingProvider createHtmlRenderingProvider() {
        return new ThymeleafRenderingProvider().asDevelopment(config.isDevelopmentHere());
    }
}
