package org.rapidoid.http;

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

import org.rapidoid.RapidoidModule;
import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.RapidoidModuleDesc;
import org.rapidoid.annotation.Since;
import org.rapidoid.ioc.IoC;
import org.rapidoid.jpa.JPAUtil;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import org.rapidoid.setup.On;
import org.rapidoid.setup.Setup;
import org.rapidoid.u.U;

@Authors("Nikolche Mihajlovski")
@Since("5.3.0")
@RapidoidModuleDesc(name = "HTTP", order = 700)
public class HttpModule extends RapidoidThing implements RapidoidModule {

	@Override
	public void beforeTest(Object test, boolean isIntegrationTest) {
		cleanUp();

		App.beans(IoC.defaultContext().getManagedInstances());
		App.beans(IoC.defaultContext().getManagedClasses());
	}

	@Override
	public void afterTest(Object test, boolean isIntegrationTest) {
		cleanUp();
	}

	private void cleanUp() {
		JPAUtil.reset(); // FIXME JPA module

		My.reset();
		App.resetGlobalState();
		On.changes().ignore();

		for (Setup setup : Setup.instances()) {
			setup.routes().reset();
			U.must(setup.routes().all().isEmpty());
		}
	}

}
