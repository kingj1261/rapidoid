package org.rapidoid.goodies;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.group.GroupOf;
import org.rapidoid.group.Groups;
import org.rapidoid.gui.GUI;
import org.rapidoid.html.Tag;
import org.rapidoid.http.Req;
import org.rapidoid.http.ReqRespHandler;
import org.rapidoid.http.Resp;
import org.rapidoid.process.ProcessHandle;
import org.rapidoid.u.U;

import java.util.List;

/*
 * #%L
 * rapidoid-web
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

@Authors("Nikolche Mihajlovski")
@Since("5.3.0")
public class ProcessDetailsHandler extends GUI implements ReqRespHandler {

	@Override
	public Object execute(Req req, Resp resp) {
		List<Object> info = U.list();

		String groupName = req.data("group");
		String id = req.data("id");

		GroupOf<ProcessHandle> group = Groups.find(ProcessHandle.class, groupName);
		U.must(group != null, "Cannot find the process group!");

		ProcessHandle handle = group.find(id);
		U.must(handle != null, "Cannot find the process!");

		info.add(h1("Process details"));
		info.add(code(U.join(" ", handle.params().command())));

		info.add(h2("Standard output:"));
		info.add(showOutput(handle.out()));

		info.add(h2("Error output:"));
		info.add(showOutput(handle.err()));

		return multi(info);
	}

	public static List<Tag> showOutput(String out) {
		String[] lines = out.split("\n");
		List<Tag> els = U.list();

		for (String line : lines) {
			line = line.trim();

			els.add(pre(line).class_(getOutputLineClass(line)));
		}

		return els;
	}

	public static String getOutputLineClass(String line) {
		String upperLine = line.toUpperCase();

		if (upperLine.contains("[SEVERE]")) return "proc-out proc-out-severe";
		if (upperLine.contains("[WARNING]") || upperLine.contains("[WARN]")) return "proc-out proc-out-warning";
		if (upperLine.contains("[ERROR]")) return "proc-out proc-out-error";

		return "proc-out proc-out-default";
	}

}
