/*
 * Adopted from Java Tutorials Code Examples
 * 
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of Oracle or the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.processmining.framework.util.ui.widgets;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

public class ProMAutoCompletingTextField extends ProMTextField {

	private static final long serialVersionUID = 3530217391302932937L;

	private final class DocumentListenerImpl implements DocumentListener {
		public void removeUpdate(DocumentEvent e) {
		}

		public void insertUpdate(DocumentEvent e) {
			if (e.getLength() != 1) {
				return;
			}

			try {
				int pos = e.getOffset();
				String content = e.getDocument().getText(0, pos + 1);

				int startIndex;
				for (startIndex = pos; startIndex >= 0; startIndex--) {
					if (isStopCharacter(content.charAt(startIndex))) {
						break;
					}
				}
				int charactersInWord = pos - startIndex;
				if (charactersInWord < 2) {
					// Too few chars
					return;
				}

				String prefix = e.getDocument().getText(startIndex + 1, e.getDocument().getLength());
				int wordIndex = Collections.binarySearch(getDictionary(), prefix);
				if (wordIndex < 0 && -wordIndex <= getDictionary().size()) {
					String match = getDictionary().get(-wordIndex - 1);
					if (match.startsWith(prefix)) {
						// A completion is found
						String completion = match.substring(pos - startIndex);
						// We cannot modify Document from within notification,
						// so we submit a task that does the change later
						SwingUtilities.invokeLater(new CompletionTask(completion, pos + 1));
					}
				} else {
					// Nothing found
					mode = Mode.INSERT;
				}
			} catch (BadLocationException e1) {
			}
		}

		public void changedUpdate(DocumentEvent e) {
		}
	}

	private static enum Mode {
		INSERT, COMPLETION
	};

	private class CompletionTask implements Runnable {

		String completion;
		int position;

		CompletionTask(String completion, int position) {
			this.completion = completion;
			this.position = position;
		}

		public void run() {
			try {
				getTextField().getDocument().insertString(position, completion, null);
				getTextField().setCaretPosition(position + completion.length());
				getTextField().moveCaretPosition(position);
				mode = Mode.COMPLETION;
			} catch (BadLocationException e) {
			}
		}
	}

	private class CommitAction extends AbstractAction {

		private static final long serialVersionUID = 98076970931098392L;

		public void actionPerformed(ActionEvent ev) {
			try {
				if (mode == Mode.COMPLETION) {
					int pos = getTextField().getSelectionEnd();
					getTextField().getDocument().insertString(pos, " ", null);
					getTextField().setCaretPosition(pos + 1);
					mode = Mode.INSERT;
				} else {
					getTextField().replaceSelection("");
					getTextField().postActionEvent();
				}
			} catch (BadLocationException e) {
			}
		}
	}

	private Mode mode = Mode.INSERT;

	private ImmutableList<String> dictionary = ImmutableList.of();
	private ImmutableSet<Character> stopCharacter = ImmutableSet.of('.', '\"', '=', '>', '<', '(', ')');

	public ProMAutoCompletingTextField() {
		super();
		init();
	}

	public ProMAutoCompletingTextField(String initial, String hint) {
		super(initial, hint);
		init();
	}

	public ProMAutoCompletingTextField(String initial) {
		super(initial);
		init();
	}

	private void init() {
		getTextField().getDocument().addDocumentListener(new DocumentListenerImpl());
		InputMap im = getTextField().getInputMap();
		ActionMap am = getTextField().getActionMap();
		im.put(KeyStroke.getKeyStroke("ENTER"), "commit");
		am.put("commit", new CommitAction());
	}

	public ImmutableList<String> getDictionary() {
		return dictionary;
	}

	public void setDictionary(List<String> dictionary) {
		this.dictionary = Ordering.natural().immutableSortedCopy(dictionary);
	}

	public ImmutableSet<Character> getStopCharacter() {
		return stopCharacter;
	}

	protected boolean isStopCharacter(char c) {
		return stopCharacter.contains(c);
	}

	public void setStopCharacter(Set<Character> stopCharacter) {
		this.stopCharacter = ImmutableSet.copyOf(stopCharacter);
	}

}