/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HttpServletRequestMock implements HttpServletRequest {

	StringBuffer requestUrl;
	String queryString;

	@Override
	public Object getAttribute(final String name) {

		return null;
	}

	@Override
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
	public Enumeration getParameterNames() {

		return null;
	}

	@Override
	public String[] getParameterValues(final String name) {

		return null;
	}

	@Override
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
	public Enumeration getHeaders(final String name) {

		return null;
	}

	@Override
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

}
