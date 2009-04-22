/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.web.intercept;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.matcher.AuthenticationMatcher.anAuthenticationWithUsername;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.MockApplicationEventPublisher;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.intercept.RunAsManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.intercept.WebInvocationPrivilegeEvaluator;
import org.springframework.security.web.util.FilterInvocationUtils;


/**
 * Tests {@link org.springframework.security.web.intercept.WebInvocationPrivilegeEvaluator}.
 *
 * @author Ben Alex
 * @version $Id$
 */
public class WebInvocationPrivilegeEvaluatorTests {
    private Mockery jmock = new JUnit4Mockery();
    private AuthenticationManager am;
    private AccessDecisionManager adm;
    private FilterInvocationSecurityMetadataSource ods;
    private RunAsManager ram;
    private FilterSecurityInterceptor interceptor;

    //~ Methods ========================================================================================================

    @Before
    public final void setUp() throws Exception {
        interceptor = new FilterSecurityInterceptor();
        am = jmock.mock(AuthenticationManager.class);
        ods = jmock.mock(FilterInvocationSecurityMetadataSource.class);
        adm = jmock.mock(AccessDecisionManager.class);
        ram = jmock.mock(RunAsManager.class);
        interceptor.setAuthenticationManager(am);
        interceptor.setSecurityMetadataSource(ods);
        interceptor.setAccessDecisionManager(adm);
        interceptor.setRunAsManager(ram);
        interceptor.setApplicationEventPublisher(new MockApplicationEventPublisher(true));
        SecurityContextHolder.clearContext();
    }

    @After
    public void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void allowsAccessIfAccessDecisionMangerDoes() throws Exception {
        Authentication token = new TestingAuthenticationToken("test", "Password", "MOCK_INDEX");
        FilterInvocation fi = FilterInvocationUtils.create("/foo/index.jsp");

        WebInvocationPrivilegeEvaluator wipe = new WebInvocationPrivilegeEvaluator();
        wipe.setSecurityInterceptor(interceptor);
        wipe.afterPropertiesSet();

        jmock.checking(new Expectations() {{
            ignoring(ram); ignoring(ods);
            oneOf(adm).decide(with(anAuthenticationWithUsername("test")), with(anything()), with(aNonNull(List.class)));
        }});

        assertTrue(wipe.isAllowed(fi, token));
        jmock.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deniesAccessIfAccessDecisionMangerDoes() throws Exception {
        Authentication token = new TestingAuthenticationToken("test", "Password", "MOCK_INDEX");
        FilterInvocation fi = FilterInvocationUtils.create("/foo/index.jsp");

        WebInvocationPrivilegeEvaluator wipe = new WebInvocationPrivilegeEvaluator();
        wipe.setSecurityInterceptor(interceptor);
        wipe.afterPropertiesSet();

        jmock.checking(new Expectations() {{
            ignoring(ram); ignoring(ods);
            oneOf(adm).decide(with(anAuthenticationWithUsername("test")), with(anything()), with(aNonNull(List.class)));
                will(throwException(new AccessDeniedException("")));
        }});

        assertFalse(wipe.isAllowed(fi, token));
        jmock.assertIsSatisfied();
    }
}