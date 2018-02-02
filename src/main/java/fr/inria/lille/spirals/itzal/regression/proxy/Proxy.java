package fr.inria.lille.spirals.itzal.regression.proxy;

import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Callback;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy {
	private List<RequestListener> listeners = new ArrayList<>();
	private final int port;
	private String target;
	private Map<HttpServletRequest, RequestContent> responses = new HashMap<>();
	private Map<HttpServletRequest, RequestContent> inputs = new HashMap<>();

	public Proxy(String target, int port) {
		this.target = target;
		this.port = port;
	}

	public void addRequestListener(RequestListener listener) {
		listeners.add(listener);
	}

	public void start() {
		final Server server = new Server(port);

		// Create root context and add the ProxyServlet.Transparent to it
		ServletContextHandler contextHandler = new ServletContextHandler();
		server.setHandler(contextHandler);

		final ProxyServlet servlet = new ProxyServlet() {
			@Override
			protected void onProxyResponseSuccess(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse) {
				super.onProxyResponseSuccess(clientRequest, proxyResponse, serverResponse);
				for (RequestListener listener : listeners) {
					listener.onRequestSuccess(clientRequest, proxyResponse, serverResponse, inputs.get(clientRequest), responses.get(clientRequest));
				}
				responses.remove(clientRequest);
				inputs.remove(clientRequest);
			}

			@Override
			protected void onClientRequestFailure(HttpServletRequest clientRequest,
					Request proxyRequest, HttpServletResponse proxyResponse,
					Throwable failure) {
				super.onClientRequestFailure(clientRequest, proxyRequest,
						proxyResponse,
						failure);
			}

			@Override
			protected void onProxyResponseFailure(HttpServletRequest clientRequest,
					HttpServletResponse proxyResponse, Response serverResponse,
					Throwable failure) {
				super.onProxyResponseFailure(clientRequest, proxyResponse, serverResponse, failure);
				for (RequestListener listener : listeners) {
					listener.onRequestFailure(clientRequest, proxyResponse, serverResponse, inputs.get(clientRequest), responses.get(clientRequest));
				}
				responses.remove(clientRequest);
				inputs.remove(clientRequest);
			}

			@Override
			protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				for (RequestListener listener : listeners) {
					listener.onRequestBegin(request, response);
				}
				super.service(request, response);
			}

			@Override
			protected ContentProvider proxyRequestContent(
					final HttpServletRequest request,
					HttpServletResponse response, Request proxyRequest)
					throws IOException {
				RequestContent content = new RequestContent();
				if (!inputs.containsKey(request)) {
					inputs.put(request, content);
				} else {
					content = inputs.get(request);
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024];
				int len;
				while ((len = request.getInputStream().read(buffer)) > -1 ) {
					baos.write(buffer, 0, len);
				}
				baos.flush();

				InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
				content.addBody(new InputStreamContentProvider(new ByteArrayInputStream(baos.toByteArray())), request.getContentLength());

				return new InputStreamContentProvider(is1) {
					@Override
					public long getLength() {
						return request.getContentLength();
					}
				};
			}

			@Override
			protected void onResponseContent(HttpServletRequest request, HttpServletResponse response, Response proxyResponse, byte[] buffer, int offset, int length, Callback callback) {
				if (!responses.containsKey(request)) {
					responses.put(request, new RequestContent());
				}
				responses.get(request).addBody(length, buffer);
				super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
			}

			@Override
			protected String rewriteTarget(HttpServletRequest clientRequest) {
				StringBuilder uri = new StringBuilder(target);
				String path = clientRequest.getRequestURI();
				if(target.endsWith("/")) {
					uri.setLength(uri.length() - 1);
				}

				String rest = path;
				if(rest != null && rest.length() > 0) {
					if(!rest.startsWith("/")) {
						uri.append("/");
					}

					uri.append(rest);
				}

				String query = "";
				try {
					query = clientRequest.getQueryString();
					if (query != null) {
						String rewrittenURI = "://";
						if (uri.indexOf("/",
								uri.indexOf(rewrittenURI) + rewrittenURI.length())
								< 0) {
							uri.append("/");
						}

						uri.append("?").append(query);
					}
				} catch (NullPointerException e) {
					// ignore
				}
				System.out.println(URI.create(uri.toString()).normalize());
				return URI.create(uri.toString()).normalize().toString();
			}
		};

		contextHandler.addServlet(new ServletHolder(servlet), "/*");

		// Start the server
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}