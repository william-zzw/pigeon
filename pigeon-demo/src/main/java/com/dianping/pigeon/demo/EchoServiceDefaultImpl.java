/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dpsf.async.ServiceCallback;
import com.dianping.dpsf.exception.DPSFException;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.util.InvokerHelper;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderHelper;
import com.dianping.pigeon.util.ContextUtils;

public class EchoServiceDefaultImpl implements EchoService {

	List<User<?>> users = new ArrayList<User<?>>();

	UserService userService = null;

	public EchoServiceDefaultImpl() {
		InvokerConfig<UserService> config = new InvokerConfig<UserService>(UserService.class);
		config.setTimeout(1000);
		config.setCallType("callback");
		userService = ServiceFactory.getService(config);
	}

	@Override
	public String echo(String msg) {
		// System.out.println("client-ip:" +
		// ContextUtils.getLocalContext("CLIENT_IP"));
		// System.out.println("request-key:" +
		// ContextUtils.getLocalContext("key1"));
		// System.out.println("global-SOURCE_APP:" +
		// ContextUtils.getGlobalContext("SOURCE_APP"));
		// System.out.println("SOURCE_APP:" +
		// ContextUtils.getGlobalContext("SOURCE_APP"));
		// System.out.println("SOURCE_IP:" +
		// ContextUtils.getGlobalContext("SOURCE_IP"));
		 try {
		 Thread.sleep(10);
		 } catch (InterruptedException e) {
		 }
		// System.out.println(msg);
		ContextUtils.putResponseContext("key1", "repsonse1");
		// ProviderHelper.writeSuccessResponse(ProviderHelper.getContext(),
		// "async echo:" + msg);

		// return "echo:" + userService.echo(msg);
		return "echo:" + msg;
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public List<User<?>> findUsers(int count) {
		// return Lists.newArrayList(users.subList(0, count));
		return users.subList(0, count);
	}

	@Override
	public void addUser(User<?> user) {
		users.add(user);
	}

	@Override
	public String test(Map<User, String> values) {
		System.out.println(users);
		return values.size() + "";
	}

	public static void main(String[] args) {
		Map<User, String> values = new HashMap<User, String>();
		values.put(new User(1, "wuxiang", "wuxiang@dianping.com", "", 35), "hello, wuxiang");
		System.out.println(new JacksonSerializer().serializeObject(values));
	}

	@Override
	public String asyncEcho(String msg) {
		ServiceCallback callback = new ServiceCallback() {
			private ProviderContext context = ProviderHelper.getContext();

			@Override
			public void callback(Object result) {
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {
				}
				ProviderHelper.writeSuccessResponse(context, "echo service:" + result);
			}

			@Override
			public void serviceException(Exception e) {

			}

			@Override
			public void frameworkException(DPSFException e) {

			}

		};
		InvokerHelper.setCallback(callback);
		userService.echo(msg);
		return null;
	}

	@Override
	public Map<String, String> testMap(Map<String, String> values) {
		System.out.println(values);
		return values;
	}

}