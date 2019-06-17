/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.jira.mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class HttpServletRequestMock implements HttpServletRequest {

    StringBuffer requestUrl;

    String queryString;

    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(final String name) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(final String name) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Map getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(final String name, final Object o) {

    }

    @Override
    public void removeAttribute(final String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return null;
    }

    @Override
    public String getRealPath(final String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public long getDateHeader(final String name) {
        return 0;
    }

    @Override
    public String getHeader(final String name) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getHeaders(final String name) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(final String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(final String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(final String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return requestUrl;
    }

    public void setRequestURL(final StringBuffer requestUrl) {
        this.requestUrl = requestUrl;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(final boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public ServletContext getServletContext() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // Auto-generated method stub
        return null;
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
            throws IllegalStateException {
        // Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        // Auto-generated method stub
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        // Auto-generated method stub
        return false;
    }

    @Override
    public void login(final String username, final String password) throws ServletException {
        // Auto-generated method stub

    }

    @Override
    public void logout() throws ServletException {
        // Auto-generated method stub
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // Auto-generated method stub
        return null;
    }

    @Override
    public Part getPart(final String name) throws IOException, ServletException {
        // Auto-generated method stub
        return null;
    }

}
