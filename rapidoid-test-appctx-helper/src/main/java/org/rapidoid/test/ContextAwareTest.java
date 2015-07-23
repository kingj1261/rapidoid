package org.rapidoid.test;

/*
 * #%L
 * rapidoid-test-appctx-helper
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
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

import org.junit.After;
import org.junit.Before;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.appctx.Application;
import org.rapidoid.appctx.Applications;
import org.rapidoid.ctx.Ctxs;
import org.rapidoid.http.Router;

@Authors("Nikolche Mihajlovski")
@Since("4.1.0")
public abstract class ContextAwareTest extends TestCommons {

	protected Application app;

	protected Router router;

	@Before
	public void openContext() {
		app = Applications.openRootContext();
		router = app.getRouter();
	}

	@After
	public void closeContext() {
		Ctxs.close();
	}

}
