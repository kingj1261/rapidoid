package org.rapidoid.test;

/*
 * #%L
 * rapidoid-commons
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.rapidoid.RapidoidModule;
import org.rapidoid.RapidoidModules;
import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.IntegrationTest;
import org.rapidoid.annotation.RapidoidModuleDesc;
import org.rapidoid.annotation.Since;
import org.rapidoid.beany.Metadata;
import org.rapidoid.env.Env;
import org.rapidoid.log.Log;
import org.rapidoid.util.Msc;

@Authors("Nikolche Mihajlovski")
@Since("5.1.6")
public abstract class RapidoidTest extends RapidoidThing {

	private volatile boolean hasError;

	@Before
	public final void beforeRapidoidTest() {

		Log.info("--------------------------------------------------------------------------------");
		Log.info("@" + Msc.processId() + " TEST " + getClass().getCanonicalName());
		Log.info("--------------------------------------------------------------------------------");

		hasError = false;

		isTrue(Msc.isInsideTest());
		isTrue(Env.test());

		before(this);
	}

	@After
	public final void afterRapidoidTest() {
		after(this);

		if (hasError) {
			Assert.fail("Assertion error(s) occured, probably were caught or were thrown on non-main thread!");
		}
	}

	public static void before(Object test) {
		for (RapidoidModule mod : RapidoidModules.getAllAvailable()) {
			RapidoidModuleDesc ann = mod.getClass().getAnnotation(RapidoidModuleDesc.class);
			Log.debug("Initializing module before the test", "module", ann.name(), "order", ann.order());
			mod.beforeTest(test, isIntegrationTest(test));
		}

		Log.debug("All modules are initialized");
	}

	public static void after(Object test) {
		for (RapidoidModule mod : RapidoidModules.getAllAvailable()) {
			mod.afterTest(test, isIntegrationTest(test));
		}
	}

	public static boolean isIntegrationTest(Object test) {
		return Metadata.getAnnotationRecursive(test.getClass(), IntegrationTest.class) != null;
	}

	protected void registerError(AssertionError e) {
		hasError = true;
		e.printStackTrace();
	}

	protected void fail(String msg) {
		try {
			Assert.fail(msg);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void isNull(Object value) {
		try {
			Assert.assertNull(value);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void notNull(Object value) {
		try {
			Assert.assertNotNull(value);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void isTrue(boolean cond) {
		try {
			Assert.assertTrue(cond);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void isFalse(boolean cond) {
		try {
			Assert.assertFalse(cond);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void neq(Object unexpected, Object actual) {
		try {
			Assert.assertNotEquals(unexpected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(Object expected, Object actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(String expected, String actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(char expected, char actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(long expected, long actual) {
		try {
			Assert.assertEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(double expected, double actual) {
		eq(actual, expected, 0);
	}

	protected void eqApprox(double expected, double actual) {
		eq(actual, expected, 0.00000001);
	}

	protected void eq(double expected, double actual, double delta) {
		try {
			Assert.assertEquals(expected, actual, delta);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(byte[] expected, byte[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(char[] expected, char[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(int[] expected, int[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(long[] expected, long[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(float[] expected, float[] actual, float delta) {
		try {
			Assert.assertArrayEquals(expected, actual, delta);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(double[] expected, double[] actual, double delta) {
		try {
			Assert.assertArrayEquals(expected, actual, delta);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(boolean[] expected, boolean[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void eq(Object[] expected, Object[] actual) {
		try {
			Assert.assertArrayEquals(expected, actual);
		} catch (AssertionError e) {
			registerError(e);
			throw e;
		}
	}

	protected void contains(String expectedSubstring, String actual) {
		notNull(actual);
		isTrue(actual.contains(expectedSubstring));
	}

}
