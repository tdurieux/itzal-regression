package fr.inria.lille.spirals.itzal.regression.proxy;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.PatchActivation;
import fr.inria.spirals.npefix.resi.context.Location;
import org.eclipse.jetty.client.GZIPContentDecoder;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;

public class Main {
	private static final int PORT = 9999;
	private static String TARGET = "http://localhost:8080/";
	private static String projectRoot = "/home/thomas/git/shopizer-regression/sm-shop/";

	private static final Location[] locations = new Location[]{
		new Location("org.mayocat.context.AbstractScopeCookieContainerFilter", 202, -1, -1), // 5
		new Location("org.mayocat.context.RequestContextInitializer", 163, -1, -1), // 1
		new Location("org.mayocat.store.rdbms.dbi.argument.PostgresUUIDArrayArgumentFactory:", 163, -1, -1), // 4
		new Location("org.mayocat.store.rdbms.dbi.argument.DateAsTimestampArgumentFactory", 163, -1, -1), // 4
		new Location("org.mayocat.shop.catalog.store.jdbi.mapper.ProductMapper", 44, -1, -1), // 5
		new Location("org.mayocat.shop.cart.internal.DefaultCartLoader", 88, -1, -1), // 2
			new Location("org.broadleafcommerce.core.search.service.solr.SolrHelperServiceImpl$1", 593, -1, -1), // 2
			new Location("com.salesmanager.shop.store.controller.category.ShoppingCategoryController", 253, -1, -1) // 2
	};

