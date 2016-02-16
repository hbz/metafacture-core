/*
 *  Copyright 2013, 2014, 2016 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.source;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.LoggerFactory;

/**
 * Reads a directory and emits all filenames found.
 *
 * @author Markus Michael Geipel
 * @author Fabian Steeg (fsteeg)
 * @author Pascal Christoph (dr0i)
 */
@In(String.class)
@Out(String.class)
@Description("Reads a directory and emits all filenames found.")
public final class DirReader extends DefaultObjectPipe<String, ObjectReceiver<String>> {

	private boolean recursive;

	private String filenameFilterPattern = null;

	private void dir(final File dir) {
		final ObjectReceiver<String> receiver = getReceiver();
		final File[] files = this.filenameFilterPattern == null ? dir.listFiles()
				: dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.matches(DirReader.this.filenameFilterPattern);
					}
				});
		Arrays.sort(files);
		for (final File file : files) {
			if (file.isDirectory()) {
				if (this.recursive) {
					dir(file);
				}
			} else {
				receiver.process(file.getAbsolutePath());
			}
		}
	}

	@Override
	public void process(final String dir) {
		final File file = new File(dir);
		if (file.isDirectory()) {
			dir(file);
		} else {
			try {
				getReceiver().process(dir);
			} catch (final MetafactureException e) {
				LoggerFactory.getLogger(DirReader.class).error("Problems with file '" + file + "'",
						e);
				getReceiver().resetStream();
			}
		}
	}

	public void setFilenamePattern(final String filenameFilterPattern) {
		this.filenameFilterPattern = filenameFilterPattern;
	}

	/**
	 * Set to 'true' if directories should be recursively processed. Default is
	 * 'false' and thus only files residing directly in the directory will be
	 * processed.
	 *
	 * @param boolean
	 *            recursive, default is 'false'
	 */
	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}
}
