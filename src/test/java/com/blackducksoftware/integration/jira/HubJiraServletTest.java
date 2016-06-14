package com.blackducksoftware.integration.jira;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.jira.mocks.HttpServletRequestMock;
import com.blackducksoftware.integration.jira.mocks.HttpServletResponseMock;
import com.blackducksoftware.integration.jira.mocks.LoginUriProviderMock;
import com.blackducksoftware.integration.jira.mocks.TemplateRendererMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;

public class HubJiraServletTest {

	@Test
	public void testDoGetUserNull() throws Exception {
		final String redirectUrl = "http://testRedirect";
		final StringBuffer requestUrl = new StringBuffer();
		requestUrl.append(redirectUrl);

		final UserManagerMock managerMock = new UserManagerMock();

		final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();

		final TemplateRendererMock rendererMock = new TemplateRendererMock();

		final HttpServletResponseMock responseMock = new HttpServletResponseMock();

		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		requestMock.setRequestURL(requestUrl);

		final HubJiraServlet servlet = new HubJiraServlet(managerMock, loginProviderMock, rendererMock);

		servlet.doGet(requestMock, responseMock);

		assertEquals(redirectUrl, responseMock.getRedirectedLocation());
	}

	@Test
	public void testDoGetUserNotAdmin() throws Exception {
		final String userName = "TestUser";
		final String redirectUrl = "http://testRedirect";
		final StringBuffer requestUrl = new StringBuffer();
		requestUrl.append(redirectUrl);

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername(userName);

		final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();

		final TemplateRendererMock rendererMock = new TemplateRendererMock();

		final HttpServletResponseMock responseMock = new HttpServletResponseMock();

		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		requestMock.setRequestURL(requestUrl);

		final HubJiraServlet servlet = new HubJiraServlet(managerMock, loginProviderMock, rendererMock);

		servlet.doGet(requestMock, responseMock);

		assertEquals(redirectUrl, responseMock.getRedirectedLocation());
	}

	@Test
	public void testDoGetUserAdmin() throws Exception {
		final String userName = "TestUser";
		final String redirectUrl = "http://testRedirect";
		final StringBuffer requestUrl = new StringBuffer();
		requestUrl.append(redirectUrl);

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername(userName);
		managerMock.setIsSystemAdmin(true);

		final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();

		final TemplateRendererMock rendererMock = new TemplateRendererMock();

		final HttpServletResponseMock responseMock = new HttpServletResponseMock();

		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		requestMock.setRequestURL(requestUrl);

		final HubJiraServlet servlet = new HubJiraServlet(managerMock, loginProviderMock, rendererMock);

		servlet.doGet(requestMock, responseMock);

		assertEquals("text/html;charset=utf-8", responseMock.getContentType());
		assertEquals("hub-jira.vm", rendererMock.getRenderedString());
	}
}