	public static void main(String[] args) {
		Location location = locations[6];
		int idPatch = 1;

		if (args.length == 4) {
			projectRoot = args[0];
			TARGET = args[1];
			location = new Location(args[2].split(":")[0], Integer.parseInt(args[2].split(":")[1]), -1, -1);
			idPatch = Integer.parseInt(args[3]);
		}
		deleteOutputs();

		try {
			Registry registry = LocateRegistry.getRegistry(Config.CONFIG.getServerHost(), Config.CONFIG.getServerPort());

			PatchActivation patchActivation = (PatchActivation) registry.lookup(Config.CONFIG.getServerName());
			patchActivation.activatedPoint(location, idPatch);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Proxy proxy = new Proxy(TARGET, PORT);

		proxy.addRequestListener(new RequestListener() {
			@Override
			public void onRequestSuccess(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse, RequestContent input, RequestContent body) {
				onRequestEnd(clientRequest, proxyResponse, serverResponse, input, body);
			}

			@Override
			public void onRequestFailure(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse, RequestContent input, RequestContent body) {
				onRequestEnd(clientRequest, proxyResponse, serverResponse, input, body);
			}

			@Override
			public void onRequestBegin(HttpServletRequest request, HttpServletResponse response) {
				deleteOutputs();
			}

			private synchronized void onRequestEnd(HttpServletRequest clientRequest, HttpServletResponse proxyResponse, Response serverResponse, RequestContent input, RequestContent body) {
				String contentType = serverResponse.getHeaders().get("Content-type");
				if (contentType != null && (contentType.contains("javascript") || contentType.contains("image") || contentType.contains("plain") || contentType.contains("css"))) {
					return;
				}
				JSONObject requestOutput = new JSONObject();
				requestOutput.put("date", new Date().getTime());
				JSONObject executedIf = new JSONObject();
				requestOutput.put("executedIf", executedIf);

				JSONObject jsonRequest = new JSONObject();
				jsonRequest.put("url", serverResponse.getRequest().getURI());
				jsonRequest.put("query", serverResponse.getRequest().getQuery());

				if (input != null && input.getBody() != null) {
					jsonRequest.put("parameters", new String(input.getBody()));
				}
				jsonRequest.put("method", serverResponse.getRequest().getMethod());

				requestOutput.put("request", jsonRequest);
				System.out.println(jsonRequest);

				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("status", serverResponse.getStatus());
				if (body != null) {
					jsonResponse.put("body", getHtmlBody(body.getBody(), serverResponse.getHeaders(), contentType));
				}
				requestOutput.put("contentType", contentType);
				requestOutput.put("response", jsonResponse);

				File root = new File(projectRoot + "/instrumentation-output.csv");
				if (root.exists()) {
					try {
						List<String> lines = Files.readAllLines(root.toPath(), Charset.defaultCharset());
						for (String line : lines) {
							String[] split = line.split("\t");
							long date = Long.parseLong(split[0]);
							String classname = split[1];
							int ifLine = Integer.parseInt(split[2]);
							int sourceStart = Integer.parseInt(split[3]);
							int sourceEnd = Integer.parseInt(split[4]);
							boolean value = Boolean.parseBoolean(split[5]);
							String expression = split[6];

							String key = classname + ":" + line + ":" + sourceStart + "-" + sourceEnd;

							if (!executedIf.has(key)) {
								JSONObject tracedIf = new JSONObject();
								JSONObject jsonLocation = new JSONObject();
								jsonLocation.put("class", classname);
								jsonLocation.put("line", ifLine);
								jsonLocation.put("sourceStart", sourceStart);
								jsonLocation.put("sourceEnd", sourceEnd);
								tracedIf.put("location", jsonLocation);
								tracedIf.put("checkNotNull", expression.contains("!="));
								tracedIf.put("true", value?1:0);
								tracedIf.put("false", !value?1:0);
								tracedIf.put("expression", expression);

								executedIf.put(key, tracedIf);
							} else {
								if (value) {
									int countTrue = executedIf.getJSONObject(key).getInt("true");
									executedIf.getJSONObject(key).put("true", countTrue + 1);
								} else {
									int countFalse = executedIf.getJSONObject(key).getInt("false");
									executedIf.getJSONObject(key).put("false", countFalse + 1);
								}
							}
						}
						//FileWriter writer = new FileWriter(new File("./regression-" + new Date().getTime() + ".json"));
						//output.write(writer);
						//writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						root.delete();
					}
				}

				JSONObject appliedPatches = new JSONObject();
				root = new File(projectRoot + "/appliedPatch-output.csv");
				if (root.exists()) {
					try {
						List<String> lines = Files.readAllLines(root.toPath(), Charset.defaultCharset());
						for (String line : lines) {
							String[] split = line.split("\t");
							long date = Long.parseLong(split[0]);
							String classname = split[1];
							int ifLine = Integer.parseInt(split[2]);
							int sourceStart = Integer.parseInt(split[3]);
							int sourceEnd = Integer.parseInt(split[4]);
							int patchId = Integer.parseInt(split[5]);

							JSONObject appliedPatch = new JSONObject();
							appliedPatch.put("date", date);
							appliedPatch.put("classname", classname);
							appliedPatch.put("line", ifLine);
							appliedPatch.put("sourceStart", sourceStart);
							appliedPatch.put("sourceEnd", sourceEnd);
							appliedPatch.put("patchId", patchId);
							appliedPatch.put("count", 1);
							appliedPatches.put(classname + ":" + ifLine + " " + patchId, appliedPatch);
						}
						requestOutput.put("appliedPatches", appliedPatches);
					} catch (IOException e) {
						root.delete();
					}
				}

				root = new File(projectRoot + "/block-output.csv");
				JSONObject coverage = readCoverage(root);
				requestOutput.put("blockCoverage", coverage);

				root = new File(projectRoot + "/method-output.csv");
				coverage = readCoverage(root);
				requestOutput.put("methodCoverage", coverage);

				try (FileWriter writer = new FileWriter(new File("request-" + new Date().getTime() + ".json"))) {
					requestOutput.write(writer);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			private String getHtmlBody(byte[] body, HttpFields headers, String contentType){
				if(headers.containsKey("Content-Encoding") && "gzip".equals(headers.get("Content-Encoding"))) {
					if (contentType != null && contentType.startsWith("text")) {
						return new String(new GZIPContentDecoder().decode(ByteBuffer.wrap(body, 0, body.length)).array());
					}
					return "";
				}
				if (body != null) {
					return new String(body);
				}
				return "";
			}
		});

		proxy.start();
	}

	private static void deleteOutputs() {
		File root = new File(projectRoot + "/instrumentation-output.csv");
		if (root.exists()) {
			root.delete();
		}
		root = new File(projectRoot + "/appliedPatch-output.csv");
		if (root.exists()) {
			root.delete();
		}
		root = new File(projectRoot + "/block-output.csv");
		if (root.exists()) {
			root.delete();
		}
		root = new File(projectRoot + "/method-output.csv");
		if (root.exists()) {
			root.delete();
		}
	}

	private static JSONObject readCoverage(File root) {
		JSONObject coverages = new JSONObject();
		if (root.exists()) {
			try {
				List<String> lines = Files.readAllLines(root.toPath(), Charset.defaultCharset());
				for (String line : lines) {
					String[] split = line.split("\t");
					long date = Long.parseLong(split[0]);
					String classname = split[1];
					int ifLine = Integer.parseInt(split[2]);
					int sourceStart = Integer.parseInt(split[3]);
					int sourceEnd = Integer.parseInt(split[4]);

					if (coverages.has(classname + ":" + ifLine)) {
						JSONObject coverage = coverages.getJSONObject(classname + ":" + ifLine);
						coverage.put("count", coverage.getInt("count") + 1);
					} else {
						JSONObject coverage = new JSONObject();
						coverage.put("date", date);
						coverage.put("classname", classname);
						coverage.put("line", ifLine);
						coverage.put("sourceStart", sourceStart);
						coverage.put("sourceEnd", sourceEnd);
						coverage.put("count", 1);
						coverages.put(classname + ":" + ifLine, coverage);
					}
				}
			} catch (IOException e) {
				root.delete();
			}
		}
		return coverages;
	}
}