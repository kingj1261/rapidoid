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

import org.rapidoid.annotation.*;
import org.rapidoid.beany.Metadata;
import org.rapidoid.cls.Cls;
import org.rapidoid.ioc.IoCContext;
import org.rapidoid.log.Log;
import org.rapidoid.security.Secure;
import org.rapidoid.u.U;
import org.rapidoid.util.Constants;
import org.rapidoid.util.UTILS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class PojoHandlersSetup {

	private static final Set<String> CONTROLLER_ANNOTATIONS = U.set(
			Page.class.getName(), GET.class.getName(), POST.class.getName(),
			PUT.class.getName(), DELETE.class.getName(), PATCH.class.getName(),
			OPTIONS.class.getName(), HEAD.class.getName(), TRACE.class.getName()
	);

	private final Setup setup;
	private final Object[] beans;

	private PojoHandlersSetup(Setup setup, Object[] beans) {
		this.setup = setup;
		this.beans = beans;
	}

	public static PojoHandlersSetup from(Setup setup, Object[] beans) {
		return new PojoHandlersSetup(setup, beans);
	}

	public void register() {
		process(true);
	}

	public void deregister() {
		process(false);
	}

	private void process(boolean register) {
		for (Object bean : beans) {
			processBean(register, bean);
		}
	}

	private void processBean(boolean register, Object bean) {
		Class<?> clazz;
		U.notNull(bean, "bean");
		IoCContext context = setup.getIoCContext();

		if (bean instanceof Class<?>) {
			clazz = (Class<?>) bean;
			bean = null;
		} else {
			clazz = bean.getClass();
		}

		if (!Cls.isBeanType(clazz)) {
			throw new RuntimeException("Expected a bean, but found value of type: " + clazz.getName());
		}

		Log.debug("Processing bean", "class", clazz, "instance", bean);

		List<String> componentPaths = getComponentNames(clazz);

		for (String ctxPath : componentPaths) {
			for (Method method : Cls.getMethods(clazz)) {
				if (shouldExpose(method)) {

					if (bean == null) {
						bean = register ? context.singleton(clazz) : null;
					}

					registerOrDeregister(register, bean, ctxPath, method);
				}
			}
		}
	}

	private boolean shouldExpose(Method method) {
		boolean isUserDefined = !method.getDeclaringClass().equals(Object.class);

		int modifiers = method.getModifiers();

		boolean isAbstract = Modifier.isAbstract(modifiers);
		boolean isStatic = Modifier.isStatic(modifiers);
		boolean isPrivate = Modifier.isPrivate(modifiers);
		boolean isProtected = Modifier.isProtected(modifiers);

		if (isUserDefined && !isAbstract && !isStatic && !isPrivate && !isProtected && method.getAnnotations().length > 0) {
			for (Annotation ann : method.getAnnotations()) {
				String annoName = ann.annotationType().getName();
				if (CONTROLLER_ANNOTATIONS.contains(annoName)) {
					return true;
				}
			}
		}

		return false;
	}

	protected List<String> getComponentNames(Class<?> component) {
		Controller controller = Metadata.classAnnotation(component, Controller.class);

		if (controller != null) {
			return U.list(controller.value());
		} else {
			return U.list("/");
		}
	}

	private void registerOrDeregister(boolean register, Object bean, String ctxPath, Method method) {

		Set<String> rolesAllowed = Secure.getRolesAllowed(method);
		String[] roles = rolesAllowed.toArray(new String[rolesAllowed.size()]);

		for (Annotation ann : method.getAnnotations()) {
			if (ann instanceof Page) {
				Page page = (Page) ann;

				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					OnRoute route = setup.page(path).roles(roles);

					if (U.notEmpty(page.view())) {
						route.view(page.view());
					}

					if (U.notEmpty(page.title())) {
						route.title(page.title());
					}

					if (page.raw()) {
						route.html(method, bean);
					} else {
						route.render(method, bean);
					}

				} else {
					setup.deregister(Constants.GET_OR_POST, path);
				}

			} else if (ann instanceof GET) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.get(path).json(method, bean);
				} else {
					setup.deregister(Constants.GET, path);
				}

			} else if (ann instanceof POST) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.post(path).json(method, bean);
				} else {
					setup.deregister(Constants.POST, path);
				}

			} else if (ann instanceof PUT) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.put(path).json(method, bean);
				} else {
					setup.deregister(Constants.PUT, path);
				}

			} else if (ann instanceof DELETE) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.delete(path).json(method, bean);
				} else {
					setup.deregister(Constants.DELETE, path);
				}

			} else if (ann instanceof PATCH) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.patch(path).json(method, bean);
				} else {
					setup.deregister(Constants.PATCH, path);
				}

			} else if (ann instanceof OPTIONS) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.options(path).json(method, bean);
				} else {
					setup.deregister(Constants.OPTIONS, path);
				}

			} else if (ann instanceof HEAD) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.head(path).json(method, bean);
				} else {
					setup.deregister(Constants.HEAD, path);
				}

			} else if (ann instanceof TRACE) {
				String path = pathOf(method, ctxPath, uriOf(ann));

				if (register) {
					setup.trace(path).json(method, bean);
				} else {
					setup.deregister(Constants.TRACE, path);
				}
			}
		}
	}

	private String uriOf(Annotation ann) {
		Method valueMethod = Cls.getMethod(ann.getClass(), "value");
		String uri = Cls.invoke(valueMethod, ann);

		if (U.isEmpty(uri)) {
			Method uriMethod = Cls.getMethod(ann.getClass(), "uri");
			uri = Cls.invoke(uriMethod, ann);
		}

		return uri;
	}

	private String pathOf(Method method, String ctxPath, String uri) {
		String path = !U.isEmpty(uri) ? uri : method.getName();
		return UTILS.uri(ctxPath, path);
	}

}
