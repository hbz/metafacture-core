/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.util.tries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A simple Trie, which accepts a trailing wildcard
 *
 * @author Markus Michael Geipel
 * @author Pascal Christoph
 *
 * @param <P>
 *            type of value stored
 */
public final class WildcardTrie<P> {
	/**
	 *
	 * @param <T>
	 */
	private final class Node<T> {

		private Set<T> values = Collections.emptySet();
		private final CharMap<Node<T>> links = new CharMap<Node<T>>();

		protected Node() {
			// nothing to do
		}

		public Node<T> addNext(final char key) {
			final Node<T> next = new Node<T>();
			this.links.put(key, next);
			if (key == WildcardTrie.STAR_WILDCARD) {
				next.links.put(WildcardTrie.STAR_WILDCARD, next);
			}
			return next;
		}

		public void addValue(final T value) {
			if (this.values == Collections.emptySet()) {
				this.values = new LinkedHashSet<T>();
			}
			this.values.add(value);
		}

		public Node<T> getNext(final char key) {
			return this.links.get(key);
		}

		public Set<T> getValues() {
			return this.values;
		}
	}

	public static final char STAR_WILDCARD = '*';
	public static final char Q_WILDCARD = '?';

	public static final String OR_STRING = "|";
	private static final Pattern OR_PATTERN = Pattern.compile(WildcardTrie.OR_STRING,
			Pattern.LITERAL);

	private final Node<P> root = new Node<P>();
	private Set<Node<P>> nodes = new HashSet<Node<P>>();

	private Set<Node<P>> nextNodes = new HashSet<Node<P>>();

	public List<P> get(final String key) {
		try {
			this.nodes.add(this.root);
		} catch (final StackOverflowError t) {
			System.out.println("Used Memory   :  "
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
					+ " bytes");
			System.out.println("Free Memory   : " + Runtime.getRuntime().freeMemory() + " bytes");
			System.out.println("Total Memory  : " + Runtime.getRuntime().totalMemory() + " bytes");
			System.out.println("Max Memory    : " + Runtime.getRuntime().maxMemory() + " bytes");
			t.printStackTrace();
			this.nodes.clear();
			return Collections.emptyList();
		}
		final int length = key.length();
		for (int i = 0; i < length; ++i) {
			Node<P> temp;
			for (final Node<P> node : this.nodes) {
				temp = node.getNext(key.charAt(i));
				if (temp != null) {
					this.nextNodes.add(temp);
				}
				temp = node.getNext(WildcardTrie.Q_WILDCARD);
				if (temp != null) {
					this.nextNodes.add(temp);
				}

				temp = node.getNext(WildcardTrie.STAR_WILDCARD);
				if (temp != null) {
					this.nextNodes.add(temp);
					if (temp != node) {
						temp = temp.getNext(key.charAt(i));
						if (temp != null) {
							this.nextNodes.add(temp);
						}
					}
				}
			}
			this.nodes.clear();
			final Set<Node<P>> tmp = this.nodes;
			this.nodes = this.nextNodes;
			this.nextNodes = tmp;
		}

		List<P> matches = Collections.emptyList();
		for (final Node<P> node : this.nodes) {
			final Set<P> values = node.getValues();
			if (!values.isEmpty()) {
				if (matches == Collections.emptyList()) {
					matches = new ArrayList<P>();
				}
				matches.addAll(values);
			}
		}
		this.nodes.clear();
		this.nextNodes.clear();
		return matches;
	}

	/**
	 * inserts keys into the try. Use '|' to concatenate. Use '*' (0,inf) and
	 * '?' (1,1) to express wildcards.
	 *
	 * @param keys
	 * @param value
	 */
	public void put(final String keys, final P value) {
		if (keys.contains(WildcardTrie.OR_STRING)) {
			final String[] keysSplit = WildcardTrie.OR_PATTERN.split(keys);
			for (final String string : keysSplit) {
				simplyPut(string, value);
			}
		} else {
			simplyPut(keys, value);
		}
	}

	private void simplyPut(final String key, final P value) {

		final int length = key.length();

		Node<P> node = this.root;
		Node<P> next = null;
		for (int i = 0; i < length; ++i) {
			next = node.getNext(key.charAt(i));
			if (next == null) {
				next = node.addNext(key.charAt(i));
			}
			node = next;
		}
		node.addValue(value);
	}
}
