package org.rapidoid.setup;

/*
 * #%L
 * rapidoid-http-server
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.collection.Coll;
import org.rapidoid.commons.Arr;
import org.rapidoid.config.Conf;
import org.rapidoid.config.ConfigHelp;
import org.rapidoid.data.JSON;
import org.rapidoid.env.Env;
import org.rapidoid.io.Res;
import org.rapidoid.ioc.Beans;
import org.rapidoid.ioc.IoC;
import org.rapidoid.ioc.IoCContext;
import org.rapidoid.job.Jobs;
import org.rapidoid.log.Log;
import org.rapidoid.render.Templates;
import org.rapidoid.reverseproxy.Reverse;
import org.rapidoid.scan.ClasspathScanner;
import org.rapidoid.scan.ClasspathUtil;
import org.rapidoid.scan.Scan;
import org.rapidoid.u.U;
import org.rapidoid.util.Msc;

import java.util.List;
import java.util.Set;

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class App extends RapidoidThing {

	private static volatile String[] path;

	private static volatile String mainClassName;
	private static volatile String appPkgName;
	private static volatile boolean dirty;
	private static volatile boolean restarted;
	private static volatile boolean managed;

	private static final Set<Class<?>> invoked = Coll.synchronizedSet();

	static volatile ClassLoader loader = App.class.getClassLoader();

	public static void args(String[] args, String... extraArgs) {
		args = Arr.concat(extraArgs, args);

		ConfigHelp.processHelp(args);

		Env.setArgs(args);

		AppVerification.selfVerify(args);
	}

	public static AppBootstrap bootstrap(String[] args, String... extraArgs) {
		args = Arr.concat(extraArgs, args);

		args(args);

		if (!managed) scan();

		return boot();
	}

	public static AppBootstrap run(String[] args, String... extraArgs) {
		args = Arr.concat(extraArgs, args);

		args(args);
		// no implicit classpath scanning here

		return boot();
	}

	public static AppBootstrap boot() {
		Jobs.initialize();

		registerConfigListeners();

		AppBootstrap bootstrap = new AppBootstrap();
		bootstrap.services();
		return bootstrap;
	}

	private static void registerConfigListeners() {
		Conf.PROXY.addChangeListener(new ProxyConfigListener());
		Conf.API.addChangeListener(new APIConfigListener());
	}

	public static void profiles(String... profiles) {
		Env.setProfiles(profiles);
		Conf.reset();
	}

	public static void path(String... path) {
		App.path = path;
	}

	public static synchronized String[] path() {
		inferCallers();

		if (App.path == null) {
			App.path = appPkgName != null ? U.array(appPkgName) : new String[0];
		}

		return path;
	}

	static void inferCallers() {
		if (!restarted && appPkgName == null && mainClassName == null) {

			appPkgName = Msc.getCallingPackage();

			if (mainClassName == null) {
				Class<?> mainClass = Msc.getCallingMainClass();
				invoked.add(mainClass);
				mainClassName = mainClass != null ? mainClass.getName() : null;
			}

			if (mainClassName != null || appPkgName != null) {
				Log.info("Inferring application root", "!main", mainClassName, "!package", appPkgName);
			}
		}
	}

	private static synchronized void restartApp() {
		if (!Msc.hasRapidoidWatch()) {
			Log.warn("Cannot reload/restart the application, module rapidoid-watch is missing!");
		}

		if (mainClassName == null) {
			Log.warn("Cannot reload/restart the application, the main app class couldn't be detected!");
		}

		Msc.logSection("!Restarting the web application...");

		restarted = true;

		Set<AppRestartListener> listeners = U.set(On.changes().getRestartListeners());

		for (AppRestartListener listener : listeners) {
			try {
				listener.beforeAppRestart();
			} catch (Exception e) {
				Log.error("Error occurred in the app restart listener!", e);
			}
		}

		App.path = null;

		Conf.reset();
		Env.reset();
		Res.reset();
		Templates.reset();
		JSON.reset();

		AppBootstrap.reset();
		ClasspathScanner.reset();
		invoked.clear();

		for (Setup setup : Setup.instances()) {
			setup.reload();
		}

		Setup.initDefaults();

		if (Msc.hasRapidoidJPA()) {
			loader = ReloadUtil.reloader();
			ClasspathUtil.setDefaultClassLoader(loader);
		}

		Class<?> entry;
		try {
			entry = loader.loadClass(mainClassName);
		} catch (ClassNotFoundException e) {
			Log.error("Cannot restart the application, the main class (app entry point) is missing!");
			return;
		}

		Msc.invokeMain(entry, U.arrayOf(String.class, Env.args()));

		for (AppRestartListener listener : listeners) {
			try {
				listener.afterAppRestart();
			} catch (Exception e) {
				Log.error("Error occurred in the app restart listener!", e);
			}
		}

		Log.info("!Successfully restarted the application!");
	}

	public static void resetGlobalState() {
		mainClassName = null;
		appPkgName = null;
		restarted = false;
		managed = false;
		dirty = false;
		path = null;
		loader = App.class.getClassLoader();
		Setup.initDefaults();
		AppBootstrap.reset();
		invoked.clear();
		Reverse.reset();
	}

	public static void notifyChanges() {
		if (!dirty) {
			dirty = true;
			Log.info("Detected class or resource changes");
		}
	}

	static void restartIfDirty() {
		if (dirty) {
			synchronized (Setup.class) {
				if (dirty) {
					restartApp();
					dirty = false;
				}
			}
		}
	}

	public static List<Class<?>> findBeans(String... packages) {
		if (U.isEmpty(packages)) {
			packages = path();
		}

		return Scan.annotated(IoC.ANNOTATIONS).in(packages).loadAll();
	}

	public static boolean scan(String... packages) {

		String appPath = Conf.APP.entry("path").str().getOrNull();
		if (U.notEmpty(appPath)) {
			App.path(appPath);
		}

		List<Class<?>> beans = App.findBeans(packages);
		beans(beans.toArray());
		return !beans.isEmpty();
	}

	public static void beans(Object... beans) {
		Setup.ON.beans(beans);
	}

	public static IoCContext context() {
		return IoC.defaultContext();
	}

	static void filterAndInvokeMainClasses(Object[] beans) {
		managed(true);
		Msc.filterAndInvokeMainClasses(beans, invoked);
	}

	public static boolean isRestarted() {
		return restarted;
	}

	public static boolean managed() {
		return managed;
	}

	public static void managed(boolean managed) {
		App.managed = managed;
	}

	public static void register(Beans beans) {
		Setup.ON.register(beans);
	}

	public static void shutdown() {
		Setup.shutdownAll();
	}

}
