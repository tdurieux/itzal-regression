package fr.inria.lille.spirals.itzal.regression.proxy;

import org.eclipse.jetty.client.api.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestListener {
	void onRequestSuccess(HttpServletRequest clientRequest,
			HttpServletResponse proxyResponse, Response serverResponse, RequestContent input,
			RequestContent body);

	void onRequestFailure(HttpServletRequest clientRequest,
			HttpServletResponse proxyResponse, Response serverResponse, RequestContent input,
			RequestContent body);

	void onRequestBegin(HttpServletRequest request, HttpServletResponse response);
}
